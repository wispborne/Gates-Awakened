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
        if (gate.hasTag(GateCommandPlugin.TAG_GATE)) {
            if (!gate.hasTag(GateCommandPlugin.TAG_GATE_ACTIVATED)) {
                if (GateCommandPlugin.canActivate()) {
                    GateCommandPlugin.payActivationCost()
                    gate.addTag(GateCommandPlugin.TAG_GATE_ACTIVATED)
                    gate.memory
                    textPanel.addParagraph("Your crew offload the resources and, almost reverently, a single gamma core." +
                            "They get to work and, in short order, the gate is active.")
                } else {
                    textPanel.addParagraph("You don't have the resources required to activate the gate.")
                }
            } else {
                textPanel.addParagraph("The gate is already active.")
            }
            return true
        } else {
            return false
        }
    }
}
