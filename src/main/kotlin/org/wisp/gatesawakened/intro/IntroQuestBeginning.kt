package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.BarEventCreator
import org.wisp.gatesawakened.questLib.BarEventDefinition

/**
 * Creates the intro quest at the bar.
 */
class IntroBarEventCreator : BarEventCreator(
    creator = { IntroQuestBeginning().buildBarEvent() }
)

/**
 * Facilitates the intro quest at the bar.
 */
class IntroQuestBeginning : BarEventDefinition<IntroQuestBeginning>(
    shouldShowEvent = { market -> Intro.shouldOfferQuest(market) },
    interactionPrompt = {
        addPara {
            "A $manOrWoman's tattoo catches your attention. " +
                    "The dark grey circle wraps around $hisOrHer left eye, emitting a faint white glow. " +
                    "You've never seen the like. " +
                    "${heOrShe.capitalize()} is focused on $hisOrHer tripad in a corner of the bar " +
                    "and it looks like $heOrShe is staring at an image of a " + mark("Gate") + "."
        }
    },
    textToStartInteraction = { "Move nearer for a closer look at $hisOrHer screen." },
    onInteractionStarted = {
        destinationSystem = Intro.fringeGate?.starSystem!!
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara {
                    "As soon as you get close, " +
                            "$heOrShe flips off $hisOrHer tripad and quickly rushes out."
                }
                addPara {
                    "However, just before $hisOrHer tripad goes dark, you catch one line: " + mark(destinationSystem!!.name)
                }
            },
            options = listOf(
                Option(
                    text = {
                        "Watch the $manOrWoman hurry down the street and consider what " +
                                "could be at ${destinationSystem!!.baseName}."
                    },
                    onOptionSelected = { navigator ->
                        val wasQuestSuccessfullyStarted = Intro.startQuest(dialog.interactionTarget)
                        navigator.close(hideQuestOfferAfterClose = true)

                        if (!wasQuestSuccessfullyStarted) {
                            errorReporter.reportCrash(RuntimeException("Unable to start intro quest!"))
                        }
                    }
                )
            )
        )
    ),
    personRank = Ranks.SPACE_SAILOR
) {
    private var destinationSystem: StarSystemAPI? = null
    private val errorReporter = di.errorReporter
}