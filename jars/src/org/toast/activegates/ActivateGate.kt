package org.toast.activegates

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class ActivateGate : BaseCommandPlugin() {

    override fun execute(ruleId: String, dialog: InteractionDialogAPI?,
                         params: List<Misc.Token>,
                         memoryMap: Map<String, MemoryAPI>): Boolean {

        if (dialog == null) return false

        val textPanel = dialog.textPanel

        val gate = dialog.interactionTarget
        if (gate.hasTag(ActiveGates.TAG_GATE)) {
            if (!gate.hasTag(ActiveGates.TAG_GATE_ACTIVATED)) {
                if (ActiveGates.canActivate()) {
                    ActiveGates.payActivationCost()
                    gate.addTag(ActiveGates.TAG_GATE_ACTIVATED)
                    gate.memory
                    textPanel.addParagraph(ActiveGatesStrings.paidActivationCost)
                } else {
                    textPanel.addParagraph(ActiveGatesStrings.insufficientResourcesToActivateGate)
                }
            } else {
                textPanel.addParagraph(ActiveGatesStrings.gateAlreadyActive)
            }
            return true
        } else {
            return false
        }
    }
}
