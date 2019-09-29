package org.wisp.gatesawakened.constants

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI

object Strings {

    // Used for debug mode /////////
    fun debugGateAndDistance(systemName: String, distanceFromPlayer: Float): String =
        "$systemName at $distanceFromPlayer LY"


    const val debugAllGates = "All gates:"
    const val debugActiveGates = "All activated gates:"
    ///////////////////////////////
    const val modName = "gates-awakened"

    const val activeGateName = "Active Gate"
    const val inactiveGateName = "Inactive Gate"

    fun errorCouldNotFindJumpSystem(systemIdChosenByPlayer: String): String =
        "Could not find $systemIdChosenByPlayer; aborting"

    fun boyOrGirl(person: PersonAPI): String =
        if (person.gender == FullName.Gender.FEMALE) "girl" else "boy"
}