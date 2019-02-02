package org.toast.activegates.jumping

import com.fs.starfarer.api.util.Misc
import org.toast.activegates.Common
import org.toast.activegates.Di
import org.toast.activegates.Gate

internal object Jump {
    /**
     * @return whether jump was successful
     */
    fun jumpPlayer(destinationGate: Gate, isFuelRequired: Boolean = true): JumpResult {
        val playerFleet = Di.inst.sector.playerFleet

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
                return Jump.JumpResult.FuelRequired
            }
        }

        // Jump player fleet to new system
        jumpPlayerToGate(destinationGate)
        return Jump.JumpResult.Success
    }


    private fun jumpPlayerToGate(gate: Gate) {
        val playerFleet = Di.inst.sector.playerFleet
        val newSystem = gate.starSystem

        // Usable in the future?
        // Global.getSector().doHyperspaceTransition(playerFleet, jumpPoint, dest);

        // Jump player fleet to new system
        playerFleet.containingLocation.removeEntity(playerFleet)
        newSystem.addEntity(playerFleet)
        Di.inst.sector.currentLocation = newSystem

        // Move player fleet to the new gate's location
        playerFleet.setLocation(gate.location.x, gate.location.y)

        // Ensure that the player fleet's only action post-jump is to hang out around the gate
        playerFleet.clearAssignments()
        playerFleet.setMoveDestination(playerFleet.location.x, playerFleet.location.y)
    }

    sealed class JumpResult {
        object Success : JumpResult()
        object FuelRequired : JumpResult()
    }
}