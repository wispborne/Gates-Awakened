package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara
import kotlin.math.roundToInt


class CreateGateQuestIntel : IntelDefinition(
    title = "The Reach",
    iconPath = "graphics/intel/g8_gate.png",
    infoCreator = { info: TooltipMakerAPI? ->
        info?.addPara(padding = 0f, textColor = Misc.getHighlightColor()) {
            "Gate Hauler Detected"
        }
    },
    smallDescriptionCreator = { info: TooltipMakerAPI, width: Float, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        val gateSummonedTimestamp = CreateGateQuest.gateSummonedTimestamp

        if (gateSummonedTimestamp == null) {
            info.addPara {
                "A Gate Hauler is standing by, waiting for you to choose a location for a new Gate."
            }
            info.addButton("Place Gate here", BUTTON_CHOOSE, width, 20f, 10f)
        } else {
            info.addPara {
                val daysUntilGateIsDelivered =
                    CreateGateQuest.numberOfDaysToDeliverGate -
                            di.sector.clock.getElapsedDaysSince(gateSummonedTimestamp)
                "A Gate Hauler is en route to deliver a Gate to " + mark(CreateGateQuest.summonLocation!!.name) +
                        " and will arrive in " + mark(Misc.getStringForDays(daysUntilGateIsDelivered.roundToInt())) + "."
            }
        }
    },
    intelTags = listOf(
        Tags.INTEL_EXPLORATION,
        Tags.INTEL_STORY,
        org.wisp.gatesawakened.constants.Tags.INTEL_ACTIVE_GATE
    )
) {
    companion object {
        private const val BUTTON_CHOOSE = MOD_PREFIX + "choose"
    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean = true

    override fun createConfirmationPrompt(buttonId: Any?, prompt: TooltipMakerAPI) {
        super.createConfirmationPrompt(buttonId, prompt)

        prompt.addPara {
            "Are you sure that you would like to command the waiting Gate Hauler to " +
                    mark("place a Gate here") + "?"
        }

        prompt.addPara {
            "It will take " + mark(CreateGateQuest.numberOfDaysToDeliverGate.toString()) + " days to maneuver the Gate into place " +
                    "and it cannot be stopped once started."
        }
    }

    override fun getConfirmText(buttonId: Any?): String {
        return "Place Gate"
    }

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        super.buttonPressConfirmed(buttonId, ui)
        CreateGateQuest.placeGateAtPlayerLocationAfterDelay()
    }

    override fun isDone(): Boolean = CreateGateQuest.wasQuestCompleted
    override fun isEnded(): Boolean = CreateGateQuest.wasQuestCompleted
}