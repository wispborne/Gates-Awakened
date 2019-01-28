package org.toast.activegates.intro

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson
import com.fs.starfarer.api.util.Misc
import org.toast.activegates.equalsAny
import org.toast.activegates.exhaustiveWhen


class IntroBarEvent : BaseBarEventWithPerson() {

    override fun shouldShowAtMarket(market: MarketAPI): Boolean =
        when {
            !super.shouldShowAtMarket(market) -> false
            Intro.fringeGate == null -> false
            else -> !market.factionId.equalsAny("luddic_church", "luddic_path")
        }

    /**
     * Set up the text that appears when the player goes to the bar
     * and the option for them to start the conversation.
     */
    override fun addPromptAndOption(dialog: InteractionDialogAPI) {
        super.addPromptAndOption(dialog)
        regen(dialog.interactionTarget.market)
        val text = dialog.textPanel

        text.addPara("A $manOrWoman wearing an attention-grabbing cowboy hat is focused on $hisOrHer tripad in a corner of the bar; it looks like $heOrShe is staring at an image of a Gate.")

        dialog.optionPanel.addOption(
            "Move nonchalantly nearer to the \"cow$boyOrGirl\" for a closer look at $hisOrHer screen.", this
        )
    }

    /**
     * Called when the player chooses to start the conversation.
     */
    override fun init(dialog: InteractionDialogAPI) {
        super.init(dialog)
        this.done = false
        dialog.visualPanel.showPersonInfo(this.person, true)
        this.optionSelected(null as String?, OptionId.INIT)
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        if (optionData is OptionId) {
            val destinationSystem = Intro.fringeGate?.starSystem
            dialog.optionPanel.clearOptions()

            when (optionData) {
                OptionId.INIT -> {
                    dialog.textPanel.addPara("You try to act naturally, but as soon as you get close, $heOrShe flips off $hisOrHer tripad and quickly rushes out, almost deliberately not looking at you.")
                    dialog.textPanel.addPara(
                        "However, just before $hisOrHer tripad goes dark, you catch one line: %s",
                        Misc.getHighlightColor(),
                        destinationSystem?.name
                    )
                    startIntroQuest()
                    dialog.optionPanel.addOption(
                        "Watch the $manOrWoman hurry down the street and consider what could be at ${destinationSystem?.baseName}.",
                        OptionId.LEAVE
                    )
                }
                OptionId.LEAVE -> {
                    noContinue = true
                    done = true
                }
            }.exhaustiveWhen
        }
    }

    private fun startIntroQuest() {
        val wasQuestStarted = Intro.startIntroQuest(dialog.interactionTarget)

        if (wasQuestStarted) {
            BarEventManager.getInstance().notifyWasInteractedWith(this)
        } else {
            dialog.textPanel.addPara("After a moment's consideration, you decide that there's nothing out there after all.")
        }
    }

    private val boyOrGirl
        get() = if (personGender == FullName.Gender.FEMALE) "girl" else "boy"

    override fun getPersonRank(): String {
        return Ranks.SPACE_SAILOR
    }

    enum class OptionId {
        INIT,
        LEAVE
    }
}