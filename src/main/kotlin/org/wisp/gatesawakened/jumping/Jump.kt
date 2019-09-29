package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.Gate
import org.wisp.gatesawakened.di

internal object Jump {
    /**
     * @return whether jump was successful
     */
    fun jumpPlayer(sourceLocation: SectorEntityToken?, destinationGate: Gate, isFuelRequired: Boolean = true): JumpResult {
        val playerFleet = di.sector.playerFleet

        // Pay fuel cost (or show error if player lacks fuel)
        val cargo = playerFleet.cargo
        val fuelCostOfJump = Common.jumpCostInFuel(
            Misc.getDistanceLY(
                playerFleet.locationInHyperspace,
                destinationGate.locationInHyperspace
            )
        )

        if (isFuelRequired) {
            if (cargo.fuel >= fuelCostOfJump) {
                cargo.removeFuel(fuelCostOfJump.toFloat())
            } else {
                return JumpResult.FuelRequired(fuelCostOfJump.toString())
            }
        }

        // Jump player fleet to new system
//        jumpPlayerToGate(destinationGate)
        di.sector.doHyperspaceTransition(di.sector.playerFleet, sourceLocation, JumpPointAPI.JumpDestination(destinationGate, null))
        return JumpResult.Success
    }


    private fun jumpPlayerToGate(gate: Gate) {
        val playerFleet = di.sector.playerFleet
        val newSystem = gate.starSystem

        // Usable in the future?
        // Global.getSector().doHyperspaceTransition(playerFleet, jumpPoint, dest);

        // Jump player fleet to new system
        playerFleet.containingLocation.removeEntity(playerFleet)
        newSystem.addEntity(playerFleet)
        di.sector.currentLocation = newSystem

        // Move player fleet to the new gate's location
        playerFleet.setLocation(gate.location.x, gate.location.y)

        // Ensure that the player fleet's only action post-jump is to hang out around the gate
        playerFleet.clearAssignments()
        playerFleet.setMoveDestination(playerFleet.location.x, playerFleet.location.y)
    }

    sealed class JumpResult {
        object Success : JumpResult()
        data class FuelRequired(val fuelCost: String) : JumpResult()
    }
}