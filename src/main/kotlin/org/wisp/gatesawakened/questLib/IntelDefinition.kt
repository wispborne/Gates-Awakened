package org.wisp.gatesawakened.questLib

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.wispLib.addPara

/**
 * @param iconPath get via [com.fs.starfarer.api.SettingsAPI.getSpriteName]
 */
open class IntelDefinition(
    val title: String? = null,
    var iconPath: String? = null,
    var durationInDays: Float = Float.NaN,
    val infoCreator: (IntelDefinition.(info: TooltipMakerAPI?) -> Unit)? = null,
    val smallDescriptionCreator: (IntelDefinition.(info: TooltipMakerAPI, width: Float, height: Float) -> Unit)? = null,
    val showDaysSinceCreated: Boolean = false,
    val intelTags: List<String>,
    startLocation: SectorEntityToken? = null,
    endLocation: SectorEntityToken? = null,
    var removeIntelIfAnyOfTheseEntitiesDie: List<SectorEntityToken> = emptyList(),
    var soundName: String? = null,
    important: Boolean = false
) : BaseIntelPlugin() {
    val padding = 3f
    val bulletPointPadding = 10f

    private val startLocationCopy: SectorEntityToken?
    private val endLocationCopy: SectorEntityToken?

    init {
        isImportant = important

        startLocationCopy = startLocation?.let { BreadcrumbIntel.makeDoubleWithSameOrbit(it) }
        endLocationCopy = endLocation?.let { BreadcrumbIntel.makeDoubleWithSameOrbit(it) }

        if (iconPath != null) {
            di.settings.loadTexture(iconPath)
        }
    }

    override fun shouldRemoveIntel(): Boolean {
        if (removeIntelIfAnyOfTheseEntitiesDie.any { !it.isAlive }
            || endLocationCopy?.isAlive == false) {
            return true
        }

        val intelStartedTimestamp = playerVisibleTimestamp

        // Remove intel if duration has elapsed
        if (durationInDays.isFinite()
            && intelStartedTimestamp != null
            && di.sector.clock.getElapsedDaysSince(intelStartedTimestamp) >= durationInDays
        ) {
            return true
        }

        return super.shouldRemoveIntel()
    }

    final override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        title?.let { info.addPara(textColor = getTitleColor(mode), padding = 0f) { title } }
        infoCreator?.invoke(this, info)
    }

    final override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        smallDescriptionCreator?.invoke(this, info, width, height)

        if (showDaysSinceCreated && daysSincePlayerVisible > 0) {
            addDays(info, "ago.", daysSincePlayerVisible, Misc.getTextColor(), bulletPointPadding)
        }
    }

    final override fun hasSmallDescription(): Boolean = smallDescriptionCreator != null

    override fun getIcon(): String = iconPath
        ?: di.settings.getSpriteName("intel", "fleet_log")
        ?: super.getIcon()

    override fun getCommMessageSound(): String {
        return soundName
            ?: getSoundLogUpdate()
            ?: super.getCommMessageSound()
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return super.getIntelTags(map)
            .apply { this += intelTags }
    }

    override fun getSortString(): String = "Location"

    override fun getSmallDescriptionTitle(): String? = title

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? =
        endLocationCopy?.starSystem?.center
            ?: endLocationCopy

    override fun getArrowData(map: SectorMapAPI?): MutableList<IntelInfoPlugin.ArrowData>? {
        if (startLocationCopy == null)
            return null

        // If start and end are same, no arrow
        if (startLocationCopy.containingLocation == endLocationCopy?.containingLocation
            && startLocationCopy.containingLocation?.isHyperspace != true
        ) {
            return null
        }

        return mutableListOf(
            IntelInfoPlugin.ArrowData(startLocationCopy, endLocationCopy)
                .apply {
                    color = factionForUIColors?.baseUIColor
                })
    }
}