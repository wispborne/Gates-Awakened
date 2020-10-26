package org.wisp.gatesawakened.midgame

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.intro.IntroQuest
import org.wisp.gatesawakened.logging.i

object Midgame {
    const val PREREQ_FLEET_POINTS = 65
    const val PREREQ_COLONIES = 1
    const val PREREQ_COLONY_ESTABLISHED_DAYS = 60

    fun hasPlanetWithCacheBeenTagged(): Boolean =
        planetWithCache != null

    fun shouldOfferQuest(market: MarketAPI): Boolean =
        market.factionId !in listOf("luddic_church", "luddic_path")
                && IntroQuest.wasQuestCompleted
                && !hasQuestBeenStarted
                && !wasQuestCompleted
                && isMidgame()
                && planetWithCache != null
                && midgameRewardActivationCodeCount > 0 // Player can disable this quest by setting to 0

    val planetWithCache: PlanetAPI?
        get() = Common.getSystems()
            .flatMap { it.planets }
            .singleOrNull { it.hasTag(Tags.TAG_PLANET_WITH_CACHE) }

    val hasQuestBeenStarted: Boolean
        get() = di.memory[Memory.MID_QUEST_IN_PROGRESS] == true
                || di.memory[Memory.MID_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.memory[Memory.MID_QUEST_DONE] == true

    val midgameRewardActivationCodeCount = di.settings.getInt("GatesAwakened_midgameQuestRewardCodeCount")

    var remainingActivationCodes: Int
        get() = di.memory[Memory.GATE_ACTIVATION_CODES_REMAINING] as? Int ?: 0
        set(value) {
            di.memory[Memory.GATE_ACTIVATION_CODES_REMAINING] = value
        }

    fun findAndTagMidgameCacheLocation() {
        if (!Common.isDebugModeEnabled && hasPlanetWithCacheBeenTagged()) {
            di.logger.i { "Went to go tag planet with cache, but it's already done!" }
        }

        val cacheSystem = findRandomCacheSystemInRemnantSpace()

        if (cacheSystem == null) {
            di.logger.warn("No planet to put cache on, cannot start Gates Awakened midgame quest")
            return
        }

        cacheSystem.addTag(Tags.TAG_PLANET_WITH_CACHE)
    }

    /**
     * We are defining midgame as either:
     * - Player has a large enough fleet, or
     * - Player has an established colony.
     */
    fun isMidgame(): Boolean =
        Common.playerFleetPoints >= PREREQ_FLEET_POINTS || Common.establishedPlayerColonyCount(
            PREREQ_COLONY_ESTABLISHED_DAYS
        ) > PREREQ_COLONIES

    /**
     * Attempts to start the midgame quest by adding player intel.
     * Called when the player has interacted with [MidgameQuestBeginning].
     *
     * @return true if successful, false otherwise
     */
    fun startQuest(foundAt: SectorEntityToken): Boolean {
        val destination = planetWithCache
        var success = false

        if (destination != null && !hasQuestBeenStarted) {
            val intel = MidgameIntel(destination)

            if (!intel.isDone) {
                di.memory[Memory.MID_QUEST_IN_PROGRESS] = true
                di.sector.intelManager.addIntel(intel)

                success = true
            }
        }

        return success
    }

    private fun findRandomCacheSystemInRemnantSpace(): PlanetAPI? {
        fun filterPlanets(planets: List<PlanetAPI>) = planets.filter { planet ->
            !planet.isGasGiant
                    && !planet.isStar
                    && !planet.isMoon
        }

        return Common.getSystems()
            .filter {
                it.hasTag(com.fs.starfarer.api.impl.campaign.ids.Tags.THEME_REMNANT)
                        && filterPlanets(it.planets).any()
            }
            .ifEmpty {
                Common.getSystems()
                    .filter {
                        it.hasTag(com.fs.starfarer.api.impl.campaign.ids.Tags.THEME_DERELICT)
                                && filterPlanets(it.planets).any()
                    }
            }
            .ifEmpty {
                Common.getSystems()
                    .filter { filterPlanets(it.planets).any() }
            }
            .ifEmpty { null }
            ?.flatMap { filterPlanets(it.planets) }
            ?.random()
    }
}