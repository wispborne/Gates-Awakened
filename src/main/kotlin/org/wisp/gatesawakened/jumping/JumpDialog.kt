package org.wisp.gatesawakened.jumping


import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.wisp.gatesawakened.*
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.constants.Strings
import org.wisp.gatesawakened.constants.Tags

class JumpDialog : PaginatedOptions() {

    override fun init(dialog: InteractionDialogAPI) {
        this.dialog = dialog

        // Show the image and "The adamantine ring awaits..." text
        dialog.visualPanel.showImagePortion(
            "illustrations", "dead_gate",
            640f, 400f, 0f, 0f, 480f, 300f
        )
        dialog.textPanel.addPara(
            di.settings.getDescription(
                dialog.interactionTarget.customDescriptionId,
                Description.Type.CUSTOM
            ).text1
        )

        if (isPlayerBeingWatched()) {
            dialog.textPanel.appendPara(
                "A nearby fleet is %s your movements, making it %s to approach the Gate.",
                "tracking",
                "unwise"
            )
            addOption(Option.LEAVE.text, Option.LEAVE.id)
            showOptions()
            dialog.optionPanel.setShortcut(Option.LEAVE.id, Keyboard.KEY_ESCAPE, false, false, false, true)
            return
        } else {
            // Show the initial dialog options
            optionSelected(null, Option.INIT.id)
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        val activatedGates = Common.getGates(GateFilter.Active, excludeCurrentGate = true)

        if (optionData in Option.values().map { it.id }) {
            // If player chose a normal dialog option (not jump)

            // PaginatedOptions.options (which is different than `dialog.options`)
            // doesn't clear itself because the user may change pages without changing the total available options
            // so we want to clear it manually if we recognize a selection that is not a page change.
            options.clear()

            when (Option.values().single { it.id == optionData }) {
                Option.INIT,
                Option.RECONSIDER -> {
                    if (dialog.interactionTarget.isActive) {
                        addOption(Option.FLY_THROUGH.text, Option.FLY_THROUGH.id)
                    } else if (Common.remainingActivationCodes > 0) {
                        addOption(
                            Option.ACTIVATE.text.replace("%d", Common.remainingActivationCodes.toString()),
                            Option.ACTIVATE.id
                        )
                    }

                    addOption(Option.LEAVE.text, Option.LEAVE.id)
                }
                Option.ACTIVATE -> {
                    val wasNewGateActivated = dialog.interactionTarget.activate()

                    if (wasNewGateActivated) {
                        Common.remainingActivationCodes--
                        dialog.textPanel.appendPara(
                            "You follow the activation instructions carefully. " +
                                    "A barely perceptible energy signature is the only indication that it worked."
                        )
                    } else {
                        dialog.textPanel.appendPara(
                            "Hmm...it didn't work."
                        )
                    }

                    optionSelected(null, Option.INIT.id)
                }
                Option.FLY_THROUGH -> {
                    activatedGates.forEach { gate ->
                        addOption(
                            "Jump to ${gate.systemName} (${Common.jumpCostInFuel(gate.gate.distanceFromPlayer)} fuel)"
                            , gate.systemId
                        )
                    }

                    addOption(Option.RECONSIDER.text, Option.RECONSIDER.id)
                }
                Option.LEAVE -> dialog.dismiss()
            }
        } else if (optionData is String && optionData in activatedGates.map { it.systemId }) {
            // Player chose to jump!
            val jumpSuccessful = jumpToGate(text = dialog.textPanel, systemId = optionData)

            if (jumpSuccessful) {
                dialog.dismiss()
            }
        } else {
            // If we don't recognize the selection, let [PaginatedOptions] deal with it.
            // It's probably a Next/Previous page or an Escape
            super.optionSelected(optionText, optionData)
        }

        showOptions()

        // setShortcut changes "Leave" to "Leave (Escape)" with a yellow highlight. And of course you can press escape to leave.
        // The option needs to be present on the optionPanel before setShortcut is called, and the options are only added
        // when showOptions is called. So this has to go down here.
        dialog.optionPanel.setShortcut(Option.RECONSIDER.id, Keyboard.KEY_ESCAPE, false, false, false, true)
        dialog.optionPanel.setShortcut(Option.LEAVE.id, Keyboard.KEY_ESCAPE, false, false, false, true)

    }

    /**
     * Whether there are any nearby fleets watching the player's movements. If so, then they shouldn't be allowed to use the gate.
     * Logic adapted from [com.fs.starfarer.api.impl.campaign.rulecmd.salvage.HostileFleetNearbyAndAware].
     */
    private fun isPlayerBeingWatched(): Boolean {
        val playerFleet = di.sector.playerFleet

        val fleetsWatchingPlayer = playerFleet.containingLocation.fleets
            .filter { nearbyFleet ->
                nearbyFleet.ai != null
                        && !nearbyFleet.faction.isPlayerFaction
                        && nearbyFleet.battle == null
                        && playerFleet.getVisibilityLevelTo(nearbyFleet) != SectorEntityToken.VisibilityLevel.NONE
                        && nearbyFleet.fleetData.membersListCopy.isNotEmpty()
                        && Misc.getDistance(playerFleet.location, nearbyFleet.location) <= 750f
            }

        return fleetsWatchingPlayer.isNotEmpty()
    }

    private fun jumpToGate(
        text: TextPanelAPI,
        systemId: String
    ): Boolean {
        // Can only jump using activated gates
        // We shouldn't even see this dialog if the gate isn't activated, but still.
        if (!dialog.interactionTarget.isActive) {
            text.addPara(Strings.flyThroughInactiveGate)
            text.addPara(Strings.resultWhenGateDoesNotWork)
            return false
        }

        if (systemId.isBlank()) return false

        val newSystem = Common.getSystems().firstOrNull { it.id == systemId }

        if (newSystem == null) {
            text.addPara(Strings.errorCouldNotFindJumpSystem(systemId))
            return false
        }

        // Jump player fleet to new system
        val gates = newSystem.getEntitiesWithTag(Tags.TAG_GATE_ACTIVATED)

        return when (val result = Jump.jumpPlayer(gates.first())) {
            is Jump.JumpResult.Success -> {
                text.addPara(Strings.flyThroughActiveGate)
                true
            }
            is Jump.JumpResult.FuelRequired -> {
                text.addPara(Strings.notEnoughFuel(result.fuelCost))
                false
            }
        }
    }

    override fun doesCommandAddOptions(): Boolean = true

    /**
     * [PaginatedOptions] requires a String id, so we can't just use [Option] itself as the option data
     */
    enum class Option(val text: String, val id: String) {
        INIT("", "${MOD_PREFIX}init"),
        FLY_THROUGH("Fly through the gate", "${MOD_PREFIX}fly_through"),
        ACTIVATE("Use an activation code (%d left)", "${MOD_PREFIX}activate_gate"),
        RECONSIDER("Reconsider", "${MOD_PREFIX}reconsider"),
        LEAVE("Leave", "${MOD_PREFIX}leave")
    }
}