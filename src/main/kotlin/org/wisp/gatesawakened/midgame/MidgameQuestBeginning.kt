package org.wisp.gatesawakened.midgame

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
 * Creates the midgame quest at the bar.
 */
class MidgameBarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent =
        MidgameQuestBeginning()

    override fun getBarEventFrequencyWeight(): Float =
        if (Common.isDebugModeEnabled) {
            100f
        } else {
            super.getBarEventFrequencyWeight()
        }
}

class MidgameQuestBeginning : BaseBarEventWithPerson() {
    override fun shouldShowAtMarket(market: MarketAPI): Boolean =
        super.shouldShowAtMarket(market)
                && Midgame.shouldOfferQuest(market)

    /**
     * Set up the text that appears when the player goes to the bar
     * and the option for them to start the conversation.
     */
    override fun addPromptAndOption(dialog: InteractionDialogAPI) {
        super.addPromptAndOption(dialog)
        regen(dialog.interactionTarget.market)
        val text = dialog.textPanel

        text.appendPara(
            "You spot a familiar tattoo; a grey circle around the eye of a $manOrWoman" +
                    " in the corner of the bar, glowing a faint white. " +
                    "You realize that it looks a bit like a %s.",
            "Gate"
        )

        dialog.optionPanel.addOption(
            "Move in for a closer look at the tattooed $manOrWoman's tripad screen.",
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
        this.optionSelected(null, OptionId.INIT)
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        if (optionData is OptionId) {
            val planetWithCache = Midgame.planetWithCache
            dialog.optionPanel.clearOptions()

            when (optionData) {
                OptionId.INIT -> {
                    if (planetWithCache == null) {
                        dialog.textPanel.appendPara("But their screen is blank. How unexpected.")
                        dialog.optionPanel.addOption(
                            "Wander away.",
                            OptionId.LEAVE
                        )
                    } else {
                        dialog.textPanel.appendPara(
                            "You casually peer over the $manOrWoman's shoulder, " +
                                    "reading from $hisOrHer screen."
                        )
                        dialog.textPanel.appendPara(
                            ("\"The alpha core has decoded another section of the %s. " +
                                    "It mentions the location of a cache which, apparently, " +
                                    "contains %s. Absolutely incredible. We haven't shared this information, " +
                                    "so there is no rush, but when possible, please retrieve the cache. " +
                                    "It is located on %s at 56.4314° N, 6.3414° W in %s.\""),
                            "transmission",
                            "activation codes for any two Gates",
                            planetWithCache.name,
                            planetWithCache.starSystem.baseName
                        )

                        startMidgameQuest()

                        dialog.optionPanel.addOption(
                            "Note the information and casually wander away.",
                            OptionId.WANDER
                        )
                    }
                }
                OptionId.WANDER -> {
                    dialog.textPanel.appendPara(
                        "You move away without the $manOrWoman noticing. " +
                                "You can't help but think that they ought to stop flashing " +
                                "such important information around at bars."
                    )
                    dialog.optionPanel.addOption(
                        "Continue.",
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

    private fun startMidgameQuest() {
        val wasQuestSuccessfullyStarted = Midgame.startQuest(dialog.interactionTarget)

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
        WANDER,
        LEAVE
    }
}