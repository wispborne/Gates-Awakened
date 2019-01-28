package org.toast.activegates.intro

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.toast.activegates.Di
import org.toast.activegates.constants.Memory
import org.toast.activegates.equalsAny

class IntroBarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = IntroBarEvent()

    override fun getBarEventTimeoutDuration(): Float = Float.MAX_VALUE

    override fun getBarEventFrequencyWeight(): Float {
        return if (true.equalsAny(
                Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_IN_PROGRESS] as? Boolean,
                Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_DONE] as? Boolean
            )
        ) {
            0f
        } else {
            super.getBarEventFrequencyWeight()
        }
    }
}