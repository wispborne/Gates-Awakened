package org.wisp.gatesawakened

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.thoughtworks.xstream.XStream
import org.wisp.gatesawakened.activeGateIntel.ActiveGateIntel
import org.wisp.gatesawakened.constants.Strings
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.createGate.CreateGateQuestIntel
import org.wisp.gatesawakened.createGate.CreateGateQuestStart
import org.wisp.gatesawakened.intro.Intro
import org.wisp.gatesawakened.intro.IntroBarEventCreator
import org.wisp.gatesawakened.intro.IntroIntel
import org.wisp.gatesawakened.intro.IntroQuestBeginning
import org.wisp.gatesawakened.logging.i
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.midgame.MidgameBarEventCreator
import org.wisp.gatesawakened.midgame.MidgameIntel
import org.wisp.gatesawakened.midgame.MidgameQuestBeginning
import org.wisp.gatesawakened.questLib.BarEventDefinition

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

        fixBugWithBarCreatorDurationTooHigh(bar)

        // Intro quest
        if (!Intro.haveGatesBeenTagged()) {
            Intro.findAndTagIntroGatePair()
        }

        // Midgame quest
        if (!Midgame.hasPlanetWithCacheBeenTagged()) {
            Midgame.findAndTagMidgameCacheLocation()
        }

        addQuestStarts()

        adjustPlayerActivationCodesToMatchSettings()

        Common.updateActiveGateIntel()

        // Register this so we can intercept and replace interactions, such as with a gate
        di.sector.registerPlugin(CampaignPlugin())
    }

    override fun afterGameSave() {
        super.afterGameSave()
        addQuestStarts()
    }

    override fun beforeGameSave() {
        super.beforeGameSave()

        // Remove quest bar events so they don't get into save file.
        // It's a pain to migrate after refactoring and they are stateless
        // so there's no reason for them to be in the save file.
        val bar = BarEventManager.getInstance()
        bar.creators.removeAll { it is IntroBarEventCreator || it is MidgameBarEventCreator }
        bar.active.items
            .filterIsInstance<BarEventDefinition<*>.BarEvent>()
            .forEach { bar.active.remove(it) }
    }

    private fun addQuestStarts() {
        val bar = BarEventManager.getInstance()

        if (Intro.haveGatesBeenTagged()
            && !Intro.hasQuestBeenStarted
            && !bar.hasEventCreator(IntroBarEventCreator::class.java)
        ) {
            bar.addEventCreator(IntroBarEventCreator())
        }

        if (Midgame.hasPlanetWithCacheBeenTagged()
            && !Midgame.hasQuestBeenStarted
            && !bar.hasEventCreator(MidgameBarEventCreator::class.java)
        ) {
            bar.addEventCreator(MidgameBarEventCreator())
        }
    }

    private fun applyBlacklistTagsToSystems() {
        val blacklistedSystems = try {
            val jsonArray = di.settings
                .getMergedSpreadsheetDataForMod(
                    "id", "data/gates-awakened/gates_awakened_system_blacklist.csv",
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
            MidgameIntel::class to "MidgameIntel",
            CreateGateQuestStart::class to "CreateGateQuestStart",
            CreateGateQuestIntel::class to "CreateGateQuestIntel",
            ActiveGateIntel::class to "ActiveGateIntel",
            CampaignPlugin::class to "CampaignPlugin"
        )

        // Prepend "g8_" so the classes don't conflict with anything else getting serialized
        aliases.forEach { x.alias("g8_${it.second}", it.first.java) }
    }

    /**
     * The player may choose how many activates codes they want as a reward, so we should add/remove
     * codes to match that, subtracting ones they've already used to activate gates.
     */
    private fun adjustPlayerActivationCodesToMatchSettings() {
        val numberOfEarlyQuestGates = 2
        val activeGateCount = Common.getGates(GateFilter.Active, excludeCurrentGate = false)
            .filter { it.gate.canBeDeactivated }
            .count()
        val currentTotalActivateCodeCount = activeGateCount + Midgame.remainingActivationCodes
        val expectedTotalActivationCodeCount = Midgame.midgameRewardActivationCodeCount + numberOfEarlyQuestGates

        if (Midgame.wasQuestCompleted && currentTotalActivateCodeCount != expectedTotalActivationCodeCount
        ) {
            Midgame.remainingActivationCodes =
                (expectedTotalActivationCodeCount - activeGateCount)
                    .coerceAtLeast(minimumValue = 0)
        }
    }

    /**
     * Fixed in 1.1.
     *
     * Early and Mid quests originally had a super long duration so they never respawned after you initially saw them.
     * Fix by causing them to expire after a day, then they'll be re-added on the next load.
     */
    private fun fixBugWithBarCreatorDurationTooHigh(bar: BarEventManager) {
        if (bar.creators
                .filterIsInstance(IntroBarEventCreator::class.java)
                .any()
        ) {
            bar.setTimeout(IntroBarEventCreator::class.java, 1f)
        }

        if (bar.creators
                .filterIsInstance(MidgameBarEventCreator::class.java)
                .any()
        ) {
            bar.setTimeout(MidgameBarEventCreator::class.java, 1f)
        }
    }

    private data class BlacklistEntry(
        val id: String,
        val systemId: String,
        val isBlacklisted: Boolean = true,
        val priority: Int? = 0
    )
}
