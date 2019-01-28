package org.toast.activegates

import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.util.Misc

fun TextPanelAPI.addPara(text: String, vararg highlights: String) =
    this.addPara(text, Misc.getHighlightColor(), *highlights)