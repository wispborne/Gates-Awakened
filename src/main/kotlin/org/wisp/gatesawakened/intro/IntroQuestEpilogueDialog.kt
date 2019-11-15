package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.empty
import org.wisp.gatesawakened.wispLib.addPara

class IntroQuestEpilogueDialog : InteractionDialogPlugin {
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
                text.addPara {
                    "A stunned silence falls over your crew. Despite the signs, nobody had expected the Gate to work."
                }
                text.addPara {
                    "Your Second Officer recovers first. \"Sir, this is monumental...but I recommend caution. " +
                            "If any others in the Sector witness us using a working gate, they will believe that we know " +
                            "how to activate the rest and surely resort to violent measures.\""
                }
                dialog.optionPanel.addOption(Option.AGREE.text, Option.AGREE)
            }
            Option.AGREE -> {
                text.addPara("")
                text.addPara {
                    "You may now jump instantly between " +
                            mark(Intro.fringeGate?.starSystem?.baseName ?: String.empty) +
                            " and " +
                            mark(Intro.coreGate?.starSystem?.baseName ?: String.empty) + "."
                }
                text.addPara {
                    "Each jump will incur a " + mark("fuel cost") + "to power the Gate relative to the cost of a direct flight."
                }
                text.addPara {
                    "You may only use a Gate when you are not being " + mark("tracked") + " by any other fleet."
                }
                dialog.optionPanel.addOption(Option.LEAVE.text, Option.LEAVE)
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
        AGREE("Agree that secrecy is in order"),
        LEAVE("Leave")
    }
}