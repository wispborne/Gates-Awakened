package org.toast.activegates;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

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

        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        float fuelcost = getFuelCost();
        if (cargo.getFuel() >= fuelcost) {
            cargo.removeFuel(fuelcost);
        } else {
            textPanel.addParagraph("Unfortunately, your fleet lacks the " + fuelcost +
                    " fuel necessary to use the gate.");
            return false;
        }

        List<LocationAPI> systemsWithActivatedGates = new ArrayList<>();
        Random rng = new Random();
        for (LocationAPI system : Global.getSector().getStarSystems())
        {
            List<SectorEntityToken> candidates =
                system.getEntitiesWithTag(ACTIVATED);
            if (candidates.size() > 0)
            {
                systemsWithActivatedGates.add(system);
            }
        }
        LocationAPI newSys = systemsWithActivatedGates.get(
                rng.nextInt(systemsWithActivatedGates.size()));
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
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
