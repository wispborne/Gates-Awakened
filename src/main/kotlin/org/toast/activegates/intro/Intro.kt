package org.toast.activegates.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.logging.i
import org.toast.activegates.*
import org.toast.activegates.constants.Memory
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags
import org.toast.activegates.constants.isValidSystemForRandomActivation
import kotlin.random.Random

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
        if (haveGatesBeenTagged()) {
            Di.inst.logger.i({ "Went to go tag intro gates, but it's already done!" })
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

    private fun findClosestInactiveGateToCenter(): GateDestination? =
        Common.getGates(GateFilter.Inactive, excludeCurrentGate = false)
            .filter { it.gate.starSystem.isValidSystemForRandomActivation }
            .sortedBy { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .firstOrNull()

    private fun findRandomFringeGate(): GateDestination? =
        Common.getGates(GateFilter.Inactive, excludeCurrentGate = false)
            .filter { it.gate.starSystem.isValidSystemForRandomActivation }
            .sortedByDescending { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .take(5)
            .run {
                if (this.isEmpty())
                    null
                else
                    this.random(Random(Di.inst.sector.seedString.hashCode()))
            }
}