package data.campaign.rulecmd;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class ActiveGatesCommandPlugin extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        List<LocationAPI> systemsWithGates = new ArrayList<>();
        Random rng = new Random();
        for (LocationAPI system : Global.getSector().getStarSystems())
        {
            List<SectorEntityToken> candidates =
                system.getEntitiesWithTag(Tags.GATE);
            if (candidates.size() > 0)
            {
                systemsWithGates.add(system);
            }
        }
        LocationAPI newSys = systemsWithGates.get(rng.nextInt(systemsWithGates.size()));
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getContainingLocation().removeEntity(playerFleet);
        newSys.addEntity(playerFleet);
        Global.getSector().setCurrentLocation(newSys);
        List<SectorEntityToken> gates = newSys.getEntitiesWithTag(Tags.GATE);
        Vector2f newVect = gates.get(0).getLocation();
        playerFleet.setLocation(newVect.x, newVect.y);
        playerFleet.clearAssignments();

        return true;
    }
}
