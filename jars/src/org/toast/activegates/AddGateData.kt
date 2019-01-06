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

        if (dialog == null) return false

        val textPanel = dialog.textPanel
        val gate = dialog.interactionTarget
        val mem = gate.memoryWithoutUpdate

        if (!gate.hasTag(GateCommandPlugin.ACTIVATED)) {
            textPanel.addParagraph("It will take $commodityCostString to activate the gate.")
        }
        textPanel.addParagraph("It costs ${fuelCostPerLY.roundToInt()} fuel per light year to use the gate for travel.")

        if (debug) {
            val allGates = getGateMap(Tags.GATE)
            textPanel.addParagraph("All gates:")
            for (key in allGates.keys) {
                textPanel.addParagraph(allGates[key] + " at " + key)
            }
        }

        val map = getGateMap(GateCommandPlugin.ACTIVATED)
        val iter = map.keys.iterator()

        if (debug) {
            textPanel.addParagraph("All activated gates:")
            for (key in map.keys) {
                textPanel.addParagraph(map[key] + " at " + key)
            }
        }

        var count: Int
        val maxcount = 5
        count = 0
        while (count < maxcount) {
            mem.set("\$gate" + count + "exists", false, 0f)
            count++
        }
        count = 0
        while (count < maxcount && iter.hasNext()) {
            val key = iter.next()

            if (key != 0F) {
                count++
                if (debug) {
                    textPanel.addParagraph(count.toString() + "," + key + "," + map[key])
                }
                mem.set("\$gate" + count + "exists", true, 0f)
                mem.set("\$gate$count", map[key], 0f)
            }
        }

        return true
    }
}
