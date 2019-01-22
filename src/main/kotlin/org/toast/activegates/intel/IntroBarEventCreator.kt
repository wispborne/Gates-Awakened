package org.toast.activegates.intel

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator

class IntroBarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = IntroBarEvent()

    override fun getBarEventTimeoutDuration(): Float = Float.MAX_VALUE
}