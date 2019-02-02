package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags
import org.toast.activegates.jumping.Jump
import org.toast.activegates.jumping.ShowGateDestinationOptions

@Deprecated("Use JumpDialog")
class FlyThroughGate : BaseCommandPlugin() {

    override fun execute(
        ruleId: String?, dialog: InteractionDialogAPI?,
        params: List<Misc.Token>,
        memoryMap: Map<String, MemoryAPI>
    ): Boolean {
        if (dialog == null) return false

        val textPanel = dialog.textPanel

        // Can only jump using activated gates
        if (!dialog.interactionTarget.hasTag(Tags.TAG_GATE_ACTIVATED)) {
            textPanel.addParagraph(Strings.flyThroughInactiveGate)
            textPanel.addParagraph(Strings.resultWhenGateDoesNotWork)
            ShowGateDestinationOptions().execute(null, dialog, emptyList(), memoryMap)
            return false
        }

        val systemIdChosenByPlayer = params[0].getStringWithTokenReplacement(ruleId, dialog, memoryMap)

        if (systemIdChosenByPlayer == null || systemIdChosenByPlayer.isEmpty()) return false

        val newSystem = Global.getSector().getStarSystem(systemIdChosenByPlayer)

        if (newSystem == null) {
            textPanel.addParagraph(Strings.errorCouldNotFindJumpSystem(systemIdChosenByPlayer))
            ShowGateDestinationOptions().execute(null, dialog, params, memoryMap)
            return false
        }

        val playerFleet = Global.getSector().playerFleet

        // Pay fuel cost (or show error if player lacks fuel)
        val cargo = playerFleet.cargo
        val fuelCostOfJump = Common.jumpCostInFuel(
            Misc.getDistanceLY(
                playerFleet.locationInHyperspace,
                newSystem.location
            )
        )

        if (cargo.fuel >= fuelCostOfJump) {
            cargo.removeFuel(fuelCostOfJump.toFloat())
        } else {
            textPanel.addParagraph(Strings.notEnoughFuel(fuelCostOfJump))
            ShowGateDestinationOptions().execute(null, dialog, params, memoryMap)
            return false
        }

        // Jump player fleet to new system
        val gates = newSystem.getEntitiesWithTag(Tags.TAG_GATE_ACTIVATED)
        Jump.jumpPlayer(gates.first())

        textPanel.addParagraph(Strings.flyThroughActiveGate)

        dialog.dismiss()

        return true
    }
}
