package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara

class GateDeliveredIntel(private val locationOfGate: SectorEntityToken?) : IntelDefinition(
    title = { if (CreateGateQuest.wasQuestCompleted == true) "Gates Awakened - Completed" else "Gate Placed" },
    iconPath = { "graphics/intel/GatesAwakened_gate.png" },
    smallDescriptionCreator = { info: TooltipMakerAPI, width: Float, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        val initialTextColor = if (isEnding) Misc.getGrayColor() else Misc.getTextColor()
        info.addPara(textColor = initialTextColor) {
            "A Gate has been moved into place in ${locationOfGate?.starSystem?.baseName}."
        }

        // After user interacts with the gate
        if (CreateGateQuest.wasQuestCompleted == true) {
            info.addPara {
                "The Gate was active - what could it mean? No answers seem" +
                        " forthcoming, but who knows what the future might hold?"
            }
            info.addPara { "\n\n\n\nThank you for playing ${mark("Gates Awakened")}!" }
            info.addPara { "\nQuestions, comments, bugs?" }
            info.addPara { "${mark("@Wisp#0302")} on Discord" }
            info.addPara { "${mark("Wispborne")} on the Forums" }

            if (!isEnding) {
                endAfterDelay(30f)
            }
        }
    },
    endLocation = locationOfGate,
    showDaysSinceCreated = false,
    important = true,
    intelTags = listOf(
        Tags.INTEL_EXPLORATION,
        Tags.INTEL_STORY,
        org.wisp.gatesawakened.constants.Tags.INTEL_ACTIVE_GATE
    )
) {
    override fun createInstanceOfSelf() = GateDeliveredIntel(locationOfGate)
}