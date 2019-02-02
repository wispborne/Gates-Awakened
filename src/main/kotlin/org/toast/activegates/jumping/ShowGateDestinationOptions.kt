package org.toast.activegates.jumping

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.Common
import org.toast.activegates.FlyThroughGate
import org.toast.activegates.GateFilter
import org.toast.activegates.constants.MOD_PREFIX
import org.toast.activegates.constants.Strings

@Deprecated("Use JumpDialog")
class ShowGateDestinationOptions : PaginatedOptions() {
    override fun doesCommandAddOptions(): Boolean = true

    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI,
        params: List<Misc.Token>,
        memoryMap: Map<String, MemoryAPI>
    ): Boolean {
        super.execute(ruleId, dialog, params, memoryMap)


        val activatedGates =
            Common.getGates(GateFilter.Active, excludeCurrentGate = true)

//        addOption(Strings.menuOptionReconsider, "AG_ChoiceAbort")

        dialog.optionPanel.addOption(JumpDialog.Option.RECONSIDER.text, JumpDialog.Option.RECONSIDER.id)
        dialog.setOptionOnEscape(JumpDialog.Option.RECONSIDER.text, JumpDialog.Option.RECONSIDER.id)

        for (gate in activatedGates) {
            if (Common.isDebugModeEnabled) {
                dialog.textPanel.addParagraph(
                    "${gate.distanceFromPlayer} LY, ${gate.systemName}"
                )
            }

            dialog.optionPanel.addOption(
                Strings.menuOptionJumpToSystem(
                    systemName = gate.systemName,
                    jumpCostInFuel = Common.jumpCostInFuel(gate.distanceFromPlayer)
                ), gate.systemId
            )
        }

        for ((index, gate) in activatedGates.withIndex()) {
            if (Common.isDebugModeEnabled) {
//                dialog.textPanel.addParagraph(
//                    Strings.debugJumpOptionsAndDistances(
//                        index.toString(),
//                        gate.distanceFromPlayer,
//                        gate.systemName
//                    )
//                )
            }

            addOption(
                Strings.menuOptionJumpToSystem(
                    gate.systemName,
                    Common.jumpCostInFuel(gate.distanceFromPlayer)
                ), gate.systemId
            )
        }

        showOptions()
        return true
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        super.optionSelected(optionText, optionData)
        val activatedGates =
            Common.getGates(GateFilter.Active, excludeCurrentGate = true)

        if (optionData is String && optionData in activatedGates.map { it.systemId }) {
            FlyThroughGate().execute(
                ruleId = null,
                dialog = dialog,
                params = listOf(
                    Misc.Token(optionData, Misc.TokenType.LITERAL),
                    Misc.Token(optionText, Misc.TokenType.LITERAL)
                ),
                memoryMap = memoryMap
            )
        }
    }

    /**
     * [PaginatedOptions] requires a String id, so we can't just use [Option] itself as the option data
     */
    enum class Option(val text: String, val id: String) {
        INIT("", "${MOD_PREFIX}init"),
        FLY_THROUGH("Fly through the gate", "${MOD_PREFIX}fly_through"),
        RECONSIDER("Reconsider", "${MOD_PREFIX}reconsider"),
        LEAVE("Leave", "${MOD_PREFIX}leave")
    }
}