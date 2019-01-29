package org.toast.activegates.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.Di
import org.toast.activegates.constants.Memory


class IntroIntel(foundAt: SectorEntityToken, target: SectorEntityToken) : BreadcrumbIntel(foundAt, target) {

    companion object {
        private val iconSpritePath: String by lazy(LazyThreadSafetyMode.NONE) {
            val path = "graphics/intel/g8_gate.png"
            Di.inst.settings.loadTexture(path)
            path
        }
    }

    override fun getName(): String = getTitle()

    override fun getTitle(): String = "Gate investigation"

    override fun getIcon(): String = iconSpritePath

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(Di.inst.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        info.addPara(
            "You saw an image of a Gate and the name of a system on the tripad of someone dressed like an explorer.",
            10f
        )
        info.addPara(
            "Perhaps it's worth a visit to %s to search for a Gate.",
            10f,
            Misc.getHighlightColor(),
            target.starSystem.baseName
        )

        val days = daysSincePlayerVisible

        if (days >= 1) {
            addDays(info, "ago.", days, Misc.getTextColor(), 10f)
        }
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)
        info.addPara("Investigate a possible gate at %s", 0f, Misc.getHighlightColor(), target.starSystem.baseName)
    }

    override fun hasSmallDescription() = true

    override fun isEnded(): Boolean =
        Di.inst.sector.memoryWithoutUpdate[Memory.INTRO_QUEST_DONE] as? Boolean == true

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply {
                add(Tags.INTEL_EXPLORATION)
            }
}