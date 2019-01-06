package org.toast.activegates

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import kotlin.math.roundToInt

class AddGateData : GateCommandPlugin() {
    override fun execute(ruleId: String, dialog: InteractionDialogAPI?,
                         params: List<Misc.Token>,
                         memoryMap: Map<String, MemoryAPI>): Boolean {
        super.execute(ruleId, dialog, params, memoryMap)

        if (dialog == null) return false

        val textPanel = dialog.textPanel
        val currentGate = dialog.interactionTarget

        if (!currentGate.hasTag(GateCommandPlugin.ACTIVATED)) {
            textPanel.addParagraph("It will take $commodityCostString to activate the gate.")
        }
        textPanel.addParagraph("It costs ${fuelCostPerLY.roundToInt()} fuel per light year to use the gate for travel.")

        if (debug) {
            val allGates = getGateMap(Tags.GATE)
            textPanel.addParagraph("All gates:")
            for (gate in allGates) {
                textPanel.addParagraph(gate.systemName + " at " + gate.distanceFromPlayer)
            }
        }

        val activatedGates = getGateMap(GateCommandPlugin.ACTIVATED)

        if (debug) {
            textPanel.addParagraph("All activated gates:")
            for (gate in activatedGates) {
                textPanel.addParagraph("${gate.systemName} at ${gate.distanceFromPlayer}")
            }
        }

        return true
    }
}
