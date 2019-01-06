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

        if (!currentGate.hasTag(GateCommandPlugin.TAG_GATE_ACTIVATED)) {
            textPanel.addParagraph("Your engineers estimate with ${GateCommandPlugin.commodityCostString}, they can reactivate the gate.")
        }

        if (GateCommandPlugin.debug) {
            textPanel.addParagraph("All gates:")

            for (gate in GateCommandPlugin.getGateMap(GateFilter.All)) {
                textPanel.addParagraph(gate.systemName + " at " + gate.distanceFromPlayer)
            }

            textPanel.addParagraph("All activated gates:")

            for (gate in GateCommandPlugin.getGateMap(GateFilter.Active)) {
                textPanel.addParagraph("${gate.systemName} at ${gate.distanceFromPlayer}")
            }
        }

        return true
    }
}
