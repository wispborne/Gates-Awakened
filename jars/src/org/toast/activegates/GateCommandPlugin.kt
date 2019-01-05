package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import java.util.*

abstract class GateCommandPlugin : BaseCommandPlugin() {

    val debug: Boolean
        get() = Global.getSettings().getBoolean("activeGatesDebug")

    val fuelCostPerLY: Float
        get() {
            val multiplier = Global.getSettings().getFloat("activeGatesFuelMultiplier")
            var cost = Global.getSector().playerFleet.logistics.fuelCostPerLightYear * multiplier
            if (cost < 1) cost = 1f
            return cost
        }

    val commodityCostString: String
        get() = (getCommodityCost("metals").toString() + " metals, "
                + getCommodityCost("machinery") + " heavy machinery, "
                + getCommodityCost("transplutonics") + " transplutonics, "
                + getCommodityCost("organics") + " organics, and "
                + getCommodityCost("volatiles") + " volatiles")

    fun getCommodityCost(commodity: String): Float {
        val settings = Global.getSettings()
        return when {
            commodity === "metals" -> settings.getFloat("activeGatesMetals")
            commodity === "machinery" -> settings.getFloat("activeGatesHeavyMachinery")
            commodity === "transplutonics" -> settings.getFloat("activeGatesTransplutonics")
            commodity === "organics" -> settings.getFloat("activeGatesOrganics")
            commodity === "volatiles" -> settings.getFloat("activeGatesVolatiles")
            else -> 0f
        }
    }

    fun canActivate(): Boolean {
        val cargo = Global.getSector().playerFleet.cargo
        return cargo.getCommodityQuantity("metals") >= getCommodityCost("metals") &&
                cargo.getCommodityQuantity("rare_metals") >= getCommodityCost("transplutonics") &&
                cargo.getCommodityQuantity("heavy_machinery") >= getCommodityCost("machinery") &&
                cargo.getCommodityQuantity("organics") >= getCommodityCost("organics") &&
                cargo.getCommodityQuantity("volatiles") >= getCommodityCost("volatiles")
    }

    fun payActivationCost(): Boolean {
        val cargo = Global.getSector().playerFleet.cargo
        return if (canActivate()) {
            cargo.removeCommodity("metals", getCommodityCost("metals"))
            cargo.removeCommodity("rare_metals", getCommodityCost("transplutonics"))
            cargo.removeCommodity("heavy_machinery", getCommodityCost("machinery"))
            cargo.removeCommodity("organics", getCommodityCost("organics"))
            cargo.removeCommodity("volatiles", getCommodityCost("volatiles"))
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

    companion object {
        val ACTIVATED = "gate_activated"
    }


}
