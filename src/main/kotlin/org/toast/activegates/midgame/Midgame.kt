package org.toast.activegates.midgame

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.toast.activegates.Common
import org.toast.activegates.constants.Memory
import org.toast.activegates.constants.Tags
import org.toast.activegates.constants.isBlacklisted
import org.toast.activegates.di
import org.toast.activegates.distanceFromCenter
import org.toast.activegates.intro.Intro
import org.toast.activegates.logging.i

object Midgame {
    fun hasPlanetWithCacheBeenTagged(): Boolean =
        planetWithCache != null

    fun shouldOfferQuest(market: MarketAPI): Boolean =
        market.factionId !in listOf("luddic_church", "luddic_path")
                && Intro.wasQuestCompleted
                && !hasQuestBeenStarted
                && !wasQuestCompleted
                && isMidgame()

    val planetWithCache: PlanetAPI?
        get() = Common.getSystems()
            .flatMap { it.planets }
            .singleOrNull { it.hasTag(Tags.TAG_PLANET_WITH_CACHE) }

    val hasQuestBeenStarted: Boolean
        get() = di.sector.memoryWithoutUpdate[Memory.MID_QUEST_IN_PROGRESS] == true
                || di.sector.memoryWithoutUpdate[Memory.MID_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.sector.memoryWithoutUpdate[Memory.MID_QUEST_DONE] == true

    fun findAndTagMidgameCacheLocation() {
        if (!Common.isDebugModeEnabled && hasPlanetWithCacheBeenTagged()) {
            di.logger.i { "Went to go tag planet with cache, but it's already done!" }
        }

        val cacheSystem = findRandomCacheSystemInRemnantSpace()

        if (cacheSystem == null) {
            di.logger.warn("No planet to put cache on, cannot start AG midgame quest")
            return
        }

        cacheSystem.addTag(Tags.TAG_PLANET_WITH_CACHE)
    }

    /**
     * We are defining midgame as either:
     * - Player has a large enough fleet size, or
     * - Player has an established colony.
     */
    fun isMidgame(): Boolean {
        val fleetSize = di.sector.playerFleet.fleetPoints

        if (fleetSize >= 65) {
            return true
        }

        val playerColonies = di.sector.economy.marketsCopy
            .filter { it.isPlayerOwned }

        if (playerColonies.any { it.daysInExistence >= 60 }) {
            return true
        }

        return false
    }

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
            val intel = MidgameIntel(foundAt, destination)

            if (!intel.isDone) {
                di.sector.memoryWithoutUpdate[Memory.MID_QUEST_IN_PROGRESS] = true
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
            .sortedByDescending { it.distanceFromCenter }
            .filter {
                !it.isBlacklisted
                        && it.hasTag(com.fs.starfarer.api.impl.campaign.ids.Tags.THEME_REMNANT)
                        && filterPlanets(it.planets).any()
            }
            .ifEmpty { null }
            ?.flatMap { filterPlanets(it.planets) }
            ?.random()
    }
}