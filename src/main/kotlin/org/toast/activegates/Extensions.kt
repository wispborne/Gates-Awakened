package org.toast.activegates

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags

internal fun TextPanelAPI.appendPara(text: String, vararg highlights: String) =
    this.addPara(text, Misc.getHighlightColor(), *highlights)

val SectorEntityToken.isGate: Boolean
    get() = com.fs.starfarer.api.impl.campaign.ids.Tags.GATE in this.tags

internal val Gate.isActive: Boolean
    get() = Tags.TAG_GATE_ACTIVATED in this.tags

internal fun Gate.activate(): Boolean {
    this.name = Strings.activeGateName

    if (this.isGate && Tags.TAG_GATE_ACTIVATED !in this.tags) {
        this.tags += Tags.TAG_GATE_ACTIVATED
        return true
    }

    return false
}

internal val SectorEntityToken.distanceFromCenter: Float
    get() = this.starSystem.distanceFromCenter

internal val StarSystemAPI.distanceFromCenter: Float
    get() = Misc.getDistanceLY(
        this.location,
        di.sector.hyperspace.location
    )

internal val SectorEntityToken.distanceFromPlayer: Float
    get() = this.starSystem.distanceFromPlayer

internal val StarSystemAPI.distanceFromPlayer: Float
    get() = Misc.getDistanceLY(
        this.location,
        di.sector.playerFleet.locationInHyperspace
    )

internal val String.Companion.empty
    get() = ""