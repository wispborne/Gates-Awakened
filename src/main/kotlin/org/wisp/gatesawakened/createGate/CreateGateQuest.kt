package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.createToken
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.wispLib.PersistentNullableData

object CreateGateQuest {
    fun shouldOfferQuest(): Boolean =
        Midgame.wasQuestCompleted
                && hasQuestBeenStarted != true
                && wasQuestCompleted != true
                && isEndgame()

    var gateSummonedTimestamp: Long? by PersistentNullableData("create_gate_hauler_summon_timestamp")

    var wasGateSummoned: Boolean? by PersistentNullableData("create_gate_was_gate_summoned")

    var summonLocation: SectorEntityToken? by PersistentNullableData("create_gate_location_for_gate")

    var hasQuestBeenStarted: Boolean? by PersistentNullableData("create_gate_in_progress", defaultValue = false)

    var wasGateDelivered: Boolean? by PersistentNullableData("create_gate_gate_delivered", defaultValue = false)

    var wasQuestCompleted: Boolean? by PersistentNullableData("create_gate_done", defaultValue = false)

    var numberOfDaysToDeliverGate = di.settings.getInt("GatesAwakened_numberOfDaysToDeliverGate")

    const val PREREQ_FLEET_POINTS = 180
    const val PREREQ_COLONIES = 3
    const val PREREQ_COLONY_ESTABLISHED_DAYS = 60

    /**
     * We are defining midgame as either:
     * - Player has a large enough fleet, or
     * - Player has three established colonies.
     */
    fun isEndgame(): Boolean =
        Common.playerFleetPoints >= PREREQ_FLEET_POINTS || Common.establishedPlayerColonyCount(
            PREREQ_COLONY_ESTABLISHED_DAYS
        ) > PREREQ_COLONIES

    fun startQuest() {
        di.intelManager.addIntel(CreateGateQuestIntel())
        hasQuestBeenStarted = true
    }

    fun placeGateAtPlayerLocationAfterDelay() {
        summonLocation = di.sector.playerFleet.createToken()
        gateSummonedTimestamp = di.sector.clock.timestamp

        di.sector.addScript(CountdownToGateHaulerScript())
        wasGateSummoned = true
    }

    fun spawnGateQuestStep() {
        Common.spawnGateAtLocation(summonLocation, activateAfterSpawning = true)
            ?.apply {
                this.tags += Tags.TAG_NEWLY_CONSTRUCTED_GATE
            }
        addGateDefenceFleet(summonLocation)
        wasGateDelivered = true
        di.intelManager.addIntel(GateDeliveredIntel(summonLocation))
    }

    fun completeQuest(createdGate: SectorEntityToken?) {
        wasQuestCompleted = true
        createdGate?.removeTag(Tags.TAG_NEWLY_CONSTRUCTED_GATE)
    }

    private fun addGateDefenceFleet(gateToDefend: SectorEntityToken?) {
        if (gateToDefend == null) {
            di.errorReporter.reportCrash(NullPointerException("Gate to defend cannot be null"))
            return
        }

        val fleetParams = FleetParamsV3(
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
                setLocation(gateToDefend.location.x, gateToDefend.location.y)
                name = "Gate Drone Fleet of The Reach"
            }
        gateToDefend.starSystem.addEntity(fleet)
        Misc.makeHostile(fleet)
        fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, gateToDefend, 1000f)
    }

    enum class Rules(val text: String) {
        Proximity("\" - May not be located at an unsafe proximity to a celestial body.\""),
        Hyperspace("\" - May not be located in hyperspace. Doing so will collapse hyperspace in a 10 ly radius.\""),
        MultipleGates("\" - Multiple Gates within the same star system will result in total system hyperwave collapse.\"")
    }
}