package org.toast.activegates

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions
import com.fs.starfarer.api.util.Misc

class ShowGateDestinationOptions : PaginatedOptions() {
    override fun doesCommandAddOptions(): Boolean = true

    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: List<Misc.Token>, memoryMap: Map<String, MemoryAPI>?): Boolean {
        super.execute(ruleId, dialog, params, memoryMap)

        if (dialog == null) return false

        val activatedGates = ActiveGates.getGates(GateFilter.Active)

        addOption(Strings.menuOptionReconsider, "AG_ChoiceAbort")

        for ((index, gate) in activatedGates.withIndex()) {
            if (ActiveGates.inDebugMode) {
                dialog.textPanel.addParagraph(Strings.debugJumpOptionsAndDistances(index.toString(), gate.distanceFromPlayer, gate.systemName))
            }

            addOption(Strings.menuOptionJumpToSystem(gate.systemName, ActiveGates.jumpCostInFuel(gate.distanceFromPlayer)), gate.systemId)
        }

        showOptions()
        return true
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        super.optionSelected(optionText, optionData)
        val activatedGates = ActiveGates.getGates(GateFilter.Active)

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