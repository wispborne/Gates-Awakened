package org.wisp.gatesawakened.constants

import org.wisp.gatesawakened.di

object PersistentData {
    operator fun get(key: String): Any? {
        val keyWithPrefix = createPrefixedKey(key)
        return di.sector.persistentData[keyWithPrefix] as? Any?
    }

    /**
     * Automatically adds mod prefix.
     */
    operator fun set(key: String, value: Any?) {
        di.sector.persistentData[createPrefixedKey(key)] = value
    }

    fun unset(key: String) {
        di.sector.persistentData.remove(createPrefixedKey(key))
    }

    private fun createPrefixedKey(key: String) = if (key.startsWith('$')) key else "$${MOD_PREFIX}${key}"
}