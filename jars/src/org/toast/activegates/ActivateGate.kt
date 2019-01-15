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
        if (gate.hasTag(ActiveGates.TAG_GATE_CANDIDATE)) {
            if (!gate.hasTag(ActiveGates.TAG_GATE_ACTIVATED)) {
                if (ActiveGates.canActivate()) {
                    ActiveGates.payActivationCost()
                    gate.addTag(ActiveGates.TAG_GATE_ACTIVATED)
                    gate.memory
                    textPanel.addParagraph(Strings.paidActivationCost)
                } else {
                    textPanel.addParagraph(Strings.insufficientResourcesToActivateGate)
                }
            } else {
                textPanel.addParagraph(Strings.gateAlreadyActive)
            }
            return true
        } else {
            return false
        }
    }
}
