package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import org.wisp.gatesawakened.Common
import kotlin.math.roundToInt

object Strings {

    // Used for debug mode /////////
    fun debugGateAndDistance(systemName: String, distanceFromPlayer: Float): String =
        "$systemName at $distanceFromPlayer LY"


    const val debugAllGates = "All gates:"
    const val debugActiveGates = "All activated gates:"
    ///////////////////////////////
    const val modName = "gates-awakened"

    const val activeGateName = "Active Gate"

    const val flyThroughInactiveGate = "Your fleet passes through the inactive gate..."
    const val flyThroughActiveGate = "Your fleet passes through the gate..."
    const val resultWhenGateDoesNotWork = "and nothing happens."

    fun notEnoughFuel(fuelCostOfJump: String) =
        "Unfortunately, your fleet lacks the $fuelCostOfJump fuel necessary to use the gate."

    fun errorCouldNotFindJumpSystem(systemIdChosenByPlayer: String): String =
        "Could not find $systemIdChosenByPlayer; aborting"

    fun boyOrGirl(person: PersonAPI): String =
        if (person.gender == FullName.Gender.FEMALE) "girl" else "boy"
}