package org.toast.activegates.midgame

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import org.toast.activegates.Common
import org.toast.activegates.appendPara
import org.toast.activegates.constants.Memory
import org.toast.activegates.di

class MidgameQuestFinishedDialog : InteractionDialogPlugin {
    private lateinit var dialog: InteractionDialogAPI

    override fun init(dialog: InteractionDialogAPI) {
        this.dialog = dialog

        dialog.visualPanel.showImagePortion("illustrations", "survey", 640f, 400f, 0f, 0f, 480f, 300f)

        dialog.setOptionOnEscape(
            Option.LEAVE.text,
            Option.LEAVE
        )
        optionSelected(null as String?, Option.INIT)
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        dialog.optionPanel.clearOptions()
        val text = dialog.textPanel

        when (optionData as? Option) {
            null -> return
            Option.INIT -> {
                text.appendPara(
                    "Squarely located at the coordinates from the %s is a small island." +
                            " Visible from the ground is an awe-inspiring cave entrance." +
                            " Natural basalt pillars line the walls, their hexagonal edges overgrown with luminescent moss." +
                            " The only sign that your crew is not the first to step foot here is the %s" +
                            " almost casually placed on top of a pillar deep in the cave.",
                    "decrypted transmission",
                    "Universal Access Chip"
                )
                text.appendPara(
                    """It contains detailed, but slightly foreboding, instructions on how to 
                        |reactivate "carefully considered %s", "should my mission be successful".""".trimMargin(),
                    "Gates"
                )

                text.appendPara(
                    "It appears that %s",
                    "any active Gate may be accessed from any other."
                )

                text.appendPara(
                    """At the very end are %s and the writer's signature: "Ludd".""",
                    "two activation codes"
                )

                Common.remainingActivationCodes = 2

                (di.sector.intelManager.getFirstIntel(MidgameIntel::class.java) as MidgameIntel)
                    .endImmediately()

                di.sector.memoryWithoutUpdate[Memory.MID_QUEST_DONE] = true

                dialog.optionPanel.addOption(
                    Option.LEAVE.text,
                    Option.LEAVE
                )
            }

            Option.LEAVE -> {
                di.sector.isPaused = false
                dialog.dismiss()
            }
        }
    }

    override fun optionMousedOver(optionText: String?, optionData: Any?) {
    }

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {
    }

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null

    override fun advance(amount: Float) {
    }

    override fun getContext(): Any? = null

    enum class Option(val text: String) {
        INIT(""),
        LEAVE("Take the data and leave")
    }
}