package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import kotlin.math.roundToInt

class FlyThroughGate : BaseCommandPlugin() {

    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?,
                         params: List<Misc.Token>,
                         memoryMap: Map<String, MemoryAPI>): Boolean {
        if (dialog == null) return false

        val textPanel = dialog.textPanel

        // can only fly through activated gates
        if (!dialog.interactionTarget.hasTag(GateCommandPlugin.ACTIVATED)) {
            textPanel.addParagraph("Your fleet passes through the inactive gate...")
            textPanel.addParagraph("and nothing happens.")
            ShowGateDestinationOptions().execute(null, dialog, emptyList(), memoryMap)
            return false
        }

        val systemIdChosenByPlayer = params[0].getStringWithTokenReplacement(ruleId, dialog, memoryMap)
        if (systemIdChosenByPlayer == null || systemIdChosenByPlayer.isEmpty()) return false

        val newSys = Global.getSector().getStarSystem(systemIdChosenByPlayer)

        if (newSys == null) {
            textPanel.addParagraph("Could not find $systemIdChosenByPlayer; aborting")
            ShowGateDestinationOptions().execute(null, dialog, params, memoryMap)
            return false
        }

        val playerFleet = Global.getSector().playerFleet

        val cargo = playerFleet.cargo
        val fuelcost = GateCommandPlugin.fuelCostPerLY * Misc.getDistanceLY(playerFleet.locationInHyperspace, newSys.location)
        if (cargo.fuel >= fuelcost) {
            cargo.removeFuel(fuelcost)
        } else {
            textPanel.addParagraph("Unfortunately, your fleet lacks the " + fuelcost.roundToInt() +
                    " fuel necessary to use the gate.")
            ShowGateDestinationOptions().execute(null, dialog, params, memoryMap)
            return false
        }

        val oldSys = playerFleet.containingLocation
        oldSys.removeEntity(playerFleet)
        newSys.addEntity(playerFleet)
        Global.getSector().currentLocation = newSys
        val gates = newSys.getEntitiesWithTag(Tags.GATE)
        val newVect = gates[0].location
        playerFleet.setLocation(newVect.x, newVect.y)
        playerFleet.clearAssignments()

        textPanel.addParagraph("Your fleet passes through the gate...")
        if (oldSys === newSys) {
            textPanel.addParagraph("and nothing happens.")
            ShowGateDestinationOptions().execute(null, dialog, params, memoryMap)
        } else {
            dialog.dismiss()
        }

        return true
    }
}
