package org.toast.activegates

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.thoughtworks.xstream.XStream
import org.toast.activegates.constants.Strings
import org.toast.activegates.constants.Tags
import org.toast.activegates.intro.Intro
import org.toast.activegates.intro.IntroBarEventCreator
import org.toast.activegates.intro.IntroIntel
import org.toast.activegates.intro.IntroQuestBeginning
import org.toast.activegates.logging.i
import org.toast.activegates.midgame.Midgame
import org.toast.activegates.midgame.MidgameBarEventCreator
import org.toast.activegates.midgame.MidgameQuestBeginning

class LifecyclePlugin : BaseModPlugin() {

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

        if (!Intro.haveGatesBeenTagged()) {
            Intro.findAndTagIntroGatePair()
        }

        if (!Midgame.hasPlanetWithCacheBeenTagged()) {
            Midgame.findAndTagMidgameCacheLocation()
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        // When the game (re)loads, we want to grab the new instances of everything, especially the new sector.
        di = Di()

        // Keep track of what gates this mod can interact with
        // Other mods may blacklist systems at will.
        applyBlacklistTagsToSystems()

        val bar = BarEventManager.getInstance()

        // Intro quest
        if (!Intro.haveGatesBeenTagged()) {
            Intro.findAndTagIntroGatePair()
        }

        if (Intro.haveGatesBeenTagged()
            && !Intro.hasQuestBeenStarted
            && !bar.hasEventCreator(IntroBarEventCreator::class.java)
        ) {
            bar.addEventCreator(IntroBarEventCreator())
        }

        // Midgame quest
        if (!Midgame.hasPlanetWithCacheBeenTagged()) {
            Midgame.findAndTagMidgameCacheLocation()
        }

        if (Midgame.hasPlanetWithCacheBeenTagged()
            && !Midgame.hasQuestBeenStarted
            && !bar.hasEventCreator(MidgameBarEventCreator::class.java)
        ) {
            bar.addEventCreator(MidgameBarEventCreator())
        }

        // Register this so we can intercept and replace interactions, such as with a gate
        di.sector.registerPlugin(CampaignPlugin())
    }

    override fun beforeGameSave() {
        val intel = di.sector.intelManager.getIntel(IntroIntel::class.java)

        if (intel != null) {
//            Di.inst.sector.intelManager.removeIntel(IntroIntel::class.java)
        }

        super.beforeGameSave()
    }

    private fun applyBlacklistTagsToSystems() {
        val blacklistedSystems = try {
            val jsonArray = di.settings
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
            di.logger.error(e.message, e)
            emptyList<BlacklistEntry>()
        }

        val systems = di.sector.starSystems

        // Mark all blacklisted systems as blacklisted, remove tags from ones that aren't
        for (system in systems) {
            if (blacklistedSystems.any { it.systemId == system.id }) {
                di.logger.i { "Blacklisting system: ${system.id}" }
                system.addTag(Tags.TAG_BLACKLISTED_SYSTEM)
            } else {
                system.removeTag(Tags.TAG_BLACKLISTED_SYSTEM)
            }
        }
    }

    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)

        // DO NOT CHANGE THESE STRINGS, DOING SO WILL BREAK SAVE GAMES
        val aliases = listOf(
            IntroIntel::class to "IntroIntel",
            IntroQuestBeginning::class to "IntroBarEvent",
            IntroBarEventCreator::class to "IntroBarEventCreator",
            MidgameQuestBeginning::class to "MidgameQuestBeginning",
            MidgameBarEventCreator::class to "MidgameBarEventCreator",
            CampaignPlugin::class to "CampaignPlugin"
        )

        // Prepend "g8_" so the classes don't conflict with anything else getting serialized
        aliases.forEach { x.alias("g8_${it.second}", it.first.java) }
    }

    private data class BlacklistEntry(
        val id: String,
        val systemId: String,
        val isBlacklisted: Boolean = true,
        val priority: Int? = 0
    )
}