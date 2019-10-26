package org.wisp.gatesawakened.questLib

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson

abstract class BarEventDefinition<S : InteractionDefinition<S>>(
    private val shouldShowEvent: (MarketAPI) -> Boolean,
    val interactionPrompt: S.() -> Unit,
    val textToStartInteraction: S.() -> String,
    onInteractionStarted: S.() -> Unit,
    pages: List<DialogPage<S>>,
    val personRank: String = Ranks.CITIZEN,
    val personFaction: String = Factions.INDEPENDENT,
    val personGender: FullName.Gender = FullName.Gender.ANY,
    val personPost: String = Ranks.CITIZEN,
    val personPortrait: String? = null
) : InteractionDefinition<S>(
    onInteractionStarted = onInteractionStarted,
    pages = pages
) {

    lateinit var manOrWoman: String
    lateinit var hisOrHer: String
    lateinit var heOrShe: String
    lateinit var event: BaseBarEventWithPerson

    /**
     * Needed so we can figure out which BarEvents are part of this mod
     * when looking at [BarEventManager.getInstance().active.items].
     */
    abstract inner class BarEvent : BaseBarEventWithPerson() {
    }

    fun buildBarEvent(): BarEvent {
        return object : BarEvent() {
            private val navigator = object : PageNavigator {
                override fun goToPage(pageId: Any) {
                    showPage(pages.single { it.id == pageId })
                }

                override fun close(hideQuestOfferAfterClose: Boolean) {
                    if (hideQuestOfferAfterClose) {
                        BarEventManager.getInstance().notifyWasInteractedWith(event)
                    }

                    noContinue = true
                    done = true
                }
            }

            override fun shouldShowAtMarket(market: MarketAPI?): Boolean =
                super.shouldShowAtMarket(market) && market?.let(shouldShowEvent) ?: true

            /**
             * Set up the text that appears when the player goes to the bar
             * and the option for them to init the conversation.
             */
            override fun addPromptAndOption(dialog: InteractionDialogAPI) {
                super.addPromptAndOption(dialog)
                regen(dialog.interactionTarget.market)
                this@BarEventDefinition.manOrWoman = manOrWoman
                this@BarEventDefinition.hisOrHer = hisOrHer
                this@BarEventDefinition.heOrShe = heOrShe
                this@BarEventDefinition.dialog = dialog
                this@BarEventDefinition.event = this
                interactionPrompt(this@BarEventDefinition as S)

                dialog.optionPanel.addOption(
                    textToStartInteraction(),
                    this as BaseBarEventWithPerson
                )
            }

            /**
             * Called when the player chooses to start the conversation.
             */
            override fun init(dialog: InteractionDialogAPI) {
                super.init(dialog)
                this.done = false
                dialog.visualPanel.showPersonInfo(this.person, true)
                onInteractionStarted(this@BarEventDefinition as S)

                if (pages.any()) {
                    showPage(pages.first())
                }
            }

            override fun optionSelected(optionText: String?, optionData: Any?) {
                val optionSelected = pages
                    .flatMap { page ->
                        page.options.filter { option ->
                            option.id == optionData
                        }
                    }.single()

                optionSelected.onOptionSelected(this@BarEventDefinition as S, navigator)
            }

            fun showPage(page: DialogPage<S>) {
                dialog.optionPanel.clearOptions()

                page.onPageShown(this@BarEventDefinition as S)
                page.options.forEach { option ->
                    dialog.optionPanel.addOption(option.text(this@BarEventDefinition as S), option.id)
                }
            }
        }
    }
}

abstract class BarEventCreator(
    private val probability: Float? = null,
    private val creator: () -> PortsideBarEvent
) : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = creator()

    override fun getBarEventFrequencyWeight(): Float =
        probability ?: super.getBarEventFrequencyWeight()
}