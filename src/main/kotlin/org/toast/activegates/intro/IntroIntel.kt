package org.toast.activegates.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import org.toast.activegates.Di
import org.toast.activegates.Memory


class IntroIntel(foundAt: SectorEntityToken, target: SectorEntityToken) : BreadcrumbIntel(foundAt, target) {

    init {
        setTitle("Gate investigation")
        setText(
            "You saw an image of a Gate and the name of a system on the tripad of someone dressed like an explorer." +
                    " Perhaps it's worth a visit to ${target.starSystem.baseName} to search for a Gate."
        )
//        setIcon("graphics/stations/gate.png")
    }

    override fun isEnded(): Boolean =
        Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_MISSION_DONE] as? Boolean == true

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply {
                add(Tags.INTEL_EXPLORATION)
            }
}