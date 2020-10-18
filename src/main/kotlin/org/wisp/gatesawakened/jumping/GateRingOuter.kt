@file:Suppress("unused")

package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.wisp.gatesawakened.di
import java.awt.Color

class GateRingOuter : BaseCustomEntityPlugin() {
    companion object {
        const val INTENSITY_MULT = 1f
    }

    @Transient
    var ringSprite: SpriteAPI? = null

    @Transient
    var lightningSprite: SpriteAPI? = null

    @Transient
    lateinit var spriteBatch: SpriteBatch

    @Transient
    private var jumpAnimation: JumpAnimation? = null

    @Transient
    private val lightnings = List(size = MathUtils.getRandomNumberInRange(8, 12)) {
        Lightning(
            angle = MathUtils.getRandomNumberInRange(0f, 360f),
            velocity = Vector2f(
                MathUtils.getRandomNumberInRange(-15f, 15f) * INTENSITY_MULT,
                MathUtils.getRandomNumberInRange(-15f, 15f) * INTENSITY_MULT
            ),
            angularVelocity = MathUtils.getRandomNumberInRange(-6f, 6f) * INTENSITY_MULT,
            size = MathUtils.getRandomNumberInRange(10.4f, 30.7f) * INTENSITY_MULT,
            lifetime = MathUtils.getRandomNumberInRange(0.8f, 1.4f)
        )
    }

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

        // Circular dependency bad, but if save/reload during animation we'll just lose the animation
        // so no need to overengineer this by making this into an EveryFrameScript
        if (pluginParams !is JumpAnimation) {
            throw ClassCastException("GateRingOuter's pluginParams must be a JumpAnimation")
        }

        jumpAnimation = pluginParams
        jumpAnimation?.gateRingOuter = this

        lightnings.forEach {
            it.location = this.entity.location
                .translate(
                    MathUtils.getRandomNumberInRange(-50f, 50f),
                    MathUtils.getRandomNumberInRange(-50f, 50f)
                )
        }

        readResolve()
        spriteBatch = SpriteBatch(lightningSprite!!)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun readResolve(): Any {
        ringSprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_outer")
        lightningSprite = di.settings.getSprite("GatesAwakenedFx", "zappy_trail")
        return this
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        val loc = entity.location
        ringSprite!!.renderAtCenter(loc.x, loc.y)
        renderLightning()
    }

    private fun renderLightning() {
        spriteBatch.clear()

        for (lightning in lightnings) {
//            sprite.angle = lightning.angle
//            sprite.setSize(lightning.size, lightning.size)g

            spriteBatch.add(
                x = lightning.location.x,
                y = lightning.location.y,
                angle = lightning.angle,
                size = lightning.size,
                color = Color.white,
                alphaMod = 1f
            )
//            sprite.renderAtCenter(lightning.location.x, lightning.location.x)

            lightning.angle += lightning.angularVelocity
            lightning.location = lightning.location.translate(lightning.velocity.x, lightning.velocity.x)
        }

        spriteBatch.finish()
        spriteBatch.draw()
    }

    data class Lightning(
        var angle: Float,
        val velocity: Vector2f,
        val angularVelocity: Float,
        val size: Float,
        var lifetime: Float
    ) {
        lateinit var location: Vector2f
    }
}