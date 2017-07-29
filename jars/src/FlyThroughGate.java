package org.toast.activegates;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.fs.starfarer.api.campaign.*;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class FlyThroughGate extends GateCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        TextPanelAPI textPanel = dialog.getTextPanel();

        // can only fly through activated gates
        if (!dialog.getInteractionTarget().hasTag(ACTIVATED)) {
            textPanel.addParagraph("The gate is not activated.");
            textPanel.addParagraph("Nothing happens.");
            return false;
        }

        String target = params.get(0).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
        if (target == null || target.isEmpty()) return false;

        LocationAPI newSys = Global.getSector().getStarSystem(target);
        textPanel.addParagraph(target + " selected");
        if (newSys == null) {
            textPanel.addParagraph("Could not find " + target + "; aborting");
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        CargoAPI cargo = playerFleet.getCargo();
        float fuelcost = getFuelCostPerLY() *
                Misc.getDistanceLY(playerFleet.getLocationInHyperspace(), newSys.getLocation());
        if (cargo.getFuel() >= fuelcost) {
            cargo.removeFuel(fuelcost);
        } else {
            textPanel.addParagraph("Unfortunately, your fleet lacks the " + fuelcost +
                    " fuel necessary to use the gate.");
            return false;
        }

        LocationAPI oldSys = playerFleet.getContainingLocation();
        oldSys.removeEntity(playerFleet);
        newSys.addEntity(playerFleet);
        Global.getSector().setCurrentLocation(newSys);
        List<SectorEntityToken> gates = newSys.getEntitiesWithTag(Tags.GATE);
        Vector2f newVect = gates.get(0).getLocation();
        playerFleet.setLocation(newVect.x, newVect.y);
        playerFleet.clearAssignments();

        textPanel.addParagraph("Your fleet passes through the gate...");
        if (oldSys == newSys) {
            textPanel.addParagraph("and nothing happens.");
        }

        return true;
    }
}
