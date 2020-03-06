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

//        di.soundPlayer.playUISound(
//            "GatesAwakened_jump",
//            1f,
//            1.5f
//        )

//        if(sourceGate != null) {
//            renderEffects(sourceGate)
//        }

        // Jump player fleet to new system
        di.sector.doHyperspaceTransition(
            di.sector.playerFleet,
            if (flyToGateBeforeJumping) sourceGate else null,
            JumpPointAPI.JumpDestination(destinationGate, null)
        )
        return JumpResult.Success
    }

    fun renderEffects(anchor: SectorEntityToken) {
//        di.sector.addScript(MagicCampaignTrailPlugin())

//        val jumpFxId = MagicCampaignTrailPlugin.getUniqueID()
//        MagicCampaignTrailPlugin.AddTrailMemberSimple(
//            di.sector.playerFleet,
//            jumpFxId,
//            di.settings.getSprite("GatesAwakenedFx", "emp_arcs"),
//            di.sector.playerFleet.location,
//            0f,
//            Random.nextFloat(),
//            5f,
//            2f,
//            Color.RED,
//            .5f,
//            5f,
//            true,
//            Vector2f(2f, 2f)
//        )
//
//        MagicCampaignTrailPlugin.AddTrailMemberSimple(
//            di.sector.playerFleet,
//            jumpFxId,
//            di.settings.getSprite("GatesAwakenedFx", "emp_arcs"),
//            di.sector.playerFleet.location,
//            0f,
//            Random.nextFloat(),
//            5f,
//            2f,
//            Color.GREEN,
//            .5f,
//            5f,
//            true,
//            Vector2f(4f, 4f)
//        )
//
//        MagicCampaignTrailPlugin.AddTrailMemberSimple(
//            di.sector.playerFleet,
//            jumpFxId,
//            di.settings.getSprite("GatesAwakenedFx", "emp_arcs"),
//            di.sector.playerFleet.location,
//            0f,
//            Random.nextFloat(),
//            5f,
//            2f,
//            Color.BLUE,
//            .5f,
//            5f,
//            true,
//            Vector2f(6f, 6f)
//        )
    }

    private fun createParticle() {

    }

    /**
     * Updates a particle position
     * @param particle The particle to update
     * @param elapsedTime Elapsed time in milliseconds
     */
//    fun updatePosition(particle: Particle, elapsedTime: Long) {
//        val speed: Vector3f = particle.getSpeed()
//        val delta = elapsedTime / 1000.0f
//        val dx = speed.x * delta
//        val dy = speed.y * delta
//        val dz = speed.z * delta
//        val pos: Vector3f = particle.getPosition()
//        particle.setPosition(pos.x + dx, pos.y + dy, pos.z + dz)
//    }

    sealed class JumpResult {
        object Success : JumpResult()
        data class FuelRequired(val fuelCost: String) : JumpResult()
    }
}