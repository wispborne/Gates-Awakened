package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.OrbitAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
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
        get() = di.memory[MEM_KEY_HAULER_SUMMON_TIMESTAMP] as? Long

    val summonLocation: SectorEntityToken?
        get() = di.memory[MEM_KEY_LOCATION_FOR_GATE] as? SectorEntityToken

    val hasQuestBeenStarted: Boolean
        get() = di.memory[MEM_KEY_QUEST_IN_PROGRESS] == true
                || di.memory[MEM_KEY_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.memory[MEM_KEY_QUEST_DONE] == true

    val numberOfDaysToDeliverGate = di.settings.getInt("gatesAwakened_numberOfDaysToDeliverGate")

    fun startQuest() {
        di.intelManager.addIntel(CreateGateQuestIntel())
    }

    fun placeGateAtPlayerLocationAfterDelay() {
        di.memory[MEM_KEY_LOCATION_FOR_GATE] = di.sector.playerFleet.createToken()
        di.memory[MEM_KEY_HAULER_SUMMON_TIMESTAMP] = di.sector.clock.timestamp
        di.memory[MEM_KEY_QUEST_IN_PROGRESS] = true

        di.sector.addScript(CountdownToGateHaulerScript())
    }

    fun spawnGateAtDesignatedLocation(): Boolean {
        val targetLocation = di.memory[MEM_KEY_LOCATION_FOR_GATE] as? SectorEntityToken

        if (targetLocation == null) {
            di.errorReporter.reportCrash(NullPointerException("Tried to spawn gate but target location was null!"))
            return false
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

        return true
    }

    fun completeQuest() {
        di.memory.unset(MEM_KEY_LOCATION_FOR_GATE)
        di.memory.unset(MEM_KEY_HAULER_SUMMON_TIMESTAMP)
        di.memory[MEM_KEY_QUEST_DONE] = true
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