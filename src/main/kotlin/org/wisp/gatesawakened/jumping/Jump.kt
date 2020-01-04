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
    fun jumpPlayer(
        sourceLocation: SectorEntityToken?,
        destinationGate: Gate,
        isFuelRequired: Boolean = true
    ): JumpResult {
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
        di.sector.doHyperspaceTransition(
            di.sector.playerFleet,
            sourceLocation,
            JumpPointAPI.JumpDestination(destinationGate, null)
        )
        return JumpResult.Success
    }

    sealed class JumpResult {
        object Success : JumpResult()
        data class FuelRequired(val fuelCost: String) : JumpResult()
    }
}