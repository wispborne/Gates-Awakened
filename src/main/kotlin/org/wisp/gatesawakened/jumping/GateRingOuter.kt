@file:Suppress("unused")

package org.wisp.gatesawakened.jumping

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin

class GateRingOuter : BaseCustomEntityPlugin() {

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
    }
}