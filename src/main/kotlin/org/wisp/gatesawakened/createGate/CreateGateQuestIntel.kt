package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.createToken
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.isInsideCircle
import org.wisp.gatesawakened.questLib.IntelDefinition
import org.wisp.gatesawakened.questLib.InteractionDefinition
import org.wisp.gatesawakened.show
import org.wisp.gatesawakened.wispLib.addPara
import kotlin.math.roundToInt


class CreateGateQuestIntel : IntelDefinition(
    title = "The Reach",
    iconPath = "graphics/intel/g8_gate.png",
    infoCreator = { info: TooltipMakerAPI? ->
        info?.addPara(padding = 0f, textColor = Misc.getHighlightColor()) {
            "Gate Hauler Detected"
        }
    },
    smallDescriptionCreator = { info: TooltipMakerAPI, width: Float, _ ->
        info.addImage(di.settings.getSpriteName("illustrations", "dead_gate"), width, 10f)
        val gateSummonedTimestamp = CreateGateQuest.gateSummonedTimestamp

        if (gateSummonedTimestamp == null) {
            info.addPara {
                "A Gate Hauler is standing by, waiting for you to choose a location for a new Gate."
            }
            info.addButton("Place Gate here", BUTTON_CHOOSE, width, 20f, 10f)
        } else {
            info.addPara {
                val daysUntilGateIsDelivered =
                    CreateGateQuest.numberOfDaysToDeliverGate -
                            di.sector.clock.getElapsedDaysSince(gateSummonedTimestamp)
                "A Gate Hauler is en route to deliver a Gate to " + mark(CreateGateQuest.summonLocation!!.containingLocation.name) +
                        " and will arrive in " + mark(Misc.getStringForDays(daysUntilGateIsDelivered.roundToInt())) + "."
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

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        super.buttonPressConfirmed(buttonId, ui)

        val reasonGateCannotBePlacedAtLocation =
            reasonGateCannotBePlacedAtEntityLocation(entityToPlaceGateOn = di.sector.playerFleet.createToken())

        if (reasonGateCannotBePlacedAtLocation != null) {
            // TODO note working
            di.sector.campaignUI.currentInteractionDialog?.dismiss()

            CannotSummonGateHereDialog(reasonGateCannotBePlacedAtLocation).build()
                .show(di.sector.campaignUI, di.sector.playerFleet)
        } else {
            CreateGateQuest.placeGateAtPlayerLocationAfterDelay()
            ui?.recreateIntelUI()
        }
    }

    private fun reasonGateCannotBePlacedAtEntityLocation(entityToPlaceGateOn: SectorEntityToken): String? {
        val planets = entityToPlaceGateOn.containingLocation.planets

        if (planets.any {
                // Ensure not close to a celestial body
                di.sector.playerFleet.location.isInsideCircle(
                    center = it.location,
                    radius = it.radius + 600
                )
            }) {
            return "the Gate cannot be placed so close to a celetial body"
        }

        if (di.sector.playerFleet.isInHyperspace || di.sector.playerFleet.isInHyperspaceTransition) {
            return "a Gate cannot be placed in hyperspace"
        }

        if (di.sector.playerFleet.starSystem.getCustomEntitiesWithTag(Tags.GATE).any()) {
            return "there is already a Gate in this system"
        }

        return null
    }

    private class CannotSummonGateHereDialog(reasonToShow: String) : InteractionDefinition<CannotSummonGateHereDialog>(
        onInteractionStarted = {
            this.addPara { "You command the TriPad to bring a Gate to your current location." }
            this.addPara(highlightColor = Misc.getNegativeHighlightColor()) {
                "The screen flashes red and it informs you that " + mark(reasonToShow) + "."
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


    override fun isDone(): Boolean = CreateGateQuest.wasQuestCompleted
    override fun isEnded(): Boolean = CreateGateQuest.wasQuestCompleted
}