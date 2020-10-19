package org.wisp.gatesawakened.gateIntel

import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.*
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.intro.IntroQuest
import org.wisp.gatesawakened.wispLib.PersistentData

object GateIntelCommon {
    var discoveredInactiveGates: List<Gate> by PersistentData("discovered_inactive_gates", defaultValue = emptyList())
        private set

    var shouldShowInactiveGateIntel: Boolean by PersistentData("should_show_inactive_gate_intel", defaultValue = true)

    fun listenForWhenPlayerDiscoversNewGates() {
        di.sector.addTransientListener(object : BaseCampaignEventListener(false) {
            override fun reportFleetJumped(
                fleet: CampaignFleetAPI?,
                from: SectorEntityToken?,
                to: JumpPointAPI.JumpDestination?
            ) {
                to?.destination?.starSystem ?: return

                if (fleet == di.sector.playerFleet) {
                    val gatesToAppendToList =
                        to.destination.starSystem.getEntitiesWithTag(Tags.TAG_GATE)
                            .filterNot { it.isActive }
                            .filter { it !in discoveredInactiveGates }

                    updateDiscoveredInactiveGates(gatesToAppendToList)
                    updateInactiveGateIntel()
                }
            }
        })
    }

    /**
     * Updates the list of inactive gates that the player has discovered.
     * If they've just discovered one, pass it as a parameter and it will be appended to the list
     * (rather than addedat an arbitrary position in the list).
     * Don't forget to call [updateActiveGateIntel] afterwards!
     */
    fun updateDiscoveredInactiveGates(newlyDiscoveredInactiveGatesToAppend: List<SectorEntityToken>?) {
        // This will pick up the newly-discovered gate(s), too.
        // Filter those out so we can add them to the end of the list,
        // rather than having them in a "random" place in it.
        val knownInactiveGatesOutsideCurrentSystem = getAllExploredInactiveGates()
            .filter { exploredGate -> exploredGate !in (discoveredInactiveGates) }
            .minus(newlyDiscoveredInactiveGatesToAppend.orEmpty())


        discoveredInactiveGates = discoveredInactiveGates
            .plus(knownInactiveGatesOutsideCurrentSystem)
            .plus(newlyDiscoveredInactiveGatesToAppend.orEmpty())
            .distinct()
            .filterNot { it.isActive }

    }

    fun updateActiveGateIntel() {
        val activeGates = Common.getGates(GateFilter.Active, excludeCurrentGate = false).map { it.gate }

        val currentGateIntels = di.intelManager.getIntel(ActiveGateIntel::class.java)
            .filterIsInstance(ActiveGateIntel::class.java)

        // Add intel for gates that don't have any
        activeGates
            .filter { it !in currentGateIntels.map { activeIntel -> activeIntel.activeGate } }
            .forEach { di.intelManager.addIntel(ActiveGateIntel(it)) }

        // Remove intel for gates that aren't in the active list
        currentGateIntels
            .filter { it.activeGate !in activeGates }
            .forEach { di.intelManager.removeIntel(it) }
    }

    /**
     * Reads list of `InactiveGates.playerDiscoveredGates` and updates player `Intel` based on it.
     */
    fun updateInactiveGateIntel() {
        if (!IntroQuest.wasQuestCompleted) return

        val inactiveGates = discoveredInactiveGates

        val currentGateIntels = di.intelManager.getIntel(InactiveGateIntel::class.java)
            .filterIsInstance(InactiveGateIntel::class.java)

        if (shouldShowInactiveGateIntel) {        // Add intel for gates that don't have any
            inactiveGates
                .filter { it !in currentGateIntels.map { activeIntel -> activeIntel.inactiveGate } }
                .forEach { di.intelManager.addIntel(InactiveGateIntel(it)) }

            // Remove intel for gates that aren't in the active list
            currentGateIntels
                .filter { it.inactiveGate !in inactiveGates }
                .forEach { di.intelManager.removeIntel(it) }
        } else {
            // Remove all inactive gate intel
            currentGateIntels
                .forEach { di.intelManager.removeIntel(it) }
        }
    }

    private fun getAllExploredInactiveGates(): List<Gate> =
        di.sector.starSystems
            .filter { system -> Misc.getMinSystemSurveyLevel(system) == MarketAPI.SurveyLevel.FULL }
            .flatMap { system -> system.getEntitiesWithTag(Tags.TAG_GATE) }
            .filterNot { gate -> gate.isActive }
}