package org.toast.activegates.intro

import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.logging.i
import org.toast.activegates.*
import kotlin.random.Random

internal object Intro {
    fun haveGatesBeenTagged(): Boolean =
        Common.getGates(GateFilter.IntroCore).any()

    /**
     * Finds a pair of gates, one in the core and one on the edge of the sector, and tags them for later use.
     */
    fun findAndTagIntroGatePair() {
        if (haveGatesBeenTagged()) {
            Di.inst.logger.i({ "Went to go tag intro gates, but it's already done!" })
        }

        val coreGate = findClosestInactiveGateToCenter()

        if (coreGate == null) {
            Di.inst.logger.warn("No inactive gates for core gate, cannot start AG early game mission")
            return
        }

        val fringeGate = findRandomFringeGate()

        if (fringeGate == null) {
            Di.inst.logger.warn("No inactive gates for fringe gate, cannot start AG early game mission")
            return
        }

        coreGate.gate.addTag(Tags.TAG_GATE_INTRO_CORE)
        fringeGate.gate.addTag(Tags.TAG_GATE_INTRO_FRINGE)
    }

    private fun findClosestInactiveGateToCenter(): GateDestination? =
        Common.getGates(GateFilter.Inactive)
            .filter { it.gate.starSystem.isValidSystemForRandomActivation }
            .sortedBy { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .firstOrNull()

    private fun findRandomFringeGate(): GateDestination? =
        Common.getGates(GateFilter.Inactive)
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