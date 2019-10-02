package org.wisp.gatesawakened

import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.constants.Strings
import org.wisp.gatesawakened.constants.Tags

object GateActivation {

    /**
     * Activate a gate. Does not affect activation codes.
     */
    internal fun activate(gate: Gate): Boolean {
        if (gate.isGate && Tags.TAG_GATE_ACTIVATED !in gate.tags) {
            gate.tags += Tags.TAG_GATE_ACTIVATED
            storeOriginalGateNameInMemory(gate)
            gate.name = Strings.activeGateName
            Common.updateActiveGateIntel()
            return true
        }

        return false
    }

    /**
     * Deactivate a gate. Does not affect activation codes.
     */
    internal fun deactivate(gate: Gate): Boolean {
        if (gate.isGate && gate.canBeDeactivated) {
            gate.tags -= Tags.TAG_GATE_ACTIVATED
            gate.name = popOriginalGateNameFromMemory(gate) ?: Strings.inactiveGateName
            Common.updateActiveGateIntel()
            return true
        }

        return false
    }

    private fun storeOriginalGateNameInMemory(gate: Gate) {
        di.memory[MOD_PREFIX + gate.id] = gate.name
    }

    private fun popOriginalGateNameFromMemory(gate: Gate): String? {
        val value = di.memory[MOD_PREFIX + gate.id] as? String?
        di.memory.unset(MOD_PREFIX + gate.id)
        return value
    }
}