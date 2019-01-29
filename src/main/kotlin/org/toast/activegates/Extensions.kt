package org.toast.activegates

import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags

internal fun TextPanelAPI.addPara(text: String, vararg highlights: String) =
    this.addPara(text, Misc.getHighlightColor(), *highlights)

internal val Gate.isActive: Boolean
    get() = Tags.TAG_GATE_ACTIVATED in this.tags

internal fun Gate.activate() {
    this.name = Strings.activeGateName

    if (Tags.TAG_GATE_ACTIVATED !in this.tags) {
        this.tags += Tags.TAG_GATE_ACTIVATED
    }
}

internal val String.Companion.empty
    get() = ""