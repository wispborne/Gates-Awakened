package org.toast.activegates

import com.fs.starfarer.api.Global
import kotlin.math.roundToInt

object Strings {
    // Used for debug mode /////////
    fun debugGateAndDistance(systemName: String, distanceFromPlayer: Float): String =
            "$systemName at $distanceFromPlayer LY"


    const val debugAllGates: String = "All gates:"
    const val debugActiveGates: String = "All activated gates:"
    ///////////////////////////////
    const val modName = "active-gates"

    val activationExplanation: String
        get() = "Your engineers produce a list of materials that, if procured, they think they can use to activate the gate."

    const val flyThroughInactiveGate: String = "Your fleet passes through the inactive gate..."
    const val flyThroughActiveGate: String = "Your fleet passes through the gate..."
    const val resultWhenGateDoesNotWork: String = "and nothing happens."
    const val menuOptionReconsider: String = "Reconsider"
    const val paidActivationCost: String = "Your crew offload the resources from your fleet. " +
            "They get to work and, in short order, the gate is active."
    const val insufficientResourcesToActivateGate: String = "You don't have the resources required to activate the gate."
    const val gateAlreadyActive: String = "The gate is already active."

    fun notEnoughFuel(fuelCostOfJump: Int): String =
            "Unfortunately, your fleet lacks the $fuelCostOfJump fuel necessary to use the gate."

    val activationCost: String
        get() {
            val cargo = Global.getSector().playerFleet.cargo
            return ActiveGates.activationCost
                    .filter { it.value > 0 }
                    .map { "• ${it.value.roundToInt()} ${Global.getSettings().getCommoditySpec(it.key).name}  (${cargo.getCommodityQuantity(it.key).roundToInt()} in cargo)" }
                    .joinToString(separator = "\n")
        }

    fun errorCouldNotFindJumpSystem(systemIdChosenByPlayer: String): String = "Could not find $systemIdChosenByPlayer; aborting"
    fun debugJumpOptionsAndDistances(optionNumber: String, distanceFromPlayer: Float, systemName: String): String =
            "$optionNumber, $distanceFromPlayer, $systemName"

    fun menuOptionJumpToSystem(systemName: String, jumpCostInFuel: Int): String =
            "Jump to $systemName ($jumpCostInFuel fuel)"
}