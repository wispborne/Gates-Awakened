package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.SoundAPI
import org.wisp.gatesawakened.di

class JumpAnimation(
) {
    companion object {
        private const val DURATION = 3000f
        private const val MAX_SPIN_SPEED = 8f
        private const val SHOW_INNER_RING_TIMESTAMP = 800f
        private const val SHOW_OUTER_RING_FADEIN_START_TIMESTAMP = SHOW_INNER_RING_TIMESTAMP
        private const val SHOW_OUTER_RING_FADEIN_END_TIMESTAMP = SHOW_INNER_RING_TIMESTAMP + 450f
        private const val START_SPIN_TIMESTAMP = SHOW_OUTER_RING_FADEIN_END_TIMESTAMP + 450f
        private const val WARP_TIMESTAMP = 4200f

        private const val RING_APPEAR_SPEED = 2f
    }

    var gateRingInner: GateRingInner? = null
    var gateRingOuter: GateRingOuter? = null

    private var millisSinceStart: Float = 0f
    private var currentSpinSpeed = 0f
    private var ringAppearSound: SoundAPI? = null
    private var wasWarpSoundTriggered = false

    fun advance(amountInSeconds: Float) {
        millisSinceStart += (amountInSeconds * 1000)
        update()
    }

    private fun update() {
        val playerFleet = di.sector.playerFleet

        // Don't do anything until both [GateRingInner] and [GateRingOuter] have set their properties
        val innerSprite = gateRingInner?.sprite ?: return
        val outerSprite = gateRingOuter?.sprite ?: return

        innerSprite.alphaMult = 0f
        outerSprite.alphaMult = 0f

        if (millisSinceStart >= SHOW_INNER_RING_TIMESTAMP && millisSinceStart < WARP_TIMESTAMP) {
            di.soundPlayer.playLoop(
                "GatesAwakened_gate_spinup",
                playerFleet,
                Easing.Quadratic.easeIn(
                    time = millisSinceStart,
                    valueAtStart = 0f,
                    valueAtEnd = 2f,
                    duration = WARP_TIMESTAMP
                ),
                1f,
                playerFleet.location,
                playerFleet.velocity
            )
        }

        if (millisSinceStart >= SHOW_INNER_RING_TIMESTAMP) {
            innerSprite.alphaMult = 1f

            if (ringAppearSound == null && !di.sector.isPaused) {
                ringAppearSound = di.soundPlayer.playSound(
                    "GatesAwakened_gate_ringAppear",
                    1f,
                    1f,
                    playerFleet.location,
                    playerFleet.velocity
                )
            } else if (di.sector.isPaused)
                ringAppearSound?.stop()
        }

        if (millisSinceStart >= SHOW_OUTER_RING_FADEIN_START_TIMESTAMP) {
            outerSprite.alphaMult = Easing.Quadratic.easeIn(
                time = millisSinceStart - SHOW_OUTER_RING_FADEIN_START_TIMESTAMP,
                valueAtStart = 0f,
                valueAtEnd = 1f,
                duration = SHOW_OUTER_RING_FADEIN_END_TIMESTAMP - SHOW_OUTER_RING_FADEIN_START_TIMESTAMP
            )

//            if (!wasOuterRingSoundTriggered) {
//                di.soundPlayer.playSound(
//                    "GatesAwakened_gate_ringAppear",
//                    1f,
//                    1f,
//                    playerFleet.location,
//                    playerFleet.velocity
//                )
//                wasOuterRingSoundTriggered = true
//            }
        }

        if (millisSinceStart >= START_SPIN_TIMESTAMP) {
            if (currentSpinSpeed < MAX_SPIN_SPEED) {
                currentSpinSpeed = Easing.Quadratic.easeIn(
                    time = millisSinceStart - START_SPIN_TIMESTAMP,
                    valueAtStart = 0f,
                    valueAtEnd = MAX_SPIN_SPEED,
                    duration = DURATION - START_SPIN_TIMESTAMP
                )
            }

            innerSprite.angle += currentSpinSpeed
            outerSprite.angle -= currentSpinSpeed
        }

        if (!wasWarpSoundTriggered && millisSinceStart >= WARP_TIMESTAMP) {
            di.soundPlayer.playUISound("GatesAwakened_gate_warp", 1f, 1f)
            wasWarpSoundTriggered = true
        }
    }
}