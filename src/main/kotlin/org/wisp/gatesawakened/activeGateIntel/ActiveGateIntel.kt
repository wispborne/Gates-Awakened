package org.wisp.gatesawakened.activeGateIntel

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.Gate
import org.wisp.gatesawakened.appendPara
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.isActive
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara

class ActiveGateIntel(val activeGate: Gate) : IntelDefinition(
    title = { "Active Gate" },
    iconPath = { "graphics/icons/icon_portal.png" },
    endLocation = activeGate,
    infoCreator = { text ->
        text?.addPara(
            textColor = Misc.getGrayColor(),
            padding = 0f
        ) { "${BULLET}In ${mark(activeGate.starSystem.baseName)}" }
    },
    smallDescriptionCreator = { text, width, _ ->
        text.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        text.addPara {
            "There is an active Gate in this system."
        }
        if (Midgame.wasQuestCompleted) {
            text.appendPara(
                "${BULLET}You have %s activation codes left.",
                0f,
                Midgame.remainingActivationCodes.toString()
            )
        }
    },
    intelTags = listOf(Tags.INTEL_ACTIVE_GATE)
) {
    override fun isEnded(): Boolean = !activeGate.isActive
    override fun createInstanceOfSelf() = ActiveGateIntel(activeGate)
    override fun getSortTier() = IntelInfoPlugin.IntelSortTier.TIER_1
}