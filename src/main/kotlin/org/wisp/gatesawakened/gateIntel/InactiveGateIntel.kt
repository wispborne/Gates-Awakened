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

class InactiveGateIntel(val inactiveGate: Gate) : IntelDefinition(
    title = { "Inactive Gate" },
    iconPath = { "graphics/intel/GatesAwakened_inactive_gate.png" },
    endLocation = inactiveGate,
    infoCreator = { text ->
        text?.addPara(
            textColor = Misc.getGrayColor(),
            padding = 0f
        ) {
            val dist = inactiveGate.distanceFromPlayerInHyperspace.roundToInt()
            BULLET + "In " + mark(inactiveGate.starSystem.baseName) +
                    if (dist > 0) " ($dist LY)"
                    else String.empty
        }
    },
    smallDescriptionCreator = { text, width, _ ->
        text.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        text.addPara { "There is an inactive Gate in this system." }

        if (Midgame.wasQuestCompleted) {
            text.addPara {
                "${BULLET}You have ${mark(Midgame.remainingActivationCodes)} of ${mark(Common.totalNumberOfActivationCodes)} activation codes left."
            }
        }

        if (GateIntelCommon.shouldShowInactiveGateIntel) {
            text.addButton("Hide Inactive Gates", TOGGLE_INACTIVE_GATE_INTEL_BUTTON, width, 20f, 10f)
        } else {
            text.addButton("Show Inactive Gates", TOGGLE_INACTIVE_GATE_INTEL_BUTTON, width, 20f, 10f)
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

    override fun isEnded(): Boolean = inactiveGate.isActive
    override fun createInstanceOfSelf() = InactiveGateIntel(inactiveGate)
    override fun getSortTier() = IntelInfoPlugin.IntelSortTier.TIER_5
}