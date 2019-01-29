package org.toast.activegates.intro

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import org.toast.activegates.Di
import org.toast.activegates.addPara
import org.toast.activegates.empty

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
                text.addPara("A stunned silence falls over your crew. Despite the signs, nobody had expected the Gate to work.")
                text.addPara(
                    "Your Second Officer recovers first. \"Sir, this is monumental...but I recommend %s. " +
                            "If any others in the Sector witness us utilizing a working gate, they will believe that we know " +
                            "how to activate the rest and surely resort to violent measures.\"",
                    "caution"
                )
                dialog.optionPanel.addOption(Option.AGREE.text, Option.AGREE)
            }
            Option.AGREE -> {
                text.addPara("")
                text.addPara(
                    "You may now jump instantly between %s and %s.",
                    Intro.fringeGate?.starSystem?.baseName ?: String.empty,
                    Intro.coreGate?.starSystem?.baseName ?: String.empty
                )
                text.addPara(
                    "Each jump will incur a %s to power the Gate equal to the cost of a direct flight.",
                    "fuel cost"
                )
                text.addPara("You may only use a Gate when you are not being %s by any other fleet.", "tracked")
                dialog.optionPanel.addOption(Option.LEAVE.text, Option.LEAVE)
            }
            Option.LEAVE -> {
                Di.inst.sector.isPaused = false
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