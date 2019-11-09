package org.wisp.gatesawakened

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.constants.Tags

@Deprecated("Replace with the one in WispText")
internal fun TextPanelAPI.appendPara(text: String, vararg highlights: String) =
    this.addPara(text, Misc.getHighlightColor(), *highlights)

internal fun TooltipMakerAPI.appendPara(text: String, padding: Float = 0f, vararg highlights: String) =
    this.addPara(text, padding, Misc.getHighlightColor(), *highlights)

internal val SectorEntityToken.isGate: Boolean
    get() = com.fs.starfarer.api.impl.campaign.ids.Tags.GATE in this.tags

internal val Gate.isActive: Boolean
    get() = Tags.TAG_GATE_ACTIVATED in this.tags || Tags.TAG_ACTIVE_GATES_GATE_ACTIVATED in this.tags

/**
 * Gates that are active from other mods (Active Gates) cannot be deactivated by Gates Awakened.
 */
internal val Gate.canBeDeactivated: Boolean
    get() = Tags.TAG_GATE_ACTIVATED in this.tags

/**
 * Activate a gate. Does not affect activation codes.
 */
internal fun Gate.activate(): Boolean = GateActivation.activate(this)

/**
 * Deactivate a gate. Does not affect activation codes.
 */
internal fun Gate.deactivate(): Boolean = GateActivation.deactivate(this)

internal val SectorEntityToken.distanceFromCenterOfSector: Float
    get() = this.starSystem.distanceFromCenterOfSector

internal val StarSystemAPI.distanceFromCenterOfSector: Float
    get() = Misc.getDistanceLY(
        this.location,
        di.sector.hyperspace.location
    )

internal val SectorEntityToken.distanceFromPlayerInHyperspace: Float
    get() = this.starSystem.distanceFromPlayerInHyperspace

internal val StarSystemAPI.distanceFromPlayerInHyperspace: Float
    get() = Misc.getDistanceLY(
        this.location,
        di.sector.playerFleet.locationInHyperspace
    )

internal val String.Companion.empty
    get() = ""