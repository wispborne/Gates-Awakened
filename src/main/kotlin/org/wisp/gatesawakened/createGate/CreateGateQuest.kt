package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.OrbitAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.activate
import org.wisp.gatesawakened.createToken
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.midgame.Midgame

object CreateGateQuest {
    private const val MEM_KEY_QUEST_IN_PROGRESS = "create_gate_in_progress"
    private const val MEM_KEY_QUEST_DONE = "create_gate_done"
    private const val MEM_KEY_HAULER_SUMMON_TIMESTAMP = "create_gate_hauler_summon_timestamp"
    private const val MEM_KEY_LOCATION_FOR_GATE = "create_gate_location_for_gate"

    fun shouldOfferQuest(): Boolean =
        Midgame.wasQuestCompleted
                && !hasQuestBeenStarted
                && !wasQuestCompleted
                && (1..10).random() == 1 // 10% chance lol

    val gateSummonedTimestamp: Long?
        get() = di.persistentData[MEM_KEY_HAULER_SUMMON_TIMESTAMP] as? Long

    val summonLocation: SectorEntityToken?
        get() = di.persistentData[MEM_KEY_LOCATION_FOR_GATE] as? SectorEntityToken

    val hasQuestBeenStarted: Boolean
        get() = di.persistentData[MEM_KEY_QUEST_IN_PROGRESS] == true
                || di.persistentData[MEM_KEY_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.persistentData[MEM_KEY_QUEST_DONE] == true

    val numberOfDaysToDeliverGate = di.settings.getInt("gatesAwakened_numberOfDaysToDeliverGate")

    fun startQuest() {
        di.intelManager.addIntel(CreateGateQuestIntel())
        di.persistentData[MEM_KEY_QUEST_IN_PROGRESS] = true
    }

    fun placeGateAtPlayerLocationAfterDelay() {
        di.persistentData[MEM_KEY_LOCATION_FOR_GATE] = di.sector.playerFleet.createToken()
        di.persistentData[MEM_KEY_HAULER_SUMMON_TIMESTAMP] = di.sector.clock.timestamp

        di.sector.addScript(CountdownToGateHaulerScript())
    }

    fun spawnGateAtLocation(location: SectorEntityToken?, activateAfterSpawning: Boolean): Boolean {
        if (location == null) {
            di.errorReporter.reportCrash(NullPointerException("Tried to spawn gate but target location was null!"))
            return false
        }

        val newGate = BaseThemeGenerator.addNonSalvageEntity(
            location.starSystem,
            BaseThemeGenerator.EntityLocation()
                .apply {
                    this.location = location.location
                    this.orbit = createOrbit(location)
                },
            "inactive_gate",
            Factions.DERELICT
        )

        if(activateAfterSpawning) {
            newGate.entity?.activate()
        }

        return true
    }

    fun addGateDefenceFleet(gateToDefend: SectorEntityToken?) {
        if (gateToDefend == null) {
            di.errorReporter.reportCrash(NullPointerException("Gate to defend cannot be null"))
            return
        }

        val fleetParams = FleetParamsV3(
            null,
            gateToDefend.locationInHyperspace,
            Factions.DERELICT,
            null,
            FleetFactory.PatrolType.COMBAT.fleetType,
            150f,
            40f,
            40f,
            40f,
            20f,
            40f,
            1f
        )

        val fleet = FleetFactoryV3.createFleet(fleetParams)
            .apply {
                isTransponderOn = false
                name = "Gate Drone Fleet"
            }
        gateToDefend.starSystem.addEntity(fleet)
        Misc.makeHostile(fleet)
        fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, gateToDefend, 1000f)
    }

    fun completeQuest() {
        val locationOfGate = summonLocation
        di.intelManager.addIntel(GateCreatedIntel(locationOfGate))
        di.persistentData.unset(MEM_KEY_LOCATION_FOR_GATE)
        di.persistentData.unset(MEM_KEY_HAULER_SUMMON_TIMESTAMP)
        di.persistentData[MEM_KEY_QUEST_DONE] = true
    }

    enum class Rules(val text: String) {
        Proximity("\" - May not be located at an unsafe proximity to a celestial body.\""),
        Hyperspace("\" - May not be located in hyperspace. Doing so will collapse hyperspace in a 10 ly radius.\""),
        MultipleGates("\" - Multiple Gates within the same star system will result in total system hyperwave collapse.\"")
    }

    private fun createOrbit(
        targetLocation: SectorEntityToken,
        orbitCenter: SectorEntityToken = targetLocation.starSystem.center
    ): OrbitAPI {
        val orbitRadius = Misc.getDistance(targetLocation, orbitCenter)

        return di.factory.createCircularOrbit(
            orbitCenter,
            Misc.getAngleInDegrees(orbitCenter.location, targetLocation.location),
            orbitRadius,
            orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f) // taken from StarSystemGenerator:1655
        )
    }
}