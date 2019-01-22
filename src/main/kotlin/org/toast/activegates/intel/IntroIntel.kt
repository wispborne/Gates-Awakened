package org.toast.activegates.intel

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI

class IntroIntel(foundAt: SectorEntityToken, target: SectorEntityToken) : BreadcrumbIntel(foundAt, target) {

    init {
        setTitle("Hello!")
        setText("test")
    }

    override fun isEnded(): Boolean {
        return false
    }

    override fun isEnding(): Boolean {
        return false
    }

    override fun isSendingUpdate(): Boolean {
        return true
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply { add("test") }
}