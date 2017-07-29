package org.toast.activegates;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class AddGateData extends GateCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        TextPanelAPI textPanel = dialog.getTextPanel();
        SectorEntityToken gate = dialog.getInteractionTarget();
        MemoryAPI mem = gate.getMemoryWithoutUpdate();

        if (!gate.hasTag(ACTIVATED)) {
            textPanel.addParagraph("It costs " + getCommodityCostString() + " to activate the gate.");
        }
        textPanel.addParagraph("It costs " + getFuelCostPerLY() + " fuel per light year to use the gate for travel.");

        if (getDebug()) {
            Map<Float, String> allGates = getGateMap(Tags.GATE);
            textPanel.addParagraph("All gates:");
            for (Float key : allGates.keySet()) {
                textPanel.addParagraph(allGates.get(key) + " at " + key);
            }
        }

        Map<Float,String> map = getGateMap(ACTIVATED);
        Iterator<Float> iter = map.keySet().iterator();

        if (getDebug()) {
            textPanel.addParagraph("All activated gates:");
            for (Float key : map.keySet()) {
                textPanel.addParagraph(map.get(key) + " at " + key);
            }
        }

        int count;
        int maxcount = 5;
        for (count = 0; count < maxcount; count++) {
            mem.set("$gate" + count + "exists", false, 0);
        }
        for (count = 0; count < maxcount && iter.hasNext(); ) {
            Float key = iter.next();
            if (key != 0) {
                count++;
                if (getDebug()) {
                    textPanel.addParagraph(count + "," + key + "," + map.get(key));
                }
                mem.set("$gate" + count + "exists", true, 0);
                mem.set("$gate" + count, map.get(key), 0);
            }
        }

        return true;
    }
}
