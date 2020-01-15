package org.wisp.demo;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import java.util.Map;

public class DemoEndDialog implements InteractionDialogPlugin {
    protected InteractionDialogAPI dialog;

    /**
     * Called when the dialog is shown.
     *
     * @param dialog the actual UI element being shown
     */
    @Override
    public void init(InteractionDialogAPI dialog) {
        // Save the dialog UI element so that we can write to it outside of this method
        this.dialog = dialog;

        // Launch into our event by triggering the invisible "INIT" option,
        // which will call `optionSelected()`
        this.optionSelected(null, DemoBarEvent.OptionId.INIT);
    }

    /**
     * This method is called when the player has selected some option on the dialog.
     *
     * @param optionText the actual text that was displayed on the selected option
     * @param optionData the value used to uniquely identify the option
     */
    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData instanceof OptionId) {
            // Clear shown options before we show new ones
            dialog.getOptionPanel().clearOptions();

            // Handle all possible options the player can choose
            switch ((OptionId) optionData) {
                // The invisible "init" option was selected by the init method.
                case INIT:
                    dialog.getTextPanel().addPara("As soon as your shuttle touches down, your mind is filled " +
                            "with knowledge of how to create quest mods. How amazingly convenient.");
                    dialog.getTextPanel().addPara("You resolve to go off and create your very own!");

                    // The quest is completed as soon as the plot is resolved
                    DemoQuestCoordinator.completeQuest();

                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case LEAVE:
                    dialog.dismiss();
                    break;
            }
        }
    }

    enum OptionId {
        INIT,
        LEAVE
    }

    // The rest of the methods must exist, but can be ignored for our simple demo quest.
    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        // Can add things like hints for what the option does here
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }
}
