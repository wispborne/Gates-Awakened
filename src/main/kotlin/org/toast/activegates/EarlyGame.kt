package org.toast.activegates

import com.fs.starfarer.api.util.Misc

internal class EarlyGame {
    fun hasAlreadyOccurred(): Boolean =
        Common.getGates(GateFilter.All)
            .any { it.gate.hasTag(Common.TAG_GATE_INTRO_CORE) }

    fun doIt() {
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

        coreGate.gate.addTag(Common.TAG_GATE_ACTIVATED)
        coreGate.gate.addTag(Common.TAG_GATE_INTRO_CORE)
    }

    private fun findClosestInactiveGateToCenter(): GateDestination? =
        Common.getGates(GateFilter.Inactive)
            .filter { !it.gate.starSystem.hasTagToAvoidForCompatibility }
            .sortedBy { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .firstOrNull()

    private fun findRandomFringeGate(): GateDestination? =
        Common.getGates(GateFilter.Inactive)
            .filter { !it.gate.starSystem.hasTagToAvoidForCompatibility }
            .sortedByDescending { Misc.getDistanceLY(it.gate.starSystem.location, Di.inst.sector.hyperspace.location) }
            .take(5)
            .run {
                if (this.isEmpty())
                    null
                else
                    this.random()
            }
}