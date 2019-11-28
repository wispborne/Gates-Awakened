package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.campaign.rules.MemoryAPI

class Memory(private val memoryApi: MemoryAPI) {
    companion object {
        const val INTRO_QUEST_IN_PROGRESS = "intro_quest_in_progress"
        const val INTRO_QUEST_DONE = "intro_quest_done"

        const val MID_QUEST_IN_PROGRESS = "mid_quest_in_progress"
        const val MID_QUEST_DONE = "mid_quest_done"

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