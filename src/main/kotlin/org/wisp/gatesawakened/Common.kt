package org.wisp.gatesawakened

import com.fs.starfarer.api.campaign.OrbitAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.wisp.gatesawakened.activeGateIntel.ActiveGateIntel
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.constants.isBlacklisted
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A collection of information and stateless utility methods.
 */
internal object Common {
    val isDebugModeEnabled: Boolean
        get() = di.settings.getBoolean("GatesAwakened_Debug")

    private val fuelCostPerLY: Float
        get() {
            val fuelMultiplierFromSettings = di.settings.getFloat("GatesAwakened_FuelMultiplier")
            return max(
                1F,
                (di.sector.playerFleet.logistics.fuelCostPerLightYear * fuelMultiplierFromSettings)
            )
        }

    fun jumpCostInFuel(distanceInLY: Float): Int = (fuelCostPerLY * distanceInLY).roundToInt()

    fun getSystems(): List<StarSystemAPI> =
        di.sector.starSystems
            .filterNot { it.isBlacklisted }

    /**
     * List of non-blacklisted gates (filterable), sorted by shortest distance from player first
     */
    fun getGates(
        filter: GateFilter,
        excludeCurrentGate: Boolean,
        includeGatesFromOtherMods: Boolean = false
    ): List<GateInfo> {
        return getSystems()
            .flatMap { system ->
                system.getEntitiesWithTag(Tags.TAG_GATE) +
                        if (includeGatesFromOtherMods)
                            system.getEntitiesWithTag(Tags.TAG_BOGGLED_GATE)
                        else
                            emptyList()
            }
            .asSequence()
            .filter { gate ->
                when (filter) {
                    GateFilter.All -> true
                    GateFilter.Active -> gate.isActive
                    GateFilter.Inactive -> !gate.isActive
                    GateFilter.IntroCore -> gate.hasTag(Tags.TAG_GATE_INTRO_CORE)
                    GateFilter.IntroFringe -> gate.hasTag(Tags.TAG_GATE_INTRO_FRINGE)
                }
            }
            .map {
                GateInfo(
                    gate = it,
                    systemId = it.starSystem.id,
                    systemName = it.starSystem.baseName,
                    sourceMod = when {
                        it.tags.contains(Tags.TAG_ACTIVE_GATES_GATE) -> GateMod.ActiveGates
                        it.tags.contains(Tags.TAG_BOGGLED_GATE) -> GateMod.BoggledPlayerGateConstruction
                        it.tags.contains(Tags.TAG_GATE_ACTIVATED) -> GateMod.GatesAwakened
                        else -> GateMod.Unknown
                    }
                )
            }
            .filter {
                if (excludeCurrentGate)
                    it.gate.distanceFromPlayerInHyperspace > 0
                else
                    true
            }
            .sortedBy { it.gate.distanceFromPlayerInHyperspace }
            .toList()
    }

    fun updateActiveGateIntel() {
        val activeGates = getGates(GateFilter.Active, excludeCurrentGate = false).map { it.gate }

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

    fun spawnGateAtLocation(location: SectorEntityToken?, activateAfterSpawning: Boolean): Boolean {
        if (location == null) {
            di.errorReporter.reportCrash(NullPointerException("Tried to spawn gate but target location was null!"))
            return false
        }

        val newGate = BaseThemeGenerator.addNonSalvageEntity(
            location.starSystem,
            BaseThemeGenerator.EntityLocation()
                .apply {
                    this.location = location.location
                    this.orbit = createOrbit(location)
                },
            "inactive_gate",
            Factions.DERELICT
        )

        if (activateAfterSpawning) {
            newGate.entity?.activate()
        }

        return true
    }

    fun createOrbit(
        targetLocation: SectorEntityToken,
        orbitCenter: SectorEntityToken = targetLocation.starSystem.center
    ): OrbitAPI {
        val orbitRadius = Misc.getDistance(targetLocation, orbitCenter)

        return di.factory.createCircularOrbit(
            orbitCenter,
            Misc.getAngleInDegrees(orbitCenter.location, targetLocation.location),
            orbitRadius,
            orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f) // taken from StarSystemGenerator:1655
        )
    }
}

internal data class GateInfo(
    val gate: Gate,
    val systemId: String,
    val systemName: String,
    val sourceMod: GateMod
)

internal enum class GateFilter {
    All,
    Active,
    Inactive,
    IntroCore,
    IntroFringe
}

enum class GateMod {
    GatesAwakened,
    ActiveGates,
    BoggledPlayerGateConstruction,
    Unknown
}

val Any?.exhaustiveWhen: Unit?
    get() = this?.run { }

typealias Gate = SectorEntityToken