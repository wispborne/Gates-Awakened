package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.BarEventDefinition
import org.wisp.gatesawakened.wispLib.CrashReporter

/**
 * Creates the intro quest at the bar.
 */
class IntroBarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent() = IntroQuestBeginning().buildBarEvent()
}

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
                    "and it looks like $heOrShe is staring at an image of a ${mark("Gate")}."
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
                if (!isNaziScumAlive) {
                    addPara {
                        "As soon as you get close, " +
                                "$heOrShe flips off $hisOrHer tripad and quickly rushes out."
                    }
                    addPara {
                        "However, just before $hisOrHer tripad goes dark, you catch one line: " + mark(destinationSystem!!.name)
                    }
                } else {
                    displayNgoInteraction()
                }
            },
            options = listOf(
                Option(
                    showIf = { !isNaziScumAlive },
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
                ),
                Option(
                    showIf = { isNaziScumAlive },
                    text = { "Leave" },
                    onOptionSelected = { it.close(hideQuestOfferAfterClose = true) }
                )
            )
        )
    ),
    personRank = Ranks.SPACE_SAILOR
) {
    private var destinationSystem: StarSystemAPI? = null

    private val errorReporter: CrashReporter
        get() = di.errorReporter

    private val isNaziScumAlive: Boolean
        get() = di.settings.modManager.isModEnabled("new_galactic_order")
                && Misc.getFactionMarkets(di.sector.getFaction("new_galactic_order")).any()

    override fun createInstanceOfSelf() = IntroQuestBeginning()
}

/**
 * If you're reading this, there's a good chance you dislike the fact that this mod refuses to work if NGO owns any worlds.
 * That's fine. This mod, like most mods, was developed in my free time and released for free.
 * It is also licensed under the Apache 2 license, which means that it's perfectly fine to set "isNaziScumAlive = false"
 * and then release a new version under your name. Put it on your pastebin or wherever. It's not my job to police how you play
 * your game; use whatever mods you want and edit them as you like.
 *
 * The reason that this NGO interaction exists is captured well here: [https://twitter.com/HoldenShearer/status/1212727688085090305]
 * [Archived version](https://web.archive.org/web/20200104121345/https://twitter.com/HoldenShearer/status/1212727688085090305)
 *
 * This may seem like creating a Streisand Effect, but it's not intentionally so.
 * The goal is to step toward normalizing the rejection of extreme hate. This is not a slippery slope.
 * Disagreements with other mod authors or other minor squabbles can be dealt with without affecting users.
 * Allowing mods with Nazi idolisation to be part of the Starsector modiverse is far enough off the slippery slope as to be another mountain.
 */
private fun IntroQuestBeginning.displayNgoInteraction() {
    addPara {
        "As you move closer, the $manOrWoman silently collapses forward, their TriPad flickering out. You spin around to see a " +
                "man quickly pocketing a gun and leaving, inconspicuous but for his shaved head " +
                "marred by a tattoo of a cross with bent arms - the unofficial symbol of the New Galactic Order - and " +
                "by the words across the back of his jacket;"
    }
    addPara { "" }
    addPara(textColor = Misc.getHighlightColor()) { "\"War is peace." }
    addPara(textColor = Misc.getHighlightColor()) { "Freedom is slavery." }
    addPara(textColor = Misc.getHighlightColor()) { "Ignorance is strength\"" }
    addPara { "" }
    addPara {
        "You get the sense that a sector where the NGO holds any power is incompatible with free scientific pursuit " +
                "or individual exploration."
    }
}