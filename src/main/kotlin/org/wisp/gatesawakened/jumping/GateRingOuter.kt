@file:Suppress("unused")

package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import org.wisp.gatesawakened.di

class GateRingOuter : BaseCustomEntityPlugin() {
    @Transient
    var sprite: SpriteAPI? = null

    @Transient
    private var jumpAnimation: JumpAnimation? = null

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

        // Circular dependency bad, but if save/reload during animation we'll just lose the animation
        // so no need to overengineer this by making this into an EveryFrameScript
        if (pluginParams !is JumpAnimation) {
            throw ClassCastException("GateRingOuter's pluginParams must be a JumpAnimation")
        }

        jumpAnimation = pluginParams
        jumpAnimation?.gateRingOuter = this

        readResolve()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun readResolve(): Any {
        sprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_outer")
        return this
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        val loc = entity.location
        sprite!!.renderAtCenter(loc.x, loc.y)
    }
}