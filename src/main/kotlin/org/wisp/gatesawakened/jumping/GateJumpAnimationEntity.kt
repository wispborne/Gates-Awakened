@file:Suppress("unused")

package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin

class GateJumpAnimationEntity : BaseCustomEntityPlugin() {
//    private lateinit var starSystemId: String

    @Transient
    private var jumpAnimation: JumpAnimation? = null

//    @Transient
//    private lateinit var star: Planet

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

        // Circular dependency bad, but if save/reload during animation we'll just lose the animation
        // so no need to overengineer this by making this into an EveryFrameScript
        if (pluginParams !is JumpAnimation) {
            throw ClassCastException("GateRingInner's pluginParams must be a JumpAnimation")
        }

        jumpAnimation = pluginParams
    }

    /**
     * Only call this from [GateJumpAnimationEntity], not [GateRingOuter], so that it isn't triggered twice per tick.
     */
    override fun advance(amount: Float) {
        super.advance(amount)
        jumpAnimation?.advance(amount)
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        jumpAnimation?.render(entity.location)
    }
}