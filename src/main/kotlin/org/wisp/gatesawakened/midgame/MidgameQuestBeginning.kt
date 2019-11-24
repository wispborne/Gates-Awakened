package org.wisp.gatesawakened.midgame

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.wisp.gatesawakened.questLib.BarEventDefinition
import org.wisp.gatesawakened.questLib.InteractionDefinition.Page

/**
 * Creates the midgame quest at the bar.
 */
class MidgameBarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent() = MidgameQuestBeginning().buildBarEvent()
}

class MidgameQuestBeginning : BarEventDefinition<MidgameQuestBeginning>(
    shouldShowEvent = { Midgame.shouldOfferQuest(it) },
    interactionPrompt = {
        addPara {
            "You spot a familiar tattoo; a grey circle around the eye of a $manOrWoman" +
                    " in the corner of the bar, glowing a faint white. " +
                    "You realize that it looks a bit like a " + mark("Gate") + "."
        }
    },
    textToStartInteraction = {
        "Move in for a closer look at the tattooed $manOrWoman's tripad screen."
    },
    onInteractionStarted = {
        planetWithCache = Midgame.planetWithCache!! // Must exist for quest to be offered
    },
    pages = listOf(
        Page(
            id = Page.Initial,
            onPageShown = {
                addPara {
                    "You casually peer over the $manOrWoman's shoulder, " +
                            "reading from $hisOrHer screen."
                }
                addPara {
                    "\"The alpha core has decoded another section of the " + mark("transmission") + ". " +
                            "It mentions the location of a cache which, apparently, " +
                            "contains " + mark("Gate activation codes") + ". Absolutely incredible. We haven't shared this information, " +
                            "so there is no rush, but when possible, please retrieve the cache. " +
                            "It is located on " + mark(planetWithCache.name) +
                            " at 56.4314° N, 6.3414° W in " + mark(planetWithCache.starSystem.baseName) + ".\""
                }
            },
            options = listOf(
                Option(
                    text = { "Note the information and casually wander away." },
                    onOptionSelected = {
                        val wasQuestSuccessfullyStarted = Midgame.startQuest(dialog.interactionTarget)

                        if (wasQuestSuccessfullyStarted) {
                            it.goToPage(Page.Wander)
                        } else {
                            addPara { "After a moment's consideration, you decide that there's nothing out there after all." }
                            it.close(hideQuestOfferAfterClose = true)
                        }
                    }
                )
            )
        ),
        Page(
            id = Page.Wander,
            onPageShown = {
                addPara {
                    "You move away without the $manOrWoman noticing. " +
                            "You can't help but think that they ought to stop flashing " +
                            "such important information around at bars."
                }
            },
            options = listOf(
                Option(
                    text = { "Continue" },
                    onOptionSelected = {
                        it.close(hideQuestOfferAfterClose = true)
                    }
                )
            )
        )
    ),
    personRank = Ranks.SPACE_SAILOR
) {
    lateinit var planetWithCache: PlanetAPI

    enum class Page {
        Initial,
        Wander
    }

    override fun createInstanceOfSelf() = MidgameQuestBeginning()
}