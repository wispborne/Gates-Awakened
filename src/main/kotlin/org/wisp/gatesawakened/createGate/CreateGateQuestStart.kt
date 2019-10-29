package org.wisp.gatesawakened.createGate

import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.questLib.InteractionDefinition

class CreateGateQuestStart : InteractionDefinition<CreateGateQuestStart>(
    onInteractionStarted = {
    },
    pages = listOf(
        Page(
            id = Pages.One,
            onPageShown = {
                addPara {
                    "As you approach the Gate, there is a chime. It is not a familiar chime, and unfamiliar sounds on your ship are rarely welcome, " +
                            "least of all when nearing an ancient, massive relic of the Domain - and one that you activated."
                }
                addPara {
                    "It turns out that the soft chiming is coming from the Tripad found in the cave back on ${Midgame.planetWithCache?.spec?.name
                        ?: "a seemingly unremarkable planet"}. The display shows three words, " + mark("\"Gate Hauler Detected\"") + "."
                }
                addPara {
                    "There isn't a child in the Persean Sector that hasn't heard of the Gate Haulers. Massive and hulking, these autonomous starships " +
                            "were responsible for moving Gates the vast distances required to build the Gate Network, the backbone upon which the Domain ruled. " +
                            "As the stories go, passing Haulers were once common occurrences. Sent from heavily guarded factory worlds, " +
                            "they ventured outward toward unknown space, but traveled near or through colonized sectors for reasons unknown."
                }
            },
            options = listOf(
                Option(
                    text = { "See what's happening with the Tripad" },
                    onOptionSelected = { it.goToPage(Pages.Two) }
                )
            )
        ),
        Page(
            id = Pages.Two,
            onPageShown = {
                addPara {
                    "As you shakily tap through the Tripad, it becomes clear that at least one reason for the Gate Haulers to travel near " +
                            "inhabited space is to allow their routes to be detected and modified as needed by specially enabled devices…such as " +
                            "the one in your hand."
                }
                addPara {
                    "The procedure is surprisingly well documented. The Gate Hauler does not actually enter the sector, but rather slows, turns and returns " +
                            "\"home\" to pick up another Gate. Its cargo is then moved the relatively short distance into place by a fleet of drones. " +
                            "The entire process is initiated simply by moving to the desired Gate location and touching a few elements on the Tripad."
                }
                addPara {
                    "It goes on to explain that selecting a remote location is no longer possible due to \"security reasons\"."
                }

            },
            options = listOf(
                Option(
                    text = { "Browse the rest of the pages on security" },
                    onOptionSelected = { it.goToPage(Pages.Security) }
                )
            )
        )
    )
) {
    enum class Pages {
        One,
        Two,
        Security
    }
}