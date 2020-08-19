package org.wisp.gatesawakened.jumping

import org.wisp.gatesawakened.di

class JumpAnimation(
) {
    companion object {
        private const val DURATION = 3000f
        private const val MAX_SPIN_SPEED = 8f
        private const val SHOW_INNER_RING_TIMESTAMP = 800f
        private const val SHOW_OUTER_RING_TIMESTAMP = SHOW_INNER_RING_TIMESTAMP + 450f
        private const val START_SPIN_TIMESTAMP = SHOW_OUTER_RING_TIMESTAMP + 450f
    }

    var gateRingInner: GateRingInner? = null
    var gateRingOuter: GateRingOuter? = null

    private var millisSinceStart: Float = 0f
    private var currentSpinSpeed = 0f
    private var wasSoundTriggered = false


    fun advance(amountInSeconds: Float) {
        if (millisSinceStart >= SHOW_INNER_RING_TIMESTAMP && !wasSoundTriggered) {
            di.soundPlayer.playUISound("GatesAwakened_jump", 1f, 1f)
            wasSoundTriggered = true
        }

        millisSinceStart += (amountInSeconds * 1000)

        update()
    }

    private fun update() {
        val innerSprite = gateRingInner?.sprite ?: return
        val outerSprite = gateRingOuter?.sprite ?: return

        innerSprite.alphaMult = 0f
        outerSprite.alphaMult = 0f

        if (millisSinceStart >= SHOW_INNER_RING_TIMESTAMP) {
            innerSprite.alphaMult = 1f
        }

        if (millisSinceStart >= SHOW_OUTER_RING_TIMESTAMP) {
            outerSprite.alphaMult = 1f
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
    }
}