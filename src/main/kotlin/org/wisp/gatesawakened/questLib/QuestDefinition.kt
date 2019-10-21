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
import com.fs.starfarer.api.util.Misc

abstract class QuestDefinition<S>(
    val state: S,
    private val shouldShowEvent: (MarketAPI) -> Boolean,
    val interactionPrompt: Context<S>.() -> Unit,
    val textToStartInteraction: Context<S>.() -> String,
    val onInteractionStarted: Context<S>.() -> Unit,
    val pages: List<DialogPage<Context<S>>>,
    val personRank: String = Ranks.CITIZEN,
    val personFaction: String = Factions.INDEPENDENT,
    val personGender: FullName.Gender = FullName.Gender.ANY,
    val personPost: String = Ranks.CITIZEN,
    val personPortrait: String? = null
) {
    class DialogPage<Context>(
        val id: Any,
        val image: Image? = null,
        val onPageShown: Context.() -> Unit,
        val options: List<Option<Context>>
    )

    open class Option<Context>(
        val text: Context.() -> String,
        val shortcut: Shortcut? = null,
        val onOptionSelected: Context.(PageNavigator) -> Unit,
        val id: String = Misc.random.nextInt().toString()
    )

    interface PageNavigator {
        fun goToPage(pageId: Any)
        fun close(hideQuestOfferAfterClose: Boolean)
    }

    /**
     * @param code constant from [org.lwjgl.input.Keyboard]
     */
    class Shortcut(
        code: Int,
        holdCtrl: Boolean,
        holdAlt: Boolean,
        holdShift: Boolean
    )

    class Image(
        val category: String,
        val id: String,
        val width: Float = 640f,
        val height: Float = 400f,
        val xOffset: Float = 0f,
        val yOffset: Float = 0f,
        val displayWidth: Float = 480f,
        val displayHeight: Float = 300f
    )

    class Context<S>(
        val state: S,
        val manOrWoman: String,
        val hisOrHer: String,
        val heOrShe: String,
        val dialog: InteractionDialogAPI,
        val event: BaseBarEventWithPerson
    )

    abstract inner class BarEvent : BaseBarEventWithPerson() {
    }

    fun build(): BarEvent {
        return object : BarEvent() {
            /**
             * Must be created after `regen` is called so that Person exists.
             */
            private lateinit var context: Context<S>

            private val navigator = object : PageNavigator {
                override fun goToPage(pageId: Any) {
                    showPage(pages.single { it.id == pageId })
                }

                override fun close(hideQuestOfferAfterClose: Boolean) {
                    if (hideQuestOfferAfterClose) {
                        BarEventManager.getInstance().notifyWasInteractedWith(context.event)
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
                context = Context(
                    state = state,
                    manOrWoman = manOrWoman,
                    hisOrHer = hisOrHer,
                    heOrShe = heOrShe,
                    dialog = dialog,
                    event = this
                )
                interactionPrompt(context)

                dialog.optionPanel.addOption(
                    textToStartInteraction(context),
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
                onInteractionStarted(context)

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

                optionSelected.onOptionSelected(context, navigator)
            }

            fun showPage(page: DialogPage<Context<S>>) {
                dialog.optionPanel.clearOptions()

                page.onPageShown(context)
                page.options.forEach { option ->
                    dialog.optionPanel.addOption(option.text(context), option.id)
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