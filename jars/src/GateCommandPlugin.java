package org.toast.activegates;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;

public abstract class GateCommandPlugin extends BaseCommandPlugin {
    public static final String ACTIVATED = "gate_activated";

    public float getFuelCost() {
        float multiplier = Global.getSettings().getFloat("activeGatesFuelMultiplier");
        float cost = Global.getSector().getPlayerFleet().getLogistics().getFuelCostPerLightYear() * multiplier;
        if (cost < 1) cost = 1;
        return cost;
    }

    public float getCommodityCost(String commodity) {
        SettingsAPI settings = Global.getSettings();
        if (commodity == "metals") {
            return settings.getFloat("activeGatesMetals");
        } else if (commodity == "machinery") {
            return settings.getFloat("activeGatesHeavyMachinery");
        } else if (commodity == "transplutonics") {
            return settings.getFloat("activeGatesTransplutonics");
        } else if (commodity == "organics") {
            return settings.getFloat("activeGatesOrganics");
        } else if (commodity == "volatiles") {
            return settings.getFloat("activeGatesVolatiles");
        } else {
            return 0;
        }
    }

    public String getCommodityCostString() {
        return getCommodityCost("metals") + " metals, "
                + getCommodityCost("machinery") + " heavy machinery, "
                + getCommodityCost("transplutonics") + " transplutonics, "
                + getCommodityCost("organics") + " organics, and "
                + getCommodityCost("volatiles") + " volatiles";
    }

    public boolean canActivate() {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        return (cargo.getCommodityQuantity("metals") >= getCommodityCost("metals")) &&
                (cargo.getCommodityQuantity("rare_metals") >= getCommodityCost("transplutonics")) &&
                (cargo.getCommodityQuantity("heavy_machinery") >= getCommodityCost("machinery")) &&
                (cargo.getCommodityQuantity("organics") >= getCommodityCost("organics")) &&
                (cargo.getCommodityQuantity("volatiles") >= getCommodityCost("volatiles"));
    }

    public boolean payActivationCost() {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        if (canActivate()) {
            cargo.removeCommodity("metals", getCommodityCost("metals"));
            cargo.removeCommodity("rare_metals", getCommodityCost("transplutonics"));
            cargo.removeCommodity("heavy_machinery", getCommodityCost("machinery"));
            cargo.removeCommodity("organics", getCommodityCost("organics"));
            cargo.removeCommodity("volatiles", getCommodityCost("volatiles"));
            return true;
        } else {
            return false;
        }
    }
}
