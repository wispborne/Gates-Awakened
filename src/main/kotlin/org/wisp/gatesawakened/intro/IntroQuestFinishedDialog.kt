package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.jumping.Jump
import org.wisp.gatesawakened.wispLib.addPara

class IntroQuestFinishedDialog : InteractionDialogPlugin {
    private lateinit var dialog: InteractionDialogAPI

    override fun init(dialog: InteractionDialogAPI) {
        this.dialog = dialog

        dialog.visualPanel.showImagePortion("illustrations", "dead_gate", 640f, 400f, 0f, 0f, 480f, 300f)

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
                text.addPara(
                    "The Gate looks derelict, the same as any other, but it is still a sight to behold. " +
                            "The Domain had many triumphs and created many wonders, " +
                            "but the Gates represented the pinnacle of the technology of Man - and now represent how far it has fallen."
                )
                text.addPara {
                    "However, as your fleet moves closer, sensors pick up a faint energy signatures on the ring; " +
                            "only perceptible to those close enough and specifically looking for something. " +
                            "It seems to be emanating from a specific area."
                }
                dialog.optionPanel.addOption(
                    Option.CONTINUE.text,
                    Option.CONTINUE
                )
            }
            Option.CONTINUE -> {
                text.addPara {
                    "The source of the energy is a small, but clearly labeled, connector to supply fuel to the gate. " +
                            "In the ${di.sector.clock.cycle} cycles since the Collapse, " +
                            "it seems the adapter design has had no reason to change."
                }
                text.addPara {
                    "A readout indicates that the gate is already fueled - perhaps left over from the time of the Domain."
                }
                text.addPara("Your crew looks to you. It seems there's only one thing to do.")

                dialog.optionPanel.addOption(
                    Option.FLY_THROUGH.text,
                    Option.FLY_THROUGH
                )
                dialog.optionPanel.addOption(
                    Option.LEAVE.text,
                    Option.LEAVE
                )
            }
            Option.FLY_THROUGH -> {
                val coreGate = Intro.coreGate

                if (coreGate != null) {
                    dialog.dismiss()
                    di.sector.isPaused = false
                    Jump.jumpPlayer(
                        sourceGate = dialog.interactionTarget,
                        destinationGate = coreGate,
                        isFuelRequired = false
                    )
                    // TODO don't show this dialog again if player touches it when jumping
                    di.sector.addScript(ShowIntroEpilogueAfterJumpCompletes())
                }
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
        CONTINUE("Continue"),
        FLY_THROUGH("Fly through the gate"),
        LEAVE("Leave")
    }
}