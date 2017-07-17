package org.toast.activegates;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class AddGateData extends GateCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        TextPanelAPI textPanel = dialog.getTextPanel();

        textPanel.addParagraph("It costs " + getCommodityCostString() + " to activate the gate.");
        if (dialog.getInteractionTarget().hasTag(ACTIVATED)) {
            textPanel.addParagraph("Fortunately, the gate has already been activated.");
        }
        textPanel.addParagraph("It costs " + getFuelCost() + " fuel to use the gate for travel.");
        return true;
    }
}
