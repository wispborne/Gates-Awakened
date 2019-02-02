package org.toast.activegates.constants

import com.fs.starfarer.api.Global
import org.toast.activegates.Common
import kotlin.math.roundToInt

object Strings {

    // Used for debug mode /////////
    fun debugGateAndDistance(systemName: String, distanceFromPlayer: Float): String =
        "$systemName at $distanceFromPlayer LY"


    const val debugAllGates = "All gates:"
    const val debugActiveGates = "All activated gates:"
    ///////////////////////////////
    const val modName = "active-gates"

    const val activeGateName = "Active Gate"

    val activationExplanation
        get() = "Your engineers produce a list of materials that, if procured, they think they can use to activate the gate."

    const val flyThroughInactiveGate = "Your fleet passes through the inactive gate..."
    const val flyThroughActiveGate = "Your fleet passes through the gate..."
    const val resultWhenGateDoesNotWork = "and nothing happens."
    const val paidActivationCost = "Your crew offload the resources from your fleet. " +
            "They get to work and, in short order, the gate is active."
    const val insufficientResourcesToActivateGate =
        "You don't have the resources required to activate the gate."
    const val gateAlreadyActive = "The gate is already active."

    fun notEnoughFuel(fuelCostOfJump: Int) =
        "Unfortunately, your fleet lacks the $fuelCostOfJump fuel necessary to use the gate."

    val activationCost: String
        get() {
            val cargo = Global.getSector().playerFleet.cargo
            return Common.activationCost
                .filter { it.value > 0 }
                .map {
                    "• ${it.value.roundToInt()} ${Global.getSettings().getCommoditySpec(it.key).name}  (${cargo.getCommodityQuantity(
                        it.key
                    ).roundToInt()} in cargo)"
                }
                .joinToString(separator = "\n")
        }

    fun errorCouldNotFindJumpSystem(systemIdChosenByPlayer: String): String =
        "Could not find $systemIdChosenByPlayer; aborting"

    fun menuOptionJumpToSystem(systemName: String, jumpCostInFuel: Int): String =
        "Jump to $systemName ($jumpCostInFuel fuel)"
}