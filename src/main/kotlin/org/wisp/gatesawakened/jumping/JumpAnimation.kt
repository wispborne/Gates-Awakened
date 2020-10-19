package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.SoundAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.plus
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


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

        const val INTENSITY_MULT = 1f
    }

    var outerRingSprite: SpriteAPI? = null
    var innerRingSprite: SpriteAPI? = null
    var lightningSprite: SpriteAPI? = null

    var spriteBatch: SpriteBatch

    private val particles = List(size = 150) {
        Particle(
            orientationAngle = MathUtils.getRandomNumberInRange(0f, 360f),
            orbitalSpeed = 0.006f * INTENSITY_MULT,
            size = MathUtils.getRandomNumberInRange(10f, 25f) * INTENSITY_MULT,
            lifetime = MathUtils.getRandomNumberInRange(0.8f, 1.4f)
        )
    }

    var gateRingInner: GateRingInner? = null
    var gateRingOuter: GateRingOuter? = null

    private var millisSinceStart: Float = 0f
    private var currentSpinSpeed = 0f
    private var ringAppearSound: SoundAPI? = null
    private var wasWarpSoundTriggered = false

    init {
        innerRingSprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_inner")
        outerRingSprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_outer")
        lightningSprite = di.settings.getSprite("GatesAwakenedFx", "smoke")
        spriteBatch = SpriteBatch(lightningSprite!!)
    }

    fun advance(amountInSeconds: Float) {
        millisSinceStart += (amountInSeconds * 1000)
        update()
    }

    fun render(location: Vector2f) {
        innerRingSprite!!.renderAtCenter(location.x, location.y)
        outerRingSprite!!.renderAtCenter(location.x, location.y)
        renderParticles(location)
    }

    private fun update() {
        val playerFleet = di.sector.playerFleet

        // Don't do anything until both [GateRingInner] and [GateRingOuter] have set their properties
        val innerSprite = innerRingSprite ?: return
        val outerSprite = outerRingSprite ?: return

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


        if (!di.sector.isPaused) {
            for (particle in particles) {
                // Set up next render
                particle.orientationAngle
                particle.angleOnCircle = particle.angleOnCircle.plus(particle.orbitalSpeed)
                particle.distanceFromCenter = particle.distanceFromCenter.minus(0.3f).coerceAtLeast(0f)
            }
        }
    }

    private fun renderParticles(location: Vector2f) {
        spriteBatch.clear()

        for (particle in particles) {
            spriteBatch.add(
                x = particle.locationRelativeTo(location).x,
                y = particle.locationRelativeTo(location).y,
                angle = particle.orientationAngle,
                size = particle.size,
                color = Color.BLACK,
                alphaMod = 1f
            )
        }

        spriteBatch.finish()

        SpriteBatch.drawAll(spriteBatch)
    }

    data class Particle(
        var orientationAngle: Float,
        val orbitalSpeed: Float,
        val size: Float,
        var lifetime: Float,
        var distanceFromCenter: Float = 70f,
        var angleOnCircle: Float = Random.nextDouble(0.0, 360.0).toFloat()
    ) {
        fun locationRelativeTo(center: Vector2f): Vector2f =
            center + Vector2f(
                cos((angleOnCircle * Math.PI * 2).toFloat()) * distanceFromCenter,
                sin(angleOnCircle * Math.PI * 2).toFloat() * distanceFromCenter
            )
    }
}