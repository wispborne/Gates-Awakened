package org.wisp.gatesawakened.midgame

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.empty
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara

class MidgameIntel(val planet: SectorEntityToken) : IntelDefinition(
    title = { "Planet investigation" + if (Midgame.wasQuestCompleted) " - Completed" else String.empty },
    iconPath = { "graphics/intel/GatesAwakened_gate_quest.png" },
    infoCreator = {
        if (!isEnding) {
            it?.addPara("Investigate a planet in ${planet.starSystem.baseName}", 0f)
        }
    },
    smallDescriptionCreator = { info, width, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        val stage1TextColor = if (Midgame.wasQuestCompleted) Misc.getGrayColor() else Misc.getTextColor()
        info.addPara(textColor = stage1TextColor) {
            "You saw a decoded transmission detailing Gate activation codes."
        }
        info.addPara(textColor = stage1TextColor) {
            "Perhaps it's worth a visit to ${mark(planet.name)} in ${mark(planet.starSystem.baseName)}."
        }

        if (Midgame.wasQuestCompleted) {
            info.addPara {
                "You visited a cave on ${mark(planet.name)}" +
                        " in ${mark(planet.starSystem.baseName)}" +
                        " and found activation codes for ${Midgame.midgameRewardActivationCodeCount} Gates."
            }
            info.addPara {
                "The TriPad from the cave may have one more secret - you keep an eye on it as you continue to use the Gate network."
            }
        }
    },
    showDaysSinceCreated = true,
    startLocation = null,
    endLocation = planet,
    intelTags = listOf(
        Tags.INTEL_EXPLORATION,
        Tags.INTEL_STORY,
        org.wisp.gatesawakened.constants.Tags.INTEL_ACTIVE_GATE
    )
) {

    override fun advance(amount: Float) {
        super.advance(amount)

        // If it's not already ending or ended and the quest was completed, mark the quest as complete
        if ((!isEnding || !isEnded) && Midgame.wasQuestCompleted) {
            endAfterDelay()
        }
    }

    override fun createInstanceOfSelf() = MidgameIntel(planet)
}