package org.toast.activegates.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.*
import org.toast.activegates.constants.Memory
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags
import org.toast.activegates.constants.isValidSystemForRandomActivation
import org.toast.activegates.logging.i

internal object Intro {
    fun haveGatesBeenTagged(): Boolean =
        Common.getGates(GateFilter.IntroCore, excludeCurrentGate = false).any()

    val fringeGate: Gate?
        get() = Common.getGates(GateFilter.IntroFringe, excludeCurrentGate = false).firstOrNull()?.gate

    val coreGate: Gate?
        get() = Common.getGates(GateFilter.IntroCore, excludeCurrentGate = false).firstOrNull()?.gate

    val wasIntroQuestStarted: Boolean
        get() = Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_IN_PROGRESS] == true
                || Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_DONE] == true

    val wasIntroQuestCompleted: Boolean
        get() = Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_DONE] == true

    /**
     * Finds a pair of gates, one in the core and one on the edge of the sector, and tags them for later use.
     */
    fun findAndTagIntroGatePair() {
        if (!Common.isDebugModeEnabled && haveGatesBeenTagged()) {
            Di.inst.logger.i { "Went to go tag intro gates, but it's already done!" }
        }

        val coreGate = findClosestInactiveGateToCenter()

        if (coreGate == null) {
            Di.inst.logger.warn("No inactive gates for core gate, cannot start AG early game quest")
            return
        }

        val fringeGate = findRandomFringeGate()

        if (fringeGate == null) {
            Di.inst.logger.warn("No inactive gates for fringe gate, cannot start AG early game quest")
            return
        }

        coreGate.gate.addTag(Tags.TAG_GATE_INTRO_CORE)
        fringeGate.gate.addTag(Tags.TAG_GATE_INTRO_FRINGE)
    }

    /**
     * Attempts to start the intro quest by adding player intel and activating the destination gate.
     * Called when the player has interacted with [IntroBarEvent].
     *
     * @return true if successful, false otherwise
     */
    fun startIntroQuest(foundAt: SectorEntityToken): Boolean {
        val destination = fringeGate
        var success = false

        if (destination != null && !wasIntroQuestStarted) {
            val intel = IntroIntel(foundAt, destination)

            if (!intel.isDone) {
                Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_IN_PROGRESS] = true
                Di.inst.sector.intelManager.addIntel(intel)
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
    fun introQuestEpilogue() {
        fringeGate?.activate()
        coreGate?.activate()

        // Pop up a dialog explaining how gates work
        Di.inst.sector.campaignUI.showInteractionDialog(IntroQuestEpilogueDialog(), Di.inst.sector.playerFleet)
        Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_DONE] = true
    }

    private fun findClosestInactiveGateToCenter(): GateDestination? {
        Di.inst.logger.i { "Finding gate closest to center and choosing first valid." }
        Di.inst.logger.i { "Invalid tags: ${Tags.systemTagsToAvoidRandomlyChoosing}" }

        @Suppress("SimplifiableCallChain")
        return Common.getGates(GateFilter.Inactive, excludeCurrentGate = false)
            .sortedBy { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .filter {
                val isValid = it.gate.starSystem.isValidSystemForRandomActivation

                Di.inst.logger.i {
                    val distanceFromCenter = Misc.getDistanceLY(
                        it.gate.starSystem.location,
                        Di.inst.sector.hyperspace.location
                    )
                    val validOrInvalid = if (isValid) "valid" else "invalid"

                    "$distanceFromCenter LY, ${it.gate.starSystem}, $validOrInvalid, tags: ${it.gate.starSystem.tags}"
                }

                isValid
            }
            .firstOrNull()
            .also { Di.inst.logger.i { "${it?.gate?.starSystem?.name} chosen for core gate." } }
    }

    private fun findRandomFringeGate(): GateDestination? {
        Di.inst.logger.i { "Finding gate farthest from center and choosing first valid." }
        Di.inst.logger.i { "Invalid tags: ${Tags.systemTagsToAvoidRandomlyChoosing}" }

        @Suppress("SimplifiableCallChain")
        return Common.getGates(GateFilter.Inactive, excludeCurrentGate = false)
            .sortedByDescending { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .filter {
                val isValid = it.gate.starSystem.isValidSystemForRandomActivation

                Di.inst.logger.i {
                    val distanceFromCenter = Misc.getDistanceLY(
                        it.gate.starSystem.location,
                        Di.inst.sector.hyperspace.location
                    )
                    val validOrInvalid = if (isValid) "valid" else "invalid"

                    "$distanceFromCenter LY, ${it.gate.starSystem}, $validOrInvalid, tags: ${it.gate.starSystem.tags}"
                }

                isValid
            }
            .firstOrNull()
            .also { Di.inst.logger.i { "${it?.gate?.starSystem?.name} chosen for fringe gate." } }
    }
}