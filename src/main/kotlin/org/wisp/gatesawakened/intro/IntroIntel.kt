package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.empty
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara
import java.awt.Color


class IntroIntel(private val target: SectorEntityToken) : IntelDefinition(
    title = { "Gate Investigation" + if (isEnding) " - Completed" else String.empty },
    iconPath = { "graphics/intel/g8_gate_quest.png" },
    infoCreator = { info ->
        if (!isEnding) {
            info?.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) { "Investigate a possible gate at ${mark(target.starSystem.baseName)}" }
        }
    },
    smallDescriptionCreator = { info, width, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)

        info.addPara { "You saw an image of a Gate and the name of a system on a tripad in a bar." }

        if (!Intro.wasQuestCompleted) {
            info.addPara { "Perhaps it's worth a visit to ${mark(target.starSystem.baseName)} to search for a Gate." }
        } else {
            info.addPara {
                "You followed a Gate in ${mark(
                    Intro.fringeGate?.starSystem?.baseName ?: "the fringe"
                )} that led to ${mark(Intro.coreGate?.starSystem?.baseName ?: "the core")}, " +
                        "a discovery best kept quiet lest the factions interrogate you."
            }
            info.addPara {
                "Perhaps you will stumble across more Gate information in the " +
                        if (Midgame.isMidgame()) "near future."
                        else "future, when you are more established."
            }
        }
    },
    showDaysSinceCreated = true,
    startLocation = null,
    endLocation = target,
    intelTags = listOf(
        Tags.INTEL_EXPLORATION,
        Tags.INTEL_STORY,
        org.wisp.gatesawakened.constants.Tags.INTEL_ACTIVE_GATE
    )
) {
    override fun advance(amount: Float) {
        super.advance(amount)

        // If it's not already ending or ended and the quest was completed, mark the quest as complete
        if ((!isEnding || !isEnded) && Intro.wasQuestCompleted) {
            endAfterDelay()
        }
    }

    override fun createInstanceOfSelf() = IntroIntel(target)
}