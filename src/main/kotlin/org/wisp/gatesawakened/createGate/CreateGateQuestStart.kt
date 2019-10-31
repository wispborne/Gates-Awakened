package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
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
            image = null,  // Show image of gate appearing on top of a station?
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
                    "It goes on to explain that selecting any position except your present location is no longer possible due to \"security reasons\"."
                }

            },
            options = listOf(
                Option(
                    text = { "Read the fine print" },
                    onOptionSelected = { it.goToPage(Pages.Security) }
                ),
                Option(
                    text = { "Don't bother reading the rest" },
                    onOptionSelected = { it.goToPage(Pages.Final) }
                )
            )
        ),
        Page(
            id = Pages.Security,
            onPageShown = {
                addPara { "\"- May not be located at an unsafe proximity to a celestial body\"" }
                addPara { "\"- May not be located in hyperspace. Doing so will collapse hyperspace in a 10 ly radius\"" }
                addPara { "\"- Multiple Gates within the same star system will result in total system hyperwave collapse\"" }
                addPara {
                    "\"- The drone fleet will automatically stand down once contact is established with the " +
                            mark("Reach") + "\""
                }
                addPara(
                    textColor = di.sector.getFaction("luddic_church")?.color ?: Misc.getHighlightColor()
                ) {
                    "\"USE THESE WORDS, MY BROTHERS. REAP THE CROPS OF THE UNREPENTANT. THEY WILL BE CAST OUT OF THE PROMISED " +
                            "LAND AND IT WILL BE SHAPED TO THE VISION OF GOD.\""
                }
                addPara {
                    "It seems clear that, whatever the original purpose of this Tripad, it has been modified."
                }
            },
            options = listOf(
                tapProclamationOption,
                Option(
                    text = { "Tap on \"Reach\"" },
                    onOptionSelected = { it.goToPage(Pages.Reach) }
                ),
                goToFinalOption
            )
        ),
        Page(
            id = Pages.Reach,
            onPageShown = {
                addPara { "Domain Agency: The Reach" }
                addPara { "\"Bringing the stars together.\"" }
                addPara { "The nearest Reach representative is located at <not found>. Appointment required." }
            },
            options = listOf(
                tapProclamationOption,
                goToFinalOption
            )
        ),
        Page(
            id = Pages.Final,
            onPageShown = {
                addPara { "You may now interact with the Gate Hauler intel to designate a location for a Gate." }
            },
            options = listOf(
                Option(
                    text = { "Close" },
                    onOptionSelected = {
                        it.close(hideQuestOfferAfterClose = true)

                    }
                )
            )
        )
    )
) {
    companion object {
        val tapProclamationOption = Option<CreateGateQuestStart>(
            text = { "Tap on the religious proclamation" },
            onOptionSelected = { addPara { "Nothing happens." } }
        )
        val goToFinalOption = Option<CreateGateQuestStart>(
            text = { "Close the Tripad" },
            onOptionSelected = { it.goToPage(Pages.Final) }
        )
    }

    enum class Pages {
        One,
        Two,
        Security,
        Reach,
        Final
    }
}