package org.wisp.gatesawakened.jumping

class JumpAnimation(
) {
    companion object {
        private val DURATION = 5000f
        private val MAX_SPIN_SPEED = .7f
    }

    var gateRingInner: GateRingInner? = null
    var gateRingOuter: GateRingOuter? = null

    private var timeSinceStart: Float = 0f
    private var currentSpinSpeed = 0.001f

    fun advance(amount: Float) {
        timeSinceStart += (amount * 100)

        update()
    }

    private fun update() {
        val innerSprite = gateRingInner?.sprite ?: return
        val outerSprite = gateRingOuter?.sprite ?: return

        innerSprite.alphaMult = 1f
        outerSprite.alphaMult = 0f
//        innerSprite.setNormalBlend()
//        outerSprite.setAdditiveBlend()

        if (timeSinceStart >= 65f) {
            outerSprite.alphaMult = 1f

            innerSprite.angle += currentSpinSpeed
            outerSprite.angle -= currentSpinSpeed

            // todo lerp this
            if (currentSpinSpeed < MAX_SPIN_SPEED) {
                currentSpinSpeed += currentSpinSpeed
            }
        }
    }
}