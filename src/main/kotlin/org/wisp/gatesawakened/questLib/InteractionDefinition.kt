package org.wisp.gatesawakened.questLib

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.util.Misc

abstract class InteractionDefinition<S : InteractionDefinition<S>>(
    val onInteractionStarted: S.() -> Unit,
    val pages: List<DialogPage<S>>
) {
    class DialogPage<S>(
        val id: Any,
        val image: Image? = null,
        val onPageShown: S.() -> Unit,
        val options: List<Option<S>>
    )

    open class Option<S>(
        val text: S.() -> String,
        val shortcut: Shortcut? = null,
        val onOptionSelected: S.(PageNavigator) -> Unit,
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

    lateinit var dialog: InteractionDialogAPI


    /**
     * Needed so we can figure out which BarEvents are part of this mod
     * when looking at [BarEventManager.getInstance().active.items].
     */
    abstract inner class InteractionDialog : InteractionDialogPlugin {
    }

    fun build(): InteractionDialog {
        return object : InteractionDialog() {

            lateinit var dialog: InteractionDialogAPI

            private val navigator = object : PageNavigator {
                override fun goToPage(pageId: Any) {
                    showPage(pages.single { it.id == pageId })
                }

                override fun close(hideQuestOfferAfterClose: Boolean) {
                    dialog.dismiss()
                }
            }


            /**
             * Called when the dialog is shown.
             */
            override fun init(dialog: InteractionDialogAPI) {
                this.dialog = dialog
                onInteractionStarted(this@InteractionDefinition as S)

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

                optionSelected.onOptionSelected(this@InteractionDefinition as S, navigator)
            }

            fun showPage(page: DialogPage<S>) {
                dialog.optionPanel.clearOptions()

                page.onPageShown(this@InteractionDefinition as S)
                page.options.forEach { option ->
                    dialog.optionPanel.addOption(option.text(this@InteractionDefinition as S), option.id)
                }
            }

            override fun optionMousedOver(optionText: String?, optionData: Any?) {
            }

            override fun getMemoryMap(): MutableMap<String, MemoryAPI> = mutableMapOf()
            override fun backFromEngagement(battleResult: EngagementResultAPI?) {
            }

            override fun advance(amount: Float) {
            }

            override fun getContext(): Any? = null
        }
    }
}