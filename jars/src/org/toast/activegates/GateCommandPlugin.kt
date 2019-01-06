package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import java.util.*
import kotlin.math.roundToInt

abstract class GateCommandPlugin : BaseCommandPlugin() {

    val debug: Boolean
        get() = Global.getSettings().getBoolean("activeGates_Debug")

    val fuelCostPerLY: Float
        get() {
            val multiplier = Global.getSettings().getFloat("activeGates_FuelMultiplier")
            var cost = Global.getSector().playerFleet.logistics.fuelCostPerLightYear * multiplier
            if (cost < 1) cost = 1f
            return cost
        }

    val commodityCostString: String
        get() {
            val cargo = Global.getSector().playerFleet.cargo
            return ("${getCommodityCost("metals").roundToInt()} (${cargo.getCommodityQuantity("metals")}) metals, " +
                    "${getCommodityCost("heavy_machinery").roundToInt()} (${cargo.getCommodityQuantity("heavy_machinery")}) heavy machinery, " +
                    "and some kind of basic processing core")
        }

    private val activationCost: Map<String, Float> =
            mapOf(
                    "metals" to Global.getSettings().getFloat("activeGates_Metals"),
                    "heavy_machinery" to Global.getSettings().getFloat("activeGates_HeavyMachinery"),
                    "gamma_core" to Global.getSettings().getFloat("activeGates_GammaCores"))

    fun canActivate(): Boolean {
        val cargo = Global.getSector().playerFleet.cargo
        return activationCost.all { commodityAndCost -> cargo.getCommodityQuantity(commodityAndCost.key) >= commodityAndCost.value }
    }

    fun payActivationCost(): Boolean {
        val cargo = Global.getSector().playerFleet.cargo
        return if (canActivate()) {
            activationCost.forEach { commodityAndcost ->
                cargo.removeCommodity(commodityAndcost.key, commodityAndcost.value)
            }
            true
        } else {
            false
        }
    }

    fun getGateMap(tag: String): Map<Float, String> {
        // returns map of systems with activated gates, keyed by distance
        // https://stackoverflow.com/questions/571388/how-can-i-sort-the-keys-of-a-map-in-java
        val map = TreeMap<Float, String>()
        val playerLoc = Global.getSector().playerFleet.locationInHyperspace
        for (system in Global.getSector().starSystems) {
            val candidates = system.getEntitiesWithTag(tag)
            if (candidates.size > 0) {
                // FIXME find the right accessor: system.getName()???
                map[Misc.getDistanceLY(playerLoc, system.location)] = system.id
            }
        }
        return map
    }

    private fun getCommodityCost(commodity: String): Float = activationCost[commodity] ?: 0F

    companion object {
        val ACTIVATED = "gate_activated"
    }
}
