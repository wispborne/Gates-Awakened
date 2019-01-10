package org.toast.activegates

import com.fs.starfarer.api.Global
import kotlin.math.roundToInt

object ActiveGatesStrings {
    // Used for debug mode /////////
    fun debugGateAndDistance(systemName: String, distanceFromPlayer: Float): String =
            "$systemName at $distanceFromPlayer LY"


    const val debugAllGates: String = "All gates:"
    const val debugActiveGates: String = "All activated gates:"
    ///////////////////////////////

    val activationExplanation: String
        get() = "Your engineers estimate with ${ActiveGatesStrings.activationCost}, they can reactivate the gate."

    const val flyThroughInactiveGate: String = "Your fleet passes through the inactive gate..."
    const val flyThroughActiveGate: String = "Your fleet passes through the gate..."
    const val resultWhenGateDoesNotWork: String = "and nothing happens."
    const val menuOptionReconsider: String = "Reconsider"
    const val paidActivationCost: String = "Your crew offload the resources and, almost reverently, a single gamma core. " +
            "They get to work and, in short order, the gate is active."
    const val insufficientResourcesToActivateGate: String = "You don't have the resources required to activate the gate."
    const val gateAlreadyActive: String = "The gate is already active."

    fun notEnoughFuel(fuelCostOfJump: Int): String =
            "Unfortunately, your fleet lacks the $fuelCostOfJump fuel necessary to use the gate."

    private val activationCost: String
        get() {
            val cargo = Global.getSector().playerFleet.cargo
            return ("${ActiveGates.getCommodityCostOf("metals").roundToInt()} (${cargo.getCommodityQuantity("metals").roundToInt()}) metals, " +
                    "${ActiveGates.getCommodityCostOf("heavy_machinery").roundToInt()} (${cargo.getCommodityQuantity("heavy_machinery").roundToInt()}) heavy machinery, " +
                    "and some kind of basic processing core")
        }

    fun errorCouldNotFindJumpSystem(systemIdChosenByPlayer: String): String = "Could not find $systemIdChosenByPlayer; aborting"
    fun debugJumpOptionsAndDistances(optionNumber: String, distanceFromPlayer: Float, systemName: String): String =
            "$optionNumber, $distanceFromPlayer, $systemName"

    fun menuOptionJumpToSystem(systemName: String, jumpCostInFuel: Int): String =
            "Jump to $systemName ($jumpCostInFuel fuel)"
}