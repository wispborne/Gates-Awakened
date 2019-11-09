package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.OrbitAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.di

object CreateGateQuest {

    fun shouldOfferQuest(): Boolean =
        !hasQuestBeenStarted
                && !wasQuestCompleted
                && (1..1).random() == 1 // 10% chance lol TODO

    // todo change to 10% chance
    fun placeGateAtPlayerLocationAfterDelay() {
        di.memory[Memory.CREATE_GATE_LOCATION_FOR_GATE] =
            di.sector.playerFleet.containingLocation.createToken(di.sector.playerFleet.location)
        di.memory[Memory.CREATE_GATE_HAULER_SUMMON_TIMESTAMP] = di.sector.clock.timestamp
        di.memory[Memory.CREATE_GATE_QUEST_IN_PROGRESS] = true

        di.sector.addScript(CountdownToGateHaulerScript())
    }

    fun spawnGateAtDesignatedLocation() {
        val targetLocation = di.memory[Memory.CREATE_GATE_LOCATION_FOR_GATE] as? SectorEntityToken

        if (targetLocation == null) {
            di.errorReporter.reportCrash(NullPointerException("Tried to spawn gate but target location was null!"))
            return
        }

        BaseThemeGenerator.addNonSalvageEntity(
            targetLocation.starSystem,
            BaseThemeGenerator.EntityLocation()
                .apply {
                    this.location = targetLocation.location
                    this.orbit = createOrbit(targetLocation)
                },
            "inactive_gate",
            Factions.DERELICT
        )

        di.memory.unset(Memory.CREATE_GATE_LOCATION_FOR_GATE)
        di.memory.unset(Memory.CREATE_GATE_HAULER_SUMMON_TIMESTAMP)
        di.memory[Memory.CREATE_GATE_QUEST_DONE] = true // Quest complete
    }

    val hasQuestBeenStarted: Boolean
        get() = di.memory[Memory.CREATE_GATE_QUEST_IN_PROGRESS] == true
                || di.memory[Memory.CREATE_GATE_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.memory[Memory.CREATE_GATE_QUEST_DONE] == true

    val numberOfDaysToDeliverGate = di.settings.getInt("gatesAwakened_numberOfDaysToDeliverGate")

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