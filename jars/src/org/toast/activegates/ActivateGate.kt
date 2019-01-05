package org.toast.activegates

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc

class ActivateGate : GateCommandPlugin() {

    override fun execute(ruleId: String, dialog: InteractionDialogAPI?,
                         params: List<Misc.Token>,
                         memoryMap: Map<String, MemoryAPI>): Boolean {

        if (dialog == null) return false

        val textPanel = dialog.textPanel

        val gate = dialog.interactionTarget
        if (gate.hasTag(Tags.GATE)) {
            if (!gate.hasTag(GateCommandPlugin.Companion.ACTIVATED)) {
                if (canActivate()) {
                    payActivationCost()
                    gate.addTag(GateCommandPlugin.Companion.ACTIVATED)
                    textPanel.addParagraph("The gate is activated.")
                } else {
                    textPanel.addParagraph("You are unable to activate the gate. " +
                            "Activation requires " + commodityCostString + ".")
                }
            } else {
                textPanel.addParagraph("The gate is already activated.")
            }
            return true
        } else {
            return false
        }
    }
}
