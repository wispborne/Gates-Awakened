package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.campaign.rules.MemoryAPI

class Memory(private val memoryApi: MemoryAPI) {
    companion object {
        const val INTRO_QUEST_IN_PROGRESS = "intro_quest_in_progress"
        const val INTRO_QUEST_DONE = "intro_quest_done"

        const val MID_QUEST_IN_PROGRESS = "mid_quest_in_progress"
        const val MID_QUEST_DONE = "mid_quest_done"

        const val CREATE_GATE_QUEST_IN_PROGRESS = "create_gate_in_progress"
        const val CREATE_GATE_QUEST_DONE = "create_gate_done"
        const val CREATE_GATE_HAULER_SUMMON_TIMESTAMP = "create_gate_hauler_summon_timestamp"
        const val CREATE_GATE_LOCATION_FOR_GATE = "create_gate_location_for_gate"

        const val GATE_ACTIVATION_CODES_REMAINING = "activation_codes_remaining"
    }

    operator fun get(key: String): Any? {
        val keyWithPrefix = createPrefixedKey(key)
        return memoryApi[keyWithPrefix] as? Any?
    }

    operator fun set(key: String, value: Any) {
        memoryApi[createPrefixedKey(key)] = value
    }

    fun unset(key: String) {
        memoryApi.unset(createPrefixedKey(key))
    }

    private fun createPrefixedKey(key: String) = if (key.startsWith('$')) key else "$${MOD_PREFIX}${key}"
}