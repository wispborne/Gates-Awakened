package org.wisp.gatesawakened.intro

import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.empty
import org.wisp.gatesawakened.questLib.InteractionDefinition

class IntroQuestEpilogueDialog : InteractionDefinition<IntroQuestEpilogueDialog>(
    onInteractionStarted = {
        di.sector.isPaused = true
    },
    pages = listOf(
        Page(
            id = 1,
            image = Image(
                category = "illustrations",
                id = "dead_gate"
            ),
            onPageShown = {
                addPara {
                    "A stunned silence falls over your crew. Despite the signs, nobody had expected the Gate to work."
                }
                addPara {
                    "Your Second Officer recovers first. \"Sir, this is monumental...but I recommend caution. " +
                            "If any others in the Sector witness us using a working gate, they will believe that we know " +
                            "how to activate the rest and surely resort to violent measures.\""
                }
            },
            options = listOf(
                Option(
                    text = { "Agree that secrecy is in order" },
                    onOptionSelected = { it.goToPage(2) })
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara { "" }
                addPara {
                    "You may now jump instantly between " +
                            mark(Intro.fringeGate?.starSystem?.baseName ?: String.empty) +
                            " and " +
                            mark(Intro.coreGate?.starSystem?.baseName ?: String.empty) + "."
                }
                addPara {
                    "Each jump will incur a " + mark("fuel cost") + "to power the Gate relative to the cost of a direct flight."
                }
                addPara {
                    "You may only use a Gate when you are not being " + mark("tracked") + " by any other fleet."
                }
            },
            options = listOf(
                Option(
                    text = { "Leave" },
                    onOptionSelected = {
                        di.sector.isPaused = false
                        it.close(hideQuestOfferAfterClose = true)
                    })
            )
        )
    )
)