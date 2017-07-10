package data.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class ActivateGateCommandPlugin extends data.campaign.rulecmd.GateCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        TextPanelAPI textPanel = dialog.getTextPanel();

        SectorEntityToken gate = dialog.getInteractionTarget();
        if (gate.hasTag(Tags.GATE)) {
            if (!gate.hasTag(ACTIVATED)) {
                // FIXME pay cost to activate
                gate.addTag(ACTIVATED);
                textPanel.addParagraph("The gate is activated.");
            } else {
                textPanel.addParagraph("The gate is already activated.");
            }
            return true;
        } else {
            return false;
        }
    }
}
