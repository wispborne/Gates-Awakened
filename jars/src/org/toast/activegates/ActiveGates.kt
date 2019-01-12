package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import kotlin.math.roundToInt

/**
 * A collection of information and stateless utility methods for the Active Gates mod.
 */
internal object ActiveGates {

    val inDebugMode: Boolean
        get() = Global.getSettings().getBoolean("activeGates_Debug")

    private val fuelCostPerLY: Float
        get() {
            val fuelMultiplierFromSettings = Global.getSettings().getFloat("activeGates_FuelMultiplier")
            return Math.max(1F, (Global.getSector().playerFleet.logistics.fuelCostPerLightYear * fuelMultiplierFromSettings))
        }

    val activationCost: Map<String, Float> =
            mapOf(
                    "metals" to Global.getSettings().getFloat("activeGates_Metals"),
                    "heavy_machinery" to Global.getSettings().getFloat("activeGates_HeavyMachinery"),
                    "rare_metals" to Global.getSettings().getFloat("activeGates_Transplutonics"),
                    "volatiles" to Global.getSettings().getFloat("activeGates_Volatiles"),
                    "gamma_core" to Global.getSettings().getFloat("activeGates_GammaCores"))

    fun canActivate(): Boolean {
        val cargo = Global.getSector().playerFleet.cargo
        return activationCost.all { commodityAndCost -> cargo.getCommodityQuantity(commodityAndCost.key) >= commodityAndCost.value }
    }

    fun payActivationCost(): Boolean {
        val cargo = Global.getSector().playerFleet.cargo

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

    /**
     * List of systems with gates (filterable), sorted by shortest distance from player first
     */
    fun getGates(filter: GateFilter, excludeCurrentGate: Boolean = true): List<GateDestination> {
        val playerLoc = Global.getSector().playerFleet.locationInHyperspace

        return Global.getSector().starSystems
                .filter {
                    when (filter) {
                        GateFilter.Active -> it.getEntitiesWithTag(TAG_GATE_ACTIVATED).any()
                        GateFilter.Inactive -> it.getEntitiesWithTag(TAG_GATE).any() && it.getEntitiesWithTag(TAG_GATE_ACTIVATED).none()
                        GateFilter.All -> it.getEntitiesWithTag(TAG_GATE).any()
                    }
                }
                .map {
                    GateDestination(
                            systemId = it.id,
                            systemName = it.baseName,
                            distanceFromPlayer = Misc.getDistanceLY(playerLoc, it.location))
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

    const val TAG_GATE_ACTIVATED = "gate_activated"
    const val TAG_GATE = Tags.GATE
}

internal data class GateDestination(val systemId: String, val systemName: String, val distanceFromPlayer: Float)

internal enum class GateFilter {
    Active,
    Inactive,
    All
}