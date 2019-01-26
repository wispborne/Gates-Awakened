package org.toast.activegates.constants

import com.fs.starfarer.api.campaign.StarSystemAPI

internal object Tags {
    /**
     * Vanilla tag for a gate.
     **/
    const val TAG_GATE = com.fs.starfarer.api.impl.campaign.ids.Tags.GATE

    /** A gate that has been activated by the player **/
    const val TAG_GATE_ACTIVATED = "g8_gate_activated"

    /** The gate in the Core that is activated for the intro event **/
    const val TAG_GATE_INTRO_CORE = "g8_gate_intro_core"

    /** The gate near the edge of the sector that is activated for the intro event **/
    const val TAG_GATE_INTRO_FRINGE = "g8_gate_intro_fringe"

    const val TAG_BLACKLISTED_SYSTEM = "g8_blacklisted_system"


    val Dme = listOf(
        "theme_breakers",
        "theme_breakers_resurgent"
    )

    val Vanilla = listOf(
        "theme_remnant_main",
        "theme_remnant_resurgent"
    )

    val systemTagsToAvoidRandomlyChoosing =
        listOf(TAG_BLACKLISTED_SYSTEM)
            .plus(Vanilla)
            .plus(Dme)

    val systemTagsToBlacklist = listOf(
        TAG_BLACKLISTED_SYSTEM
    )
}

internal val StarSystemAPI.isValidSystemForRandomActivation: Boolean
    get() = this.tags.none { Tags.systemTagsToAvoidRandomlyChoosing.contains(it) }

internal val StarSystemAPI.isBlacklisted: Boolean
    get() = this.tags.any { Tags.systemTagsToBlacklist.contains(it) }