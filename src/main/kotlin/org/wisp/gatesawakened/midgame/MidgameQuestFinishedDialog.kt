package org.wisp.gatesawakened.midgame

import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.createGate.CreateGateQuest
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.questLib.InteractionDefinition

class MidgameQuestFinishedDialog : InteractionDefinition<MidgameQuestFinishedDialog>(
    onInteractionStarted = {
    },
    pages = listOf(
        Page(
            id = 1,
            image = Image("illustrations", "survey", 640f, 400f, 0f, 0f, 480f, 300f),
            onPageShown = {
                addPara {
                    "Squarely located at the coordinates from the decrypted transmission is a small island." +
                            " Visible from the ground is an awe-inspiring cave entrance." +
                            " Natural basalt pillars line the walls, their hexagonal edges overgrown with luminescent moss." +
                            " The only sign that your crew is not the first to step foot here is the TriPad" +
                            " almost casually placed on top of a pillar deep in the cave."
                }
                addPara {
                    "It contains detailed, but slightly foreboding, instructions on how to reactivate and deactivate " +
                            "\"carefully considered Gates\", \"should my mission be successful\"."
                }

                addPara {
                    "At the very end is a list of ${Midgame.midgameRewardActivationCodeCount} activation codes" +
                            " and the writer's signature: \"Ludd\"."
                }
            },
            options = listOf(
                Option(
                    text = { "Take the data" },
                    onOptionSelected = {
                        Midgame.remainingActivationCodes = Midgame.midgameRewardActivationCodeCount

                        di.memory[Memory.MID_QUEST_DONE] = true
                        addPara { mark("You may now activate up to ${Midgame.remainingActivationCodes} Gates of your choosing.") }
                        addPara { "Active gates may be deactivated to reclaim a code." }
                        it.goToPage(2)
                    }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
            },
            options = listOf(
                Option(
                    text = { "Look around some more" },
                    showIf = { !hasRock },
                    onOptionSelected = {
                        addPara { "You find a cool-looking rock. You put it into your pocket to bring back." }
                        addPara { "However, while it is very neat, it is just a normal rock." }
                        hasRock = true
                        it.goToPage(2)
                    }
                ),
                Option(
                    text = { "Continue" },
                    onOptionSelected = { it.goToPage(3) }
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                if (CreateGateQuest.isEndgame()) {
                    addPara {
                        "You tell your communications specialist to keep an eye out for anything strange as you use the Gates. " +
                                "You have no idea what other surprises the TriPad has in store."
                    }
                } else {
                    addPara {
                        "It may take a while, but you suspect that the little TriPad hasn't given up all of its surprises yet. " +
                                "You resolve to keep an eye on it as your influence grows and you rely more and more on the Gates."
                    }
                }
            },
            options = listOf(
                Option(
                    text = { "Exit the cave" },
                    onOptionSelected = {
                        di.sector.isPaused = false
                        dialog.dismiss()
                    }
                )
            )
        )
    )
) {
    @Transient
    var hasRock = false

    override fun createInstanceOfSelf() = MidgameQuestFinishedDialog()
}