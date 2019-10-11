package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.addPara


class IntroIntel(target: SectorEntityToken) : BreadcrumbIntel(null, target) {

    companion object {
        private val iconSpritePath: String by lazy(LazyThreadSafetyMode.NONE) {
            val path = "graphics/intel/g8_gate.png"
            di.settings.loadTexture(path)
            path
        }
    }

    override fun getName(): String = getTitle()

    override fun getTitle(): String = "Gate investigation"

    override fun getIcon(): String = iconSpritePath

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)

        info.addPara { "You saw an image of a Gate and the name of a system on a tripad in a bar." }
        info.addPara { "Perhaps it's worth a visit to " + mark(target.starSystem.baseName) + " to search for a Gate." }

        val days = daysSincePlayerVisible

        if (days >= 1) {
            addDays(info, "ago.", days, Misc.getTextColor(), 10f)
        }
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)
        info.addPara(padding = 0f) { "Investigate a possible gate at " + mark(target.starSystem.baseName) }
    }

    override fun hasSmallDescription() = true

    override fun isEnded(): Boolean = Intro.wasQuestCompleted
    override fun isDone(): Boolean = Intro.wasQuestCompleted

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply {
                add(Tags.INTEL_EXPLORATION)
                add(Tags.INTEL_STORY)
            }
}