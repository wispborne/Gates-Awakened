@file:Suppress("unused")

package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.campaign.CampaignPlanet
import com.fs.starfarer.combat.CombatViewport
import com.fs.starfarer.combat.entities.terrain.Planet
import org.wisp.gatesawakened.di

class GateRingInner : BaseCustomEntityPlugin() {
    private lateinit var starSystemId: String

    @Transient
    private lateinit var sprite: SpriteAPI

    @Transient
    private lateinit var starSystemAPI: StarSystemAPI
    @Transient
    private lateinit var star: Planet

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

        if (pluginParams !is StarSystemAPI) {
            di.errorReporter.reportCrash(ClassCastException("GateRingInner's pluginParams must be a StarSystemAPI"))
            return
        }

        starSystemId = pluginParams.id

        readResolve()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun readResolve(): Any {
        sprite = di.settings.getSprite("GatesAwakenedFx", "gate_circle_inner")
        starSystemAPI = (di.sector.getStarSystem(starSystemId) as StarSystemAPI)
        star = (starSystemAPI.star as CampaignPlanet).graphics.clone()
            .run { Planet(this.spec, this.radius, this.gravity, entity.location) }
//        sprite = di.settings.getSprite(spriteName)

        return this
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        val alphaMult = viewport.alphaMult
        val loc = entity.location
        sprite.alphaMult = alphaMult
        sprite.setNormalBlend()
        sprite.color = starSystemAPI.lightColor
        sprite.angle += .5f
        sprite.renderAtCenter(loc.x, loc.y)

        viewport as CombatViewport
        star.setLoc(loc)

//        if (layer == CampaignEngineLayers.PLANETS) {
        star.renderSphere(viewport)
//        } else if (layer == CampaignEngineLayers.ABOVE) {
//        star.renderStarGlow(viewport)
//        }
    }
}