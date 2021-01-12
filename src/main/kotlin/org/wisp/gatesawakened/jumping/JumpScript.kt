package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.gatesawakened.Gate
import org.wisp.gatesawakened.di

class JumpScript : EveryFrameScript {
    private var millisSinceStartOfScript = 0f
    private var startGate: Gate? = null
    private var destinationGate: Gate? = null
    private var flyToGateBeforeJumping: Boolean = false
    private var createJumpAnimationEntity: CustomCampaignEntityAPI? = null

    private var hasTriggeredJump = false
    private var isDone = false

    override fun runWhilePaused(): Boolean = false

    override fun isDone(): Boolean = isDone

    fun start(
        startGate: Gate?,
        destinationGate: Gate,
        flyToGateBeforeJumping: Boolean = false
    ) {
        this.startGate = startGate
        this.destinationGate = destinationGate
        this.flyToGateBeforeJumping = flyToGateBeforeJumping
    }

    override fun advance(amountInSeconds: Float) {
        if (isDone) return
        val start = startGate ?: return
        millisSinceStartOfScript += (amountInSeconds * 1000f)

        if (createJumpAnimationEntity == null) {
            createJumpAnimationEntity = createJumpAnimation(sourceGate = start)
        }

        // Wait until 3.2 seconds have passed
        if (millisSinceStartOfScript < 3200) {
            return
        }

        if (!hasTriggeredJump) {
            // Jump player fleet to new system
            di.sector.doHyperspaceTransition(
                di.sector.playerFleet,
                if (flyToGateBeforeJumping) startGate else null,
                JumpPointAPI.JumpDestination(destinationGate, null)
            )
            hasTriggeredJump = true
        }

        // Wait until 6 seconds have passed
        if (millisSinceStartOfScript < 6000) {
            return
        }

        // After player has jumped, remove the animation entity and this script
        if (createJumpAnimationEntity != null) {
            start.containingLocation.removeEntity(createJumpAnimationEntity)
        }

        isDone = true
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
                this.orbit = sourceGate.orbit?.makeCopy()
            }
    }
}