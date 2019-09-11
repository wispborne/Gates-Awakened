package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.appendPara
import org.wisp.gatesawakened.exhaustiveWhen

/**
 * Creates the intro quest at the bar.
 */
class IntroBarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent =
        IntroQuestBeginning()

    override fun getBarEventTimeoutDuration(): Float = Float.MAX_VALUE

    override fun getBarEventFrequencyWeight(): Float =
        if (Common.isDebugModeEnabled) {
            100f
        } else {
            super.getBarEventFrequencyWeight()
        }
}

/**
 * Facilitates the intro quest at the bar.
 */
class IntroQuestBeginning : BaseBarEventWithPerson() {

    override fun shouldShowAtMarket(market: MarketAPI): Boolean =
        super.shouldShowAtMarket(market)
                && Intro.shouldOfferQuest(market)

    /**
     * Set up the text that appears when the player goes to the bar
     * and the option for them to init the conversation.
     */
    override fun addPromptAndOption(dialog: InteractionDialogAPI) {
        super.addPromptAndOption(dialog)
        regen(dialog.interactionTarget.market)
        val text = dialog.textPanel

        text.appendPara(
            "A $manOrWoman's tattoo catches your attention. " +
                    "The dark grey circle wraps around $hisOrHer left eye, emitting a faint white glow. " +
                    "You've never seen the like. " +
                    "${heOrShe.capitalize()} is focused on $hisOrHer tripad in a corner of the bar " +
                    "and it looks like $heOrShe is staring at an image of a %s.",
            "Gate"
        )

        dialog.optionPanel.addOption(
            "Move nearer for a closer look at $hisOrHer screen.",
            this
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
            val destinationSystem = Intro.fringeGate?.starSystem!!
            dialog.optionPanel.clearOptions()

            when (optionData) {
                OptionId.INIT -> {
                    dialog.textPanel.appendPara(
                        "As soon as you get close, " +
                                "$heOrShe flips off $hisOrHer tripad and quickly rushes out."
                    )
                    dialog.textPanel.appendPara(
                        "However, just before $hisOrHer tripad goes dark, you catch one line: %s",
                        destinationSystem.name
                    )

                    startIntroQuest()

                    dialog.optionPanel.addOption(
                        "Watch the $manOrWoman hurry down the street and consider what " +
                                "could be at ${destinationSystem.baseName}.",
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
        val wasQuestSuccessfullyStarted = Intro.startQuest(dialog.interactionTarget)

        if (wasQuestSuccessfullyStarted) {
            BarEventManager.getInstance().notifyWasInteractedWith(this)
        } else {
            dialog.textPanel.appendPara("After a moment's consideration, you decide that there's nothing out there after all.")
        }
    }

    override fun getPersonRank(): String {
        return Ranks.SPACE_SAILOR
    }

    enum class OptionId {
        INIT,
        LEAVE
    }
}