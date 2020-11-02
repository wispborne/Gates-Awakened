package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.SoundAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.FlickerUtilV2
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
        private const val SHOW_OUTER_RING_FADE_IN_START_TIMESTAMP = SHOW_INNER_RING_TIMESTAMP
        private const val SHOW_OUTER_RING_FADE_IN_END_TIMESTAMP = SHOW_INNER_RING_TIMESTAMP + 450f
        private const val START_SPIN_TIMESTAMP = SHOW_OUTER_RING_FADE_IN_END_TIMESTAMP + 450f
        private const val WARP_TIMESTAMP = 4200f

        const val INTENSITY_MULT = 1f

        val color: Color = Color(10, 10, 10)
    }

    var outerRingSprite: SpriteAPI? = null
    var innerRingSprite: SpriteAPI? = null
    var smokeSprite: SpriteAPI? = null
    var lightningSprite: SpriteAPI? = null
    var gateLightSprite: SpriteAPI? = null

    var smokeSpriteBatch: SpriteBatch
    var lightningSpriteBatch: SpriteBatch

    val particles = List(size = 2000) {
        Particle()
    }

    val lightnings = mutableListOf<Lightning>()

    var millisSinceStart: Float = 0f
    var currentSpinSpeed = 0f
    var ringAppearSound: SoundAPI? = null
    var wasWarpSoundTriggered = false

    init {
        innerRingSprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_inner")
            .apply { alphaMult = 0f }
        outerRingSprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_outer")
            .apply { alphaMult = 0f }
        smokeSprite = di.settings.getSprite("GatesAwakenedFx", "smoke")
        lightningSprite = di.settings.getSprite("GatesAwakenedFx", "lightning")
        gateLightSprite = di.settings.getSprite("GatesAwakenedFx", "gatelight")
        smokeSpriteBatch = SpriteBatch(smokeSprite!!)
        lightningSpriteBatch = SpriteBatch(lightningSprite!!)
    }

    fun advance(amountInSeconds: Float) {
        millisSinceStart += (amountInSeconds * 1000)
        update()
    }

    fun render(location: Vector2f) {
        prepareToRenderParticles(location)
        prepareToRenderLightning(location)
        SpriteBatch.drawAll(
            smokeSpriteBatch,
            lightningSpriteBatch
        )
        innerRingSprite!!.renderAtCenter(location.x, location.y)
        outerRingSprite!!.renderAtCenter(location.x, location.y)
        gateLightSprite!!.renderAtCenter(location.x, location.y)
    }

    private fun update() {
        val playerFleet = di.sector.playerFleet

        // Don't do anything until both [GateRingInner] and [GateRingOuter] have set their properties
        val innerSprite = innerRingSprite ?: return
        val outerSprite = outerRingSprite ?: return
        val lightSprite = gateLightSprite ?: return

        updateInnerGlowy(lightSprite)
        updateGateSpinupSound(playerFleet)
        updateInnerRingAppearance(innerSprite, playerFleet)
        updateOuterRingAppearance(outerSprite)
        updateRingSpin(innerSprite, outerSprite)

        if (!wasWarpSoundTriggered && millisSinceStart >= WARP_TIMESTAMP) {
            di.soundPlayer.playUISound("GatesAwakened_gate_warp", 1f, 1f)
            wasWarpSoundTriggered = true
        }

        updateParticles()
        updateLightning()
    }

    private fun updateInnerGlowy(lightSprite: SpriteAPI) {
        // Central glowy light
        lightSprite.alphaMult = Easing.Cubic.easeIn(
            time = millisSinceStart,
            valueAtStart = 0f,
            valueAtEnd = 0.7f,
            duration = WARP_TIMESTAMP
        )

        lightSprite.angle += 0.3f
        lightSprite.setSize(160f, 160f)
    }

    private fun updateGateSpinupSound(playerFleet: CampaignFleetAPI) {
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
    }

    private fun updateInnerRingAppearance(
        innerSprite: SpriteAPI,
        playerFleet: CampaignFleetAPI
    ) {
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
    }

    private fun updateOuterRingAppearance(outerSprite: SpriteAPI) {
        if (millisSinceStart in SHOW_OUTER_RING_FADE_IN_START_TIMESTAMP..SHOW_OUTER_RING_FADE_IN_END_TIMESTAMP) {
            outerSprite.alphaMult = Easing.Quadratic.easeIn(
                time = millisSinceStart - SHOW_OUTER_RING_FADE_IN_START_TIMESTAMP,
                valueAtStart = 0f,
                valueAtEnd = 1f,
                duration = SHOW_OUTER_RING_FADE_IN_END_TIMESTAMP - SHOW_OUTER_RING_FADE_IN_START_TIMESTAMP
            )
        }
    }

    private fun updateRingSpin(
        innerSprite: SpriteAPI,
        outerSprite: SpriteAPI
    ) {
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

    private fun updateLightning() {
        if (!di.sector.isPaused) {
            if (lightnings.count() < 1 && millisSinceStart >= SHOW_INNER_RING_TIMESTAMP) {
                lightnings += Lightning(scatterMultiplier = 0.6f)
            }

            if (lightnings.count() < 2 && millisSinceStart >= SHOW_OUTER_RING_FADE_IN_START_TIMESTAMP) {
                lightnings += Lightning(scatterMultiplier = 0.8f)
            }

            while (lightnings.count() < 5 && millisSinceStart >= START_SPIN_TIMESTAMP) {
                lightnings += Lightning(scatterMultiplier = 1.5f)
            }

            lightnings.forEach {
                // This is what is done in [HyperspaceTerrainPlugin]
                it.flicker.advance(di.sector.clock.convertToDays(millisSinceStart / 1000f) / 4f)
            }
        }
    }

    private fun updateParticles() {
        if (!di.sector.isPaused) {
            if (millisSinceStart > SHOW_INNER_RING_TIMESTAMP) {
                val timeSinceParticleAnimationStarted = millisSinceStart - SHOW_INNER_RING_TIMESTAMP

                particles.forEach { particle ->
                    particle.alpha = Easing.Linear.tween(
                        time = timeSinceParticleAnimationStarted,
                        valueAtStart = 0f,
                        valueAtEnd = 1f,
                        duration = WARP_TIMESTAMP
                    )
                    // Set up next render
                    particle.orientationAngle
                    particle.orbitalSpeed = Easing.Linear.tween(
                        time = timeSinceParticleAnimationStarted,
                        valueAtStart = particle.initialOrbitalSpeed,
                        valueAtEnd = particle.finalOrbitalSpeed,
                        duration = WARP_TIMESTAMP
                    )
                    particle.angleOnCircle = particle.angleOnCircle.plus(particle.orbitalSpeed)

                    // Collapse in on center
                    particle.distanceFromCenter = Easing.Quadratic.easeIn(
                        time = timeSinceParticleAnimationStarted,
                        valueAtStart = particle.initialDistanceFromCenter,
                        valueAtEnd = particle.finalDistanceFromCenter,
                        duration = WARP_TIMESTAMP - SHOW_INNER_RING_TIMESTAMP
                    )
                }
            }
        }
    }

    private fun prepareToRenderParticles(location: Vector2f) {
        smokeSpriteBatch.clear()

        for (particle in particles) {
            smokeSpriteBatch.add(
                x = particle.locationRelativeTo(location).x,
                y = particle.locationRelativeTo(location).y,
                angle = particle.orientationAngle,
                size = particle.size,
                color = color,
                alphaMod = particle.alpha
            )
        }

        smokeSpriteBatch.finish()
    }

    private fun prepareToRenderLightning(location: Vector2f) {
        lightningSpriteBatch.clear()

        for (lightning in lightnings) {
            lightningSpriteBatch.add(
                x = lightning.locationRelativeTo(location).x,
                y = lightning.locationRelativeTo(location).y,
                angle = lightning.flicker.angle,
                size = lightning.size,
                color = Color.WHITE,
                alphaMod = lightning.flicker.brightness
            )
        }

        lightningSpriteBatch.finish()
    }

    data class Particle(
        var orientationAngle: Float = MathUtils.getRandomNumberInRange(0f, 360f),
        val initialOrbitalSpeed: Float = MathUtils.getRandomNumberInRange(0.001f, 0.005f) * INTENSITY_MULT,
        val finalOrbitalSpeed: Float = MathUtils.getRandomNumberInRange(0.007f, 0.09f) * INTENSITY_MULT,
        val size: Float = MathUtils.getRandomNumberInRange(10f, 25f) * INTENSITY_MULT,
        val initialDistanceFromCenter: Float = MathUtils.getRandomNumberInRange(0f, 40f),
        val finalDistanceFromCenter: Float = MathUtils.getRandomNumberInRange(initialDistanceFromCenter, 100f),
        var angleOnCircle: Float = Random.nextDouble(0.0, 360.0).toFloat(),
        var alpha: Float = 0f
    ) {
        var distanceFromCenter: Float = initialDistanceFromCenter
        var orbitalSpeed: Float = initialOrbitalSpeed

        fun locationRelativeTo(center: Vector2f): Vector2f =
            center + Vector2f(
                cos((angleOnCircle * Math.PI * 2).toFloat()) * distanceFromCenter.coerceAtLeast(minimumValue = 0f),
                sin(angleOnCircle * Math.PI * 2).toFloat() * distanceFromCenter.coerceAtLeast(minimumValue = 0f)
            )
    }

    data class Lightning(
        val scatterMultiplier: Float = 1f,
        val flicker: FlickerUtilV2 = FlickerUtilV2().apply { newBurst() },
        var orientationAngle: Float = MathUtils.getRandomNumberInRange(0f, 360f),
        var relativeLocation: Vector2f = Vector2f(
            MathUtils.getRandomNumberInRange(-100f * scatterMultiplier, 100f * scatterMultiplier),
            MathUtils.getRandomNumberInRange(-100f * scatterMultiplier, 100f * scatterMultiplier)
        ),
        val size: Float = 110f
    ) {
        fun locationRelativeTo(center: Vector2f): Vector2f =
            center + relativeLocation
    }
}