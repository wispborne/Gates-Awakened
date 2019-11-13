package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara

class GateCreatedIntel(locationOfGate: SectorEntityToken?) : IntelDefinition(
    title = "Gate Placed",
    iconPath = "graphics/intel/g8_gate.png",
    durationInDays = 10f,
    smallDescriptionCreator = { info: TooltipMakerAPI, width: Float, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        info.addPara {
            "A Gate has been moved into place in ${locationOfGate?.starSystem?.baseName}."
        }
    },
    endLocation = locationOfGate,
    showDaysSinceCreated = true,
    important = true,
    intelTags = listOf(
        Tags.INTEL_EXPLORATION,
        Tags.INTEL_STORY,
        org.wisp.gatesawakened.constants.Tags.INTEL_ACTIVE_GATE
    )
)