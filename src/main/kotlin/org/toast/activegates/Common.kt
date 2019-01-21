package org.toast.activegates

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
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
            .flatMap { system -> system.getEntitiesWithTag(TAG_GATE) }
            .filter { gate ->
                when (filter) {
                    GateFilter.Active -> gate.hasTag(TAG_GATE_ACTIVATED)
                    GateFilter.Inactive -> !gate.hasTag(TAG_GATE_ACTIVATED)
                    GateFilter.All -> true
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

    /** A gate that has been activated by the player **/
    const val TAG_GATE_ACTIVATED = "gate_activated"

    /** The gate in the Core that is activated for the intro event **/
    const val TAG_GATE_INTRO_CORE = "ag_gate_intro_core"

    const val TAG_BLACKLISTED_SYSTEM = "ag_blacklisted_system"

    /**
     * Vanilla tag for a gate.
     **/
    private const val TAG_GATE = Tags.GATE
}

internal class GateDestination(
    val gate: SectorEntityToken,
    val systemId: String,
    val systemName: String,
    val distanceFromPlayer: Float
)

internal enum class GateFilter {
    Active,
    Inactive,
    All
}

internal val StarSystemAPI.isBlacklisted: Boolean
    get() = this.hasTag(Common.TAG_BLACKLISTED_SYSTEM)