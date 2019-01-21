package org.toast.activegates

import com.fs.starfarer.api.campaign.StarSystemAPI

internal object Compatibility {
    val systemTagsToAvoid = listOf(
        Common.TAG_AVOID,
        DME.TAG_BREAKER_SYSTEM,
        DME.TAG_BREAKER_RESURGENT_SYSTEM
    )

    object Common {
        const val TAG_AVOID = "ag_blacklist_system"
    }

    object DME {
        const val TAG_BREAKER_SYSTEM = "theme_breakers"
        const val TAG_BREAKER_RESURGENT_SYSTEM = "theme_breakers_resurgent"
    }
}

internal val StarSystemAPI.hasTagToAvoidForCompatibility: Boolean
    get() = this.tags.any { Compatibility.systemTagsToAvoid.contains(it) }