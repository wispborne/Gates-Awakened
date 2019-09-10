package org.toast.activegates

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import org.toast.activegates.constants.Memory
import org.toast.activegates.constants.Tags
import org.toast.activegates.constants.isBlacklisted
import kotlin.math.roundToInt

/**
 * A collection of information and stateless utility methods for the Active Gates mod.
 */
internal object Common {
    val isDebugModeEnabled: Boolean
        get() = di.settings.getBoolean("activeGates_Debug")

    private val fuelCostPerLY: Float
        get() {
            val fuelMultiplierFromSettings = di.settings.getFloat("activeGates_FuelMultiplier")
            return Math.max(
                1F,
                (di.sector.playerFleet.logistics.fuelCostPerLightYear * fuelMultiplierFromSettings)
            )
        }

    val activationCost: Map<String, Float>
        get() = mapOf(
            "metals" to di.settings.getFloat("activeGates_Metals"),
            "heavy_machinery" to di.settings.getFloat("activeGates_HeavyMachinery"),
            "rare_metals" to di.settings.getFloat("activeGates_Transplutonics"),
            "volatiles" to di.settings.getFloat("activeGates_Volatiles"),
            "gamma_core" to di.settings.getFloat("activeGates_GammaCores")
        )

    fun canActivate(): Boolean {
        val cargo = di.sector.playerFleet.cargo
        return activationCost.all { commodityAndCost -> cargo.getCommodityQuantity(commodityAndCost.key) >= commodityAndCost.value }
    }

    fun payActivationCost(): Boolean {
        val cargo = di.sector.playerFleet.cargo

        return if (canActivate()) {
            activationCost.forEach { commodityAndCost ->
                cargo.removeCommodity(commodityAndCost.key, commodityAndCost.value)
            }
            true
        } else {
            false
        }
    }

    var remainingActivationCodes: Int
        get() = di.sector.memoryWithoutUpdate[Memory.GATE_ACTIVATION_CODES_REMAINING] as? Int ?: 0
        set(value) {
            di.sector.memoryWithoutUpdate[Memory.GATE_ACTIVATION_CODES_REMAINING] = value
        }

    fun jumpCostInFuel(distanceInLY: Float): Int = (fuelCostPerLY * distanceInLY).roundToInt()

    fun getSystems(): List<StarSystemAPI> =
        di.sector.starSystems
            .filterNot { it.isBlacklisted }

    /**
     * List of non-blacklisted gates (filterable), sorted by shortest distance from player first
     */
    fun getGates(filter: GateFilter, excludeCurrentGate: Boolean): List<GateInfo> {
        return getSystems()
            .flatMap { system -> system.getEntitiesWithTag(Tags.TAG_GATE) }
            .asSequence()
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
                GateInfo(
                    gate = it,
                    systemId = it.starSystem.id,
                    systemName = it.starSystem.baseName
                )
            }
            .filter {
                if (excludeCurrentGate)
                    it.gate.distanceFromPlayer > 0
                else
                    true
            }
            .sortedBy { it.gate.distanceFromPlayer }
            .toList()
    }

    fun getCommodityCostOf(commodity: String): Float = activationCost[commodity] ?: 0F
}

internal data class GateInfo(
    val gate: Gate,
    val systemId: String,
    val systemName: String
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