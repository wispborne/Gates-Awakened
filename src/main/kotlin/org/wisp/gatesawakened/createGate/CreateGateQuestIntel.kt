package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.FleetLogIntel
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.wispLib.addPara


class CreateGateQuestIntel : IntelDefinition(
    title = "The Reach",
    iconPath = "graphics/intel/g8_gate.png",
    infoCreator = { info: TooltipMakerAPI? ->
        info?.addPara(padding = 0f, textColor = Misc.getHighlightColor()) {
            "Gate Hauler Detected"
        }
    },
    smallDescriptionCreator = { info: TooltipMakerAPI?, width: Float, _ ->
        info?.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        info?.addPara {
            "A Gate Hauler is standing by, waiting for you to choose a location for a new Gate."
        }
        info?.addButton("Place Gate here", BUTTON_CHOOSE, width, 20f, 10f)
    },
    intelTags = listOf(Tags.INTEL_EXPLORATION)
) {
    companion object {
        private const val BUTTON_CHOOSE = MOD_PREFIX + "choose"
    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean = true

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        super.buttonPressConfirmed(buttonId, ui)
        CreateGateQuest.placeGateAtPlayerLocation()
    }

    override fun isDone(): Boolean = CreateGateQuest.wasQuestCompleted
    override fun isEnded(): Boolean = CreateGateQuest.wasQuestCompleted
}

class CreateGateQuestIntelOld() : FleetLogIntel() {

    companion object {
        private val iconSpritePath: String by lazy(LazyThreadSafetyMode.NONE) {
            val path = "graphics/intel/g8_gate.png"
            di.settings.loadTexture(path)
            path
        }

        private const val BUTTON_CHOOSE = MOD_PREFIX + "choose"
    }

//    override fun getName(): String = getTitle()
//
//    override fun getTitle(): String = "Gate Hauler Detected"

    override fun getIcon(): String = iconSpritePath

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)
        info.addPara(padding = 0f, textColor = Misc.getHighlightColor()) {
            "Gate Hauler Detected"
        }
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        info.addPara {
            "A Gate Hauler is standing by, waiting for you to choose a location for a new Gate."
        }
        info.addButton("Place Gate here", BUTTON_CHOOSE, width, 20f, 10f)
    }

    override fun hasSmallDescription(): Boolean = true

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean = true

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        super.buttonPressConfirmed(buttonId, ui)
        CreateGateQuest.placeGateAtPlayerLocation()
    }

    override fun isDone(): Boolean = CreateGateQuest.wasQuestCompleted
    override fun isEnded(): Boolean = CreateGateQuest.wasQuestCompleted

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> =
        super.getIntelTags(map)
            .apply {
                add(Tags.INTEL_EXPLORATION)
                add(Tags.INTEL_STORY)
            }
}