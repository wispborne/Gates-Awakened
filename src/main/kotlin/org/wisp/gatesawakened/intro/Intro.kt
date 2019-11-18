package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.*
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.constants.Strings
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.constants.isValidSystemForRandomActivation
import org.wisp.gatesawakened.logging.i

/**
 * The intro mission chooses two gates, one in the core and one near the fringe of the sector.
 * It then tells the player to go to the fringe gate, which activates both gates and allows
 * the player to travel between them.
 */
internal object Intro {
    fun shouldOfferQuest(market: MarketAPI): Boolean =
        market.factionId !in listOf("luddic_church", "luddic_path")
                && !hasQuestBeenStarted
                && !wasQuestCompleted
                && haveGatesBeenTagged()

    fun haveGatesBeenTagged(): Boolean =
        Common.getGates(filter = GateFilter.IntroCore, excludeCurrentGate = false).any()
                && Common.getGates(filter = GateFilter.IntroFringe, excludeCurrentGate = false).any()

    val fringeGate: Gate?
        get() = Common.getGates(GateFilter.IntroFringe, excludeCurrentGate = false).firstOrNull()?.gate

    val coreGate: Gate?
        get() = Common.getGates(GateFilter.IntroCore, excludeCurrentGate = false).firstOrNull()?.gate

    val hasQuestBeenStarted: Boolean
        get() = di.memory[Memory.INTRO_QUEST_IN_PROGRESS] == true
                || di.memory[Memory.INTRO_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.memory[Memory.INTRO_QUEST_DONE] == true

    /**
     * Finds a pair of gates, one in the core and one on the edge of the sector, and tags them for later use.
     */
    fun findAndTagIntroGatePair() {
        if (!Common.isDebugModeEnabled && haveGatesBeenTagged()) {
            di.logger.i { "Went to go tag intro gates, but it's already done!" }
        }

        val coreGate = findClosestInactiveGateToCenter()

        if (coreGate == null) {
            di.logger.warn("No inactive gates for core gate, cannot start AG early game quest")
            return
        }

        val fringeGate = findRandomFringeGate()

        if (fringeGate == null) {
            di.logger.warn("No inactive gates for fringe gate, cannot start AG early game quest")
            return
        }

        coreGate.gate.addTag(Tags.TAG_GATE_INTRO_CORE)
        fringeGate.gate.addTag(Tags.TAG_GATE_INTRO_FRINGE)
    }

    /**
     * Attempts to start the intro quest by adding player intel and activating the destination gate.
     * Called when the player has interacted with [IntroQuestBeginning].
     *
     * @return true if successful, false otherwise
     */
    fun startQuest(foundAt: SectorEntityToken): Boolean {
        val destination = fringeGate
        var success = false

        if (destination != null && !hasQuestBeenStarted) {
            val intel = IntroIntel(destination)

            if (!intel.isDone) {
                di.memory[Memory.INTRO_QUEST_IN_PROGRESS] = true
                di.sector.intelManager.addIntel(intel)
                destination.name = Strings.activeGateName

                success = true
            }
        }

        return success
    }

    /**
     * Once player has traveled through the first gate, activate both gates for arbitrary use
     * and then learn them on how gates work.
     */
    fun displayIntroQuestEpilogueWindow() {
        fringeGate?.activate()
        coreGate?.activate()
        di.memory[Memory.INTRO_QUEST_DONE] = true
        (di.sector.intelManager.getFirstIntel(IntroIntel::class.java) as? IntroIntel?)
            ?.run { di.sector.intelManager.removeIntel(this) }

        // Pop up a dialog explaining how gates work
        di.sector.campaignUI.showInteractionDialog(IntroQuestEpilogueDialog().build(), di.sector.playerFleet)
    }

    private fun findClosestInactiveGateToCenter(): GateInfo? {
        di.logger.i { "Finding gate closest to center and choosing first valid." }
        di.logger.i { "Invalid tags: ${Tags.systemTagsToAvoidRandomlyChoosing}" }

        @Suppress("SimplifiableCallChain")
        return Common.getGates(GateFilter.Inactive, excludeCurrentGate = false)
            .sortedBy { Misc.getDistanceLY(it.gate.starSystem.location, di.sector.hyperspace.location) }
            .filter {
                val isValid = it.gate.starSystem.isValidSystemForRandomActivation

                di.logger.i {
                    val distanceFromCenter = Misc.getDistanceLY(
                        it.gate.starSystem.location,
                        di.sector.hyperspace.location
                    )
                    val validOrInvalid = if (isValid) "valid" else "invalid"

                    "$distanceFromCenter LY, ${it.gate.starSystem}, $validOrInvalid, tags: ${it.gate.starSystem.tags}"
                }

                isValid
            }
            .firstOrNull()
            .also { di.logger.i { "${it?.gate?.starSystem?.name} chosen for core gate." } }
    }

    private fun findRandomFringeGate(): GateInfo? {
        di.logger.i { "Finding gate farthest from center and choosing first valid." }
        di.logger.i { "Invalid tags: ${Tags.systemTagsToAvoidRandomlyChoosing}" }

        @Suppress("SimplifiableCallChain")
        return Common.getGates(GateFilter.Inactive, excludeCurrentGate = false)
            .sortedByDescending { it.gate.distanceFromCenterOfSector }
            .filter {
                val isValid = it.gate.starSystem.isValidSystemForRandomActivation

                di.logger.i {
                    val distanceFromCenter = it.gate.distanceFromCenterOfSector
                    val validOrInvalid = if (isValid) "valid" else "invalid"

                    "$distanceFromCenter LY, ${it.gate.starSystem}, $validOrInvalid, tags: ${it.gate.starSystem.tags}"
                }

                isValid
            }
            .ifEmpty { null }
            ?.take(5)
            ?.random()
            .also { di.logger.i { "${it?.gate?.starSystem?.name} chosen for fringe gate." } }
    }
}