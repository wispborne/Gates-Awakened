package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.createToken
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.empty
import org.wisp.gatesawakened.isInsideCircle
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.questLib.InteractionDefinition
import org.wisp.gatesawakened.wispLib.addPara
import kotlin.math.roundToInt


class CreateGateQuestIntel : IntelDefinition(
    title = { "Place Gate" + if (CreateGateQuest.wasQuestCompleted) " - Completed" else String.empty },
    iconPath = { "graphics/intel/g8_gate_quest.png" },
    infoCreator = { info: TooltipMakerAPI? ->
        //        info?.addPara(padding = 0f) {
//            ""
//        }
    },
    smallDescriptionCreator = { info: TooltipMakerAPI, width: Float, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        val gateSummonedTimestamp = CreateGateQuest.gateSummonedTimestamp

        if (gateSummonedTimestamp == null) {
            info.addPara {
                "A TriPad found in a cave turned out to be the key to the Gates. When it activated, signaling a nearby Gate Hauler, " +
                        "it gave you the ability to choose a location for the Gate."
            }
            info.addPara {
                "All that's left is to move to a valid location and signal the Gate Hauler."
            }
            info.addButton("Place Gate here", BUTTON_CHOOSE, width, 20f, 10f)
        } else {
            info.addPara {
                val daysUntilGateIsDelivered =
                    CreateGateQuest.numberOfDaysToDeliverGate -
                            di.sector.clock.getElapsedDaysSince(gateSummonedTimestamp)
                "A Gate Hauler and its drone fleet is en route to deliver a Gate to " +
                        mark(CreateGateQuest.summonLocation!!.containingLocation.name) +
                        " and will arrive in ${mark(Misc.getStringForDays(daysUntilGateIsDelivered.roundToInt()))}."
            }
        }
    },
    intelTags = listOf(
        Tags.INTEL_EXPLORATION,
        Tags.INTEL_STORY,
        org.wisp.gatesawakened.constants.Tags.INTEL_ACTIVE_GATE
    )
) {
    companion object {
        private const val BUTTON_CHOOSE = MOD_PREFIX + "choose"
    }
    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean = true

    override fun createConfirmationPrompt(buttonId: Any?, prompt: TooltipMakerAPI) {
        super.createConfirmationPrompt(buttonId, prompt)

        prompt.addPara {
            "Are you sure that you would like to command the waiting Gate Hauler to " +
                    mark("place a Gate here") + "?"
        }

        prompt.addPara {
            "It will take " + mark(CreateGateQuest.numberOfDaysToDeliverGate.toString()) + " days to maneuver the Gate into place " +
                    "and it cannot be stopped once started."
        }
    }

    override fun getConfirmText(buttonId: Any?): String {
        return "Place Gate"
    }

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI) {
        super.buttonPressConfirmed(buttonId, ui)

        val reasonGateCannotBePlacedAtLocation =
            reasonGateCannotBePlacedAtEntityLocation(entityToPlaceGateOn = di.sector.playerFleet.createToken())

        if (reasonGateCannotBePlacedAtLocation != null) {
            ui.showDialog(di.sector.playerFleet, CannotSummonGateHereDialog(reasonGateCannotBePlacedAtLocation).build())
        } else {
            ui.showDialog(di.sector.playerFleet, SummoningBegunDialog().build())
            CreateGateQuest.placeGateAtPlayerLocationAfterDelay()
            ui.recreateIntelUI()
        }
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        // If it's not already ending or ended and the quest was completed, mark the quest as complete
        if ((!isEnding || !isEnded) && CreateGateQuest.wasQuestCompleted) {
            endAfterDelay()
        }
    }

    private fun reasonGateCannotBePlacedAtEntityLocation(entityToPlaceGateOn: SectorEntityToken): CreateGateQuest.Rules? {
        val planets = entityToPlaceGateOn.containingLocation.planets

        return when {
            planets.any {
                // Ensure not close to a celestial body
                di.sector.playerFleet.location.isInsideCircle(
                    center = it.location,
                    radius = it.radius + 600
                )
            } -> CreateGateQuest.Rules.Proximity
            di.sector.playerFleet.isInHyperspace || di.sector.playerFleet.isInHyperspaceTransition -> CreateGateQuest.Rules.Hyperspace
            di.sector.playerFleet.starSystem.getCustomEntitiesWithTag(Tags.GATE).any() -> CreateGateQuest.Rules.MultipleGates
            else -> null
        }
    }

    private class SummoningBegunDialog :
        InteractionDefinition<SummoningBegunDialog>(
            onInteractionStarted = {
                dialog.visualPanel.showImagePortion("illustrations", "dead_gate", 640f, 400f, 0f, 0f, 480f, 300f)
                addPara { "You command the TriPad to bring a Gate to your current location." }
                addPara {
                    "A few seconds go by, and the device chimes in acknowledgement of the command. Far, far away, " +
                            "you know that the Gate Hauler has begun to move."
                }
                addPara {
                    "A Gate will be moved to your current location in " +
                            mark(Misc.getStringForDays(CreateGateQuest.numberOfDaysToDeliverGate)) +
                            "."
                }
            },
            pages = listOf(
                Page(
                    id = 0,
                    onPageShown = {},
                    options = listOf(
                        Option(
                            text = { "Close" },
                            onOptionSelected = {
                                it.close(hideQuestOfferAfterClose = false)
                            }
                        )
                    )
                )
            )
        )

    private class CannotSummonGateHereDialog(reasonToShow: CreateGateQuest.Rules) :
        InteractionDefinition<CannotSummonGateHereDialog>(
            onInteractionStarted = {
                dialog.visualPanel.showImagePortion("illustrations", "dead_gate", 640f, 400f, 0f, 0f, 480f, 300f)
                this.addPara { "You command the TriPad to bring a Gate to your current location, but it beeps at you." }
                CreateGateQuest.Rules.values()
                    .forEach {
                        this.addPara(
                            textColor = if (it == reasonToShow) Misc.getNegativeHighlightColor() else Misc.getTextColor()
                        ) { it.text }
                    }
            },
            pages = listOf(
                Page(
                    id = 0,
                    onPageShown = {},
                    options = listOf(
                        Option(
                            text = { "Dismiss" },
                            onOptionSelected = {
                                it.close(hideQuestOfferAfterClose = false)
                            }
                        )
                    )
                )
            )
        )
}