package org.wisp.gatesawakened.activeGateIntel

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.Gate
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.isActive


class ActiveGateIntel(val activeGate: Gate) : BreadcrumbIntel(null, activeGate) {

    companion object {
        private val iconSpritePath: String by lazy(LazyThreadSafetyMode.NONE) {
            val path = "graphics/icons/icon_portal.png"
            di.settings.loadTexture(path)
            path
        }
    }

    override fun getName(): String = getTitle()

    override fun getTitle(): String = "Active Gate"

    override fun getIcon(): String = iconSpritePath

    override fun hasSmallDescription() = true

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        info.addPara(
            "There is an active Gate here.",
            10f
        )
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)
        info.addPara("There is an active Gate in %s", 0f, Misc.getHighlightColor(), activeGate.starSystem.baseName)
    }

    override fun getSortTier(): IntelInfoPlugin.IntelSortTier = IntelInfoPlugin.IntelSortTier.TIER_1

    override fun isEnded(): Boolean = !activeGate.isActive

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply {
                add(Tags.INTEL_ACTIVE_GATE)
            }
}