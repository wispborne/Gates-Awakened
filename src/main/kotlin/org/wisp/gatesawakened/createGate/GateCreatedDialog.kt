package org.wisp.gatesawakened.createGate

import org.wisp.gatesawakened.questLib.InteractionDefinition

class GateCreatedDialog : InteractionDefinition<GateCreatedDialog>(
    onInteractionStarted = {
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
                    "It hardly seems possible, but there is now a Gate where there was none before. " +
                            "It looms ahead, completely indistinguishable from the others scattered throughout the sector."
                }
            },
            options = listOf(
                Option(
                    text = { "Scan it" },
                    onOptionSelected = { it.goToPage(2) }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara { "A quick scan shows that it is active." }
                addPara {
                    "You don't know what to make of the implications. Perhaps the entire Gate Network hadn't shut down, " +
                            "but somehow the Persean Sector had been cut off? Or maybe they had all gone down but simply " +
                            "needed to be manually activated again, and the Gate Hauler did so? Was Ludd actually a real person, " +
                            "or could this all have been some conspiracy orchestrated by the Domain? Or by fanatics?"
                }
                addPara { "You wonder if you will ever find out." }
            },
            options = listOf(
                Option(
                    text = { "Close" },
                    onOptionSelected = {
                        CreateGateQuest.completeQuest()
                        it.close(hideQuestOfferAfterClose = true)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = GateCreatedDialog()
}