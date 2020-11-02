package org.wisp.gatesawakened.gateIntel

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.*
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.ParagraphText.mark
import org.wisp.gatesawakened.wispLib.addPara
import kotlin.math.roundToInt

class ActiveGateIntel(val activeGate: Gate) : IntelDefinition(
    title = { "Active Gate" },
    iconPath = { "graphics/intel/GatesAwakened_gate.png" },
    endLocation = activeGate,
    infoCreator = { text ->
        text?.addPara(
            textColor = Misc.getGrayColor(),
            padding = 0f
        ) {
            val dist = activeGate.distanceFromPlayerInHyperspace.roundToInt()
            BULLET + "In " + mark(activeGate.starSystem.baseName) +
                    if (dist > 0) " ($dist LY)"
                    else String.empty
        }
    },
    smallDescriptionCreator = { text, width, _ ->
        text.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        text.addPara { "There is an active Gate in this system." }

        if (Midgame.wasQuestCompleted) {
            text.addPara {
                "${BULLET}You have ${mark(Midgame.remainingActivationCodes)} of ${mark(Common.totalNumberOfActivationCodes)} activation codes left."
            }
        }

        if (GateIntelCommon.shouldShowInactiveGateIntel) {
            text.addButton("Hide Inactive Gates", InactiveGateIntel.TOGGLE_INACTIVE_GATE_INTEL_BUTTON, width, 20f, 10f)
        } else {
            text.addButton("Show Inactive Gates", InactiveGateIntel.TOGGLE_INACTIVE_GATE_INTEL_BUTTON, width, 20f, 10f)
        }
    },
    intelTags = listOf(Tags.INTEL_ACTIVE_GATE)
) {
    companion object {
        const val TOGGLE_INACTIVE_GATE_INTEL_BUTTON = MOD_PREFIX + "TOGGLE_INACTIVE_GATE_INTEL_BUTTON"
    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?) =
        if (buttonId == TOGGLE_INACTIVE_GATE_INTEL_BUTTON) false
        else super.doesButtonHaveConfirmDialog(buttonId)

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        super.buttonPressConfirmed(buttonId, ui)

        if (buttonId == TOGGLE_INACTIVE_GATE_INTEL_BUTTON) {
            GateIntelCommon.shouldShowInactiveGateIntel = !GateIntelCommon.shouldShowInactiveGateIntel
            GateIntelCommon.updateInactiveGateIntel()
            ui?.recreateIntelUI()
        }
    }

    override fun isEnded(): Boolean = !activeGate.isActive
    override fun createInstanceOfSelf() = ActiveGateIntel(activeGate)
    override fun getSortTier() = IntelInfoPlugin.IntelSortTier.TIER_4
}