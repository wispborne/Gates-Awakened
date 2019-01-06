package org.toast.activegates

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions
import com.fs.starfarer.api.util.Misc
import kotlin.math.roundToInt

class ShowGateDestinationOptions : PaginatedOptions() {
    override fun doesCommandAddOptions(): Boolean = true

    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: List<Misc.Token>, memoryMap: Map<String, MemoryAPI>?): Boolean {
        super.execute(ruleId, dialog, params, memoryMap)

        if (dialog == null) return false

        val textPanel = dialog.textPanel

        val activatedGates = GateCommandPlugin.getGateMap(GateCommandPlugin.ACTIVATED)

        val maxcount = 50

        addOption("Reconsider", "AG_ChoiceAbort")

        for ((index, gate) in activatedGates.take(maxcount).withIndex()) {
            if (GateCommandPlugin.debug) {
                textPanel.addParagraph(index.toString() + "," + gate.distanceFromPlayer + "," + gate.systemName)
            }

            addOption("Warp to ${gate.systemName} (${(GateCommandPlugin.fuelCostPerLY * gate.distanceFromPlayer).roundToInt()} fuel)", gate.systemId)
        }

        showOptions()
        return true
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        super.optionSelected(optionText, optionData)
        val activatedGates = GateCommandPlugin.getGateMap(GateCommandPlugin.ACTIVATED)

        if (optionData is String && optionData in activatedGates.map { it.systemId }) {
            FlyThroughGate().execute(
                    ruleId = null,
                    dialog = dialog,
                    params = listOf(Misc.Token(optionData, Misc.TokenType.LITERAL), Misc.Token(optionText, Misc.TokenType.LITERAL)),
                    memoryMap = memoryMap
            )
        }
    }
}