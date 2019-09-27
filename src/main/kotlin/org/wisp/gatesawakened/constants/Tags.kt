package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.campaign.StarSystemAPI

internal object Tags {
    /**
     * Vanilla tag for a gate.
     **/
    const val TAG_GATE = com.fs.starfarer.api.impl.campaign.ids.Tags.GATE

    /** A gate that has been activated by the player **/
    const val TAG_GATE_ACTIVATED = "g8_gate_activated"

    /** The tag used by the Active Gates mod, added so those gates may be used as well **/
    const val TAG_ACTIVE_GATES_GATE_ACTIVATED = "gate_activated"

    /** Displays as a tag in the Intel Manager screen */
    const val INTEL_ACTIVE_GATE = "Active Gates"

    /** The gate in the Core that is activated for the intro event **/
    const val TAG_GATE_INTRO_CORE = "g8_gate_intro_core"

    /** The gate near the edge of the sector that is activated for the intro event **/
    const val TAG_GATE_INTRO_FRINGE = "g8_gate_intro_fringe"

    const val TAG_PLANET_WITH_CACHE = "g8_planet_with_cache"

    const val TAG_BLACKLISTED_SYSTEM = "g8_blacklisted_system"

    private val Dme = listOf(
        "theme_breakers",
        "theme_breakers_resurgent"
    )

    private val Vanilla = listOf(
        "theme_remnant_main",
        "theme_remnant_resurgent"
    )

    /**
     * These tags mark systems that we don't want to drop the player into, eg systems with remnants.
     */
    val systemTagsToAvoidRandomlyChoosing: List<String> =
        listOf(TAG_BLACKLISTED_SYSTEM)
            .plus(Vanilla)
            .plus(Dme)

    /**
     * These are systems we don't want to allow the mod to interact with at all.
     */
    val systemTagsToBlacklist = listOf(
        TAG_BLACKLISTED_SYSTEM
    )
}

internal val StarSystemAPI.isValidSystemForRandomActivation: Boolean
    get() = this.tags.none { Tags.systemTagsToAvoidRandomlyChoosing.contains(it) }

internal val StarSystemAPI.isBlacklisted: Boolean
    get() = this.tags.any { Tags.systemTagsToBlacklist.contains(it) }