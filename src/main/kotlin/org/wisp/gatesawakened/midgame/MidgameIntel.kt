package org.wisp.gatesawakened.midgame

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di


class MidgameIntel(foundAt: SectorEntityToken, target: SectorEntityToken) : BreadcrumbIntel(foundAt, target) {

    companion object {
        private val iconSpritePath: String by lazy(LazyThreadSafetyMode.NONE) {
            val path = "graphics/intel/g8_gate.png"
            di.settings.loadTexture(path)
            path
        }
    }

    override fun getName(): String = getTitle()

    override fun getTitle(): String = "Planet investigation"

    override fun getIcon(): String = iconSpritePath

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        info.addPara(
            "You saw a decoded transmission detailing Gate activation codes.",
            10f
        )
        info.addPara(
            "Perhaps it's worth a visit to %s.",
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
        info.addPara("Investigate a planet in %s", 0f, Misc.getHighlightColor(), target.starSystem.baseName)
    }

    override fun hasSmallDescription() = true

    override fun isEnded(): Boolean = Midgame.wasQuestCompleted

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply {
                add(Tags.INTEL_EXPLORATION)
            }
}