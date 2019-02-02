package org.toast.activegates.jumping


import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.toast.activegates.*
import org.toast.activegates.constants.MOD_PREFIX
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags

class JumpDialog : PaginatedOptions() {

    override fun init(dialog: InteractionDialogAPI) {
        this.dialog = dialog

        // Show the image and "The adamantine ring awaits..." text
        dialog.visualPanel.showImagePortion("illustrations", "dead_gate", 640f, 400f, 0f, 0f, 480f, 300f)
        dialog.textPanel.addPara(
            Di.inst.settings.getDescription(
                dialog.interactionTarget.customDescriptionId,
                Description.Type.CUSTOM
            ).text1
        )

        if (isPlayerBeingWatched()) {
            dialog.textPanel.addPara(
                "A nearby fleet is %s your movements, making it %s to use the Gate.",
                "tracking",
                "unwise"
            )
            addOption(Option.LEAVE.text, Option.LEAVE.id)
            showOptions()
            dialog.optionPanel.setShortcut(Option.LEAVE.id, Keyboard.KEY_ESCAPE, false, false, false, true)
            return
        }

        // Show the initial dialog options
        optionSelected(null, Option.INIT.id)
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
                    addOption(Option.FLY_THROUGH.text, Option.FLY_THROUGH.id)
                    addOption(Option.LEAVE.text, Option.LEAVE.id)
                }
                Option.FLY_THROUGH -> {
                    activatedGates.forEach { gate ->
                        addOption(
                            Strings.menuOptionJumpToSystem(
                                systemName = gate.systemName,
                                jumpCostInFuel = Common.jumpCostInFuel(gate.distanceFromPlayer)
                            ), gate.systemId
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
        val playerFleet = Di.inst.sector.playerFleet

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

        val playerFleet = Di.inst.sector.playerFleet

        // Pay fuel cost (or show error if player lacks fuel)
        val cargo = playerFleet.cargo
        val fuelCostOfJump = Common.jumpCostInFuel(
            Misc.getDistanceLY(
                playerFleet.locationInHyperspace,
                newSystem.location
            )
        )

        if (cargo.fuel >= fuelCostOfJump) {
            cargo.removeFuel(fuelCostOfJump.toFloat())
        } else {
            text.addPara(Strings.notEnoughFuel(fuelCostOfJump))
            return false
        }

        // Jump player fleet to new system
        val gates = newSystem.getEntitiesWithTag(Tags.TAG_GATE_ACTIVATED)
        Jump.jumpPlayer(gates.first())

        text.addPara(Strings.flyThroughActiveGate)

        return true
    }

    override fun doesCommandAddOptions(): Boolean = true

    /**
     * [PaginatedOptions] requires a String id, so we can't just use [Option] itself as the option data
     */
    enum class Option(val text: String, val id: String) {
        INIT("", "${MOD_PREFIX}init"),
        FLY_THROUGH("Fly through the gate", "${MOD_PREFIX}fly_through"),
        RECONSIDER("Reconsider", "${MOD_PREFIX}reconsider"),
        LEAVE("Leave", "${MOD_PREFIX}leave")
    }
}