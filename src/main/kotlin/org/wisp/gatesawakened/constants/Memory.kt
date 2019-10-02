package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.campaign.rules.MemoryAPI

class Memory(private val memoryApi: MemoryAPI) {
    companion object {
        const val INTRO_QUEST_IN_PROGRESS = "${MOD_PREFIX}intro_quest_in_progress"
        const val INTRO_QUEST_DONE = "${MOD_PREFIX}intro_quest_done"

        const val MID_QUEST_IN_PROGRESS = "${MOD_PREFIX}mid_quest_in_progress"
        const val MID_QUEST_DONE = "${MOD_PREFIX}mid_quest_done"

        const val GATE_ACTIVATION_CODES_REMAINING = "${MOD_PREFIX}activation_codes_remaining"
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

    private fun createPrefixedKey(key: String) = if (key.startsWith('$')) key else "$${key}"
}