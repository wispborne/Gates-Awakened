package org.toast.activegates

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class AddGateData : BaseCommandPlugin() {
    override fun execute(ruleId: String, dialog: InteractionDialogAPI?,
                         params: List<Misc.Token>,
                         memoryMap: Map<String, MemoryAPI>): Boolean {
        if (dialog == null) return false

        val textPanel = dialog.textPanel
        val currentGate = dialog.interactionTarget

        if (!currentGate.hasTag(ActiveGates.TAG_GATE_ACTIVATED)) {
            textPanel.addParagraph(Strings.activationExplanation)
            textPanel.addParagraph(Strings.activationCost)
        }

        if (ActiveGates.inDebugMode) {
            textPanel.addParagraph(Strings.debugAllGates)

            for (gate in ActiveGates.getGates(GateFilter.All)) {
                textPanel.addParagraph(Strings.debugGateAndDistance(gate.systemName, gate.distanceFromPlayer))
            }

            textPanel.addParagraph(Strings.debugActiveGates)

            for (gate in ActiveGates.getGates(GateFilter.Active)) {
                textPanel.addParagraph(Strings.debugGateAndDistance(gate.systemName, gate.distanceFromPlayer))
            }
        }

        return true
    }
}
