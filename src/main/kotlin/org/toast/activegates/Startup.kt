package org.toast.activegates

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Tags

class Startup : BaseModPlugin() {

    /** Vanilla tag for a gate.
     * We only want to touch gates that aren't blacklisted, so throughout this mod,
     * only get references to gates via TAG_GATE_CANDIDATE.
     **/
    private val TAG_GATE = Tags.GATE

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        tagPossibleGateDestinations()
    }

    private fun tagPossibleGateDestinations() {
        val blacklistedSystems = try {
            val jsonArray = Global.getSettings()
                .getMergedSpreadsheetDataForMod(
                    "id", "data/active-gates/active-gates_system_blacklist.csv",
                    Strings.modName
                )
            val blacklist = mutableListOf<BlacklistEntry>()

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)

                blacklist += BlacklistEntry(
                    id = jsonObj.getString("id"),
                    systemId = jsonObj.getString("systemId"),
                    isBlacklisted = jsonObj.optBoolean("isBlacklisted", true),
                    priority = jsonObj.optInt("priority", 0)
                )
            }

            // Sort so that the highest priorities are first
            // Then run distinctBy, which will always keep only the first element it sees for a key
            blacklist
                .sortedByDescending { it.priority }
                .distinctBy { it.systemId }
        } catch (e: Exception) {
            Global.getLogger(this::class.java).error(e.message, e)
            emptyList<BlacklistEntry>()
        }

        // Log the blacklist
        blacklistedSystems
            .filter { it.isBlacklisted == true }
            .forEach {
                Global.getLogger(this::class.java).debug("Blacklisting system: ${it.systemId}")
            }

        // Get all systems, then remove ones that are marked "true" on the blacklist.
        // Then get all gates in the remaining systems and add the "gate candidate" tag to them.
        Global.getSector().starSystems
            .flatMap { it.getEntitiesWithTag(TAG_GATE) }
            .forEach { gate ->
                val blacklistEntryForGate = blacklistedSystems.singleOrNull { it.systemId == gate.starSystem.id }

                if (blacklistEntryForGate?.isBlacklisted == true) {
                    gate.removeTag(ActiveGates.TAG_GATE_CANDIDATE)
                } else {
                    gate.addTag(ActiveGates.TAG_GATE_CANDIDATE)
                }
            }
    }

    private data class BlacklistEntry(
        val id: String,
        val systemId: String,
        val isBlacklisted: Boolean? = true,
        val priority: Int? = 0
    )
}