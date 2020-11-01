package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.Gate
import org.wisp.gatesawakened.di


internal object Jump {
    /**
     * @return whether jump was successful
     */
    fun jumpPlayer(
        sourceGate: SectorEntityToken?,
        destinationGate: Gate,
        flyToGateBeforeJumping: Boolean = false,
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

        if (sourceGate != null) {
            GlobalScope.launch {
                val animationEntity = createJumpAnimation(sourceGate)

                delay(timeMillis = 3200)

                // Jump player fleet to new system
                di.sector.doHyperspaceTransition(
                    di.sector.playerFleet,
                    if (flyToGateBeforeJumping) sourceGate else null,
                    JumpPointAPI.JumpDestination(destinationGate, null)
                )

                // After player has jumped, remove the animation entity
                delay(timeMillis = 6000)
                sourceGate.containingLocation.removeEntity(animationEntity)
            }
        }
        return JumpResult.Success
    }

    private fun createJumpAnimation(sourceGate: SectorEntityToken): CustomCampaignEntityAPI {
        val jumpAnimation = JumpAnimation()
        return sourceGate.containingLocation.addCustomEntity(
            null,
            "",
            "GatesAwakened_gateJumpAnimationEntity",
            null,
            jumpAnimation
        )
            .apply {
                this.setLocation(
                    sourceGate.location.x,
                    sourceGate.location.y
                )
                this.orbit = sourceGate.orbit.makeCopy()
            }
    }

    sealed class JumpResult {
        object Success : JumpResult()
        data class FuelRequired(val fuelCost: String) : JumpResult()
    }
}