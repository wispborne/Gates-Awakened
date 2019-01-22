package org.toast.activegates

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.util.Misc
import kotlin.math.roundToInt

/**
 * A collection of information and stateless utility methods for the Active Gates mod.
 */
internal object Common {
    val isDebugModeEnabled: Boolean
        get() = Di.inst.settings.getBoolean("activeGates_Debug")

    private val fuelCostPerLY: Float
        get() {
            val fuelMultiplierFromSettings = Di.inst.settings.getFloat("activeGates_FuelMultiplier")
            return Math.max(
                1F,
                (Di.inst.sector.playerFleet.logistics.fuelCostPerLightYear * fuelMultiplierFromSettings)
            )
        }

    val activationCost: Map<String, Float>
        get() = mapOf(
            "metals" to Di.inst.settings.getFloat("activeGates_Metals"),
            "heavy_machinery" to Di.inst.settings.getFloat("activeGates_HeavyMachinery"),
            "rare_metals" to Di.inst.settings.getFloat("activeGates_Transplutonics"),
            "volatiles" to Di.inst.settings.getFloat("activeGates_Volatiles"),
            "gamma_core" to Di.inst.settings.getFloat("activeGates_GammaCores")
        )

    fun canActivate(): Boolean {
        val cargo = Di.inst.sector.playerFleet.cargo
        return activationCost.all { commodityAndCost -> cargo.getCommodityQuantity(commodityAndCost.key) >= commodityAndCost.value }
    }

    fun payActivationCost(): Boolean {
        val cargo = Di.inst.sector.playerFleet.cargo

        return if (canActivate()) {
            activationCost.forEach { commodityAndCost ->
                cargo.removeCommodity(commodityAndCost.key, commodityAndCost.value)
            }
            true
        } else {
            false
        }
    }

    fun jumpCostInFuel(distanceInLY: Float): Int = (fuelCostPerLY * distanceInLY).roundToInt()

    fun getSystems(): List<StarSystemAPI> =
        Di.inst.sector.starSystems
            .filterNot { it.isBlacklisted }

    /**
     * List of non-blacklisted gates (filterable), sorted by shortest distance from player first
     */
    fun getGates(filter: GateFilter, excludeCurrentGate: Boolean = true): List<GateDestination> {
        val playerLoc = Di.inst.sector.playerFleet.locationInHyperspace

        return getSystems()
            .flatMap { system -> system.getEntitiesWithTag(Tags.TAG_GATE) }
            .filter { gate ->
                when (filter) {
                    GateFilter.All -> true
                    GateFilter.Active -> gate.hasTag(Tags.TAG_GATE_ACTIVATED)
                    GateFilter.Inactive -> !gate.hasTag(Tags.TAG_GATE_ACTIVATED)
                    GateFilter.IntroCore -> gate.hasTag(Tags.TAG_GATE_INTRO_CORE)
                    GateFilter.IntroFringe -> gate.hasTag(Tags.TAG_GATE_INTRO_FRINGE)
                }
            }
            .map {
                GateDestination(
                    gate = it,
                    systemId = it.starSystem.id,
                    systemName = it.starSystem.baseName,
                    distanceFromPlayer = Misc.getDistanceLY(playerLoc, it.locationInHyperspace)
                )
            }
            .filter {
                if (excludeCurrentGate)
                    it.distanceFromPlayer > 0
                else
                    true
            }
            .sortedBy { it.distanceFromPlayer }
    }

    fun getCommodityCostOf(commodity: String): Float = activationCost[commodity] ?: 0F
}

internal class GateDestination(
    val gate: Gate,
    val systemId: String,
    val systemName: String,
    val distanceFromPlayer: Float
)

internal enum class GateFilter {
    All,
    Active,
    Inactive,
    IntroCore,
    IntroFringe
}

fun Any?.equalsAny(vararg arg: Any?) = arg.any { this == it }

val Any?.exhaustiveWhen: Unit?
    get() = this?.run { }

typealias Gate = SectorEntityToken