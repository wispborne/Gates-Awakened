package org.toast.activegates

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import org.toast.activegates.intro.Intro
import org.toast.activegates.intro.IntroBarEventCreator
import org.toast.activegates.intro.IntroIntel

class ActiveGatesLifecyclePlugin : BaseModPlugin() {

    init {
//        configureXStream()
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

        if (!Intro.haveGatesBeenTagged()) {
            Intro.findAndTagIntroGatePair()
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        tagPossibleGateDestinations()

        val bar = BarEventManager.getInstance()

        if (Common.isDebugModeEnabled && !Intro.haveGatesBeenTagged()) {
            Intro.findAndTagIntroGatePair()
        }

        if (Intro.haveGatesBeenTagged() && !bar.hasEventCreator(IntroBarEventCreator::class.java)) {
            bar.addEventCreator(IntroBarEventCreator())
        }
    }

    override fun beforeGameSave() {
        val intel = Di.inst.sector.intelManager.getIntel(IntroIntel::class.java)

        if (intel != null) {
//            Di.inst.sector.intelManager.removeIntel(IntroIntel::class.java)
        }

        super.beforeGameSave()
    }

    private fun tagPossibleGateDestinations() {
        val blacklistedSystems = try {
            val jsonArray = Di.inst.settings
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
                .filter { it.isBlacklisted }
        } catch (e: Exception) {
            Di.inst.logger.error(e.message, e)
            emptyList<BlacklistEntry>()
        }

        val systems = Di.inst.sector.starSystems

        // Mark all blacklisted systems as blacklisted, remove tags from ones that aren't
        for (system in systems) {
            if (blacklistedSystems.any { it.systemId == system.id }) {
                Di.inst.logger.debug("Blacklisting system: ${system.id}")
                system.addTag(Tags.TAG_BLACKLISTED_SYSTEM)
            } else {
                system.removeTag(Tags.TAG_BLACKLISTED_SYSTEM)
            }
        }
    }

    private data class BlacklistEntry(
        val id: String,
        val systemId: String,
        val isBlacklisted: Boolean = true,
        val priority: Int? = 0
    )
}