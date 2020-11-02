@file:Suppress("unused")

package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin

class GateJumpAnimationEntity : BaseCustomEntityPlugin() {
    @Transient
    private var jumpAnimation: JumpAnimation? = null

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

        if (pluginParams !is JumpAnimation) {
            throw ClassCastException("pluginParams must be a JumpAnimation")
        }

        jumpAnimation = pluginParams
    }

    override fun advance(amount: Float) {
        super.advance(amount)
        jumpAnimation?.advance(amount)
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        jumpAnimation?.render(entity.location)
    }
}