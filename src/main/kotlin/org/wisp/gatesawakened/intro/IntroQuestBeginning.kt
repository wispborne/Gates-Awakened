package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import org.wisp.gatesawakened.questLib.BarEventCreator
import org.wisp.gatesawakened.questLib.QuestDefinition
import org.wisp.gatesawakened.questLib.addPara

/**
 * Creates the intro quest at the bar.
 */
class IntroBarEventCreator : BarEventCreator(
    creator = { IntroQuestBeginning().build() }
)

/**
 * Facilitates the intro quest at the bar.
 */
class IntroQuestBeginning : QuestDefinition<IntroQuestBeginning.State>(
    state = State(),
    shouldShowEvent = { market -> Intro.shouldOfferQuest(market) },
    textOnInteractionOffer = {
        dialog.textPanel.addPara {
            "A $manOrWoman's tattoo catches your attention. " +
                    "The dark grey circle wraps around $hisOrHer left eye, emitting a faint white glow. " +
                    "You've never seen the like. " +
                    "${heOrShe.capitalize()} is focused on $hisOrHer tripad in a corner of the bar " +
                    "and it looks like $heOrShe is staring at an image of a " + mark("Gate") + "."
        }
    },
    textToStartInteraction = { "Move nearer for a closer look at $hisOrHer screen." },
    onInteractionStarted = {
    },
    pages = listOf(
        DialogPage(
            id = State.OptionId.INIT,
            isInitialPage = true,
            textOnPageShown = {
                state.destinationSystem = Intro.fringeGate?.starSystem!!
                dialog.textPanel.addPara {
                    "As soon as you get close, " +
                            "$heOrShe flips off $hisOrHer tripad and quickly rushes out."
                }
                dialog.textPanel.addPara {
                    "However, just before $hisOrHer tripad goes dark, you catch one line: " + mark(state.destinationSystem!!.name)
                }
            },
            options = listOf(
                Option(
                    text = {
                        "Watch the $manOrWoman hurry down the street and consider what " +
                                "could be at ${state.destinationSystem!!.baseName}."
                    },
                    onClick = { navigator ->
                        val wasQuestSuccessfullyStarted = Intro.startQuest(this.dialog.interactionTarget)

                        if (!wasQuestSuccessfullyStarted) {
                            this.dialog.textPanel.addPara { "After a moment's consideration, you decide that there's nothing out there after all." }
                        }

                        navigator.close(hideQuestOfferAfterClose = true)
                    }
                )
            )
        )
    ),
    personRank = Ranks.SPACE_SAILOR
) {
    class State {
        var destinationSystem: StarSystemAPI? = null

        enum class OptionId {
            INIT
        }
    }
}