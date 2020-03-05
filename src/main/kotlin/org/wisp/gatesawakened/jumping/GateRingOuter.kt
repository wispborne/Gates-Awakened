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
    private lateinit var sprite: SpriteAPI

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)
        readResolve()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun readResolve(): Any {
        sprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_outer")
        return this
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        val alphaMult = viewport.alphaMult
        val loc = entity.location
        sprite.alphaMult = alphaMult
        sprite.setAdditiveBlend()
        sprite.angle -= .5f
        sprite.renderAtCenter(loc.x, loc.y)
    }
}