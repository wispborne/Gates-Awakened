package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.campaign.StarSystemAPI

internal object Tags {
    /**
     * Vanilla tag for a gate.
     **/
    const val TAG_GATE = com.fs.starfarer.api.impl.campaign.ids.Tags.GATE

    /** A gate that has been activated by the player **/
    const val TAG_GATE_ACTIVATED = "${MOD_PREFIX}_gate_activated"

    /** The tag used by the Active Gates mod, added so those gates may be used as well **/
    const val TAG_ACTIVE_GATES_GATE = "gate_activated"

    /** The tag used by Boggled's Player Constructed Gates mod, added so those gates may be used as well **/
    const val TAG_BOGGLED_GATE = "boggled_astral_gate"

    const val TAG_NEWLY_CONSTRUCTED_GATE = "${MOD_PREFIX}NEWLY_CONSTRUCTED_GATE"

    /** Displays as a tag in the Intel Manager screen */
    const val INTEL_ACTIVE_GATE = "Gates"

    /** The gate in the Core that is activated for the intro event **/
    const val TAG_GATE_INTRO_CORE = "${MOD_PREFIX}_gate_intro_core"

    /** The gate near the edge of the sector that is activated for the intro event **/
    const val TAG_GATE_INTRO_FRINGE = "${MOD_PREFIX}_gate_intro_fringe"

    const val TAG_PLANET_WITH_CACHE = "${MOD_PREFIX}_planet_with_cache"

    const val TAG_BLACKLISTED_SYSTEM = "${MOD_PREFIX}_blacklisted_system"

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