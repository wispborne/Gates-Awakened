package org.wisp.gatesawakened.questLib

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.wispLib.ParagraphText
import org.wisp.gatesawakened.wispLib.addPara
import java.awt.Color

abstract class InteractionDefinition<S : InteractionDefinition<S>>(
    val onInteractionStarted: S.() -> Unit,
    val pages: List<Page<S>>,
    private val shouldValidateOnDialogStart: Boolean = true
) {
    class Page<S>(
        val id: Any,
        val image: Image? = null,
        val onPageShown: S.() -> Unit,
        val options: List<Option<S>>
    )

    open class Option<S>(
        val text: S.() -> String,
        val shortcut: Shortcut? = null,
        val onOptionSelected: S.(InteractionDefinition<*>.PageNavigator) -> Unit,
        val id: String = Misc.random.nextInt().toString()
    )

//    interface PageNavigator<S> {
//        fun goToPage(pageId: Any)
//        fun gotoPage(page: Page<S>)
//        fun close(hideQuestOfferAfterClose: Boolean)
//    }

    open inner class PageNavigator() {
        open fun goToPage(pageId: Any) {
            showPage(pages.single { it.id == pageId })
        }

        open fun goToPage(page: Page<S>) {
            showPage(page)
        }

        open fun close(hideQuestOfferAfterClose: Boolean) {
            dialog.dismiss()
        }

        open fun showPage(page: Page<S>) {
            dialog.optionPanel.clearOptions()

            page.onPageShown(this@InteractionDefinition as S)
            page.options.forEach { option ->
                dialog.optionPanel.addOption(option.text(this@InteractionDefinition as S), option.id)
            }
        }
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

    fun addPara(
        textColor: Color = Misc.getTextColor(),
        highlightColor: Color = Misc.getHighlightColor(),
        stringMaker: ParagraphText.() -> String
    ) = dialog.textPanel.addPara(textColor, highlightColor, stringMaker)


    /**
     * Needed so we can figure out which BarEvents are part of this mod
     * when looking at [BarEventManager.getInstance().active.items].
     */
    abstract inner class InteractionDialog : InteractionDialogPlugin

    fun build(): InteractionDialog {
        return object : InteractionDialog() {

            private val navigator = PageNavigator()

            /**
             * Called when this class is instantiated.
             */
            init {
                if (shouldValidateOnDialogStart) {

                }
            }

            /**
             * Called when the dialog is shown.
             */
            override fun init(dialog: InteractionDialogAPI) {
                this@InteractionDefinition.dialog = dialog
                onInteractionStarted(this@InteractionDefinition as S)

                if (pages.any()) {
                    navigator.showPage(pages.first())
                }
            }

            override fun optionSelected(optionText: String?, optionData: Any?) {
                val optionSelected = pages
                    .flatMap { page ->
                        page.options
                            .filter { option -> option.id == optionData }
                    }.single()

                optionSelected.onOptionSelected(this@InteractionDefinition as S, navigator)
            }

            // Other overrides that are necessary but do nothing
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