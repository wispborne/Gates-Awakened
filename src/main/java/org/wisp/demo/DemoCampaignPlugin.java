package org.wisp.demo;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class DemoCampaignPlugin extends BaseCampaignPlugin {
    /**
     * Unique identifier for the class. Ensures that it won't get added to the game twice.
     */
    @Override
    public String getId() {
        return DemoBaseModPlugin.MOD_PREFIX + "CampaignPlugin";
    }

    /**
     * @param interactionTarget the entity that the player is interacting with
     * @return the plugin to handle the interaction, or null to skip handling it
     */
    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        // If the player is interacting with the destination planet during the quest, show the dialog
        if (DemoQuestCoordinator.isStarted()
                && !DemoQuestCoordinator.isComplete()
                && interactionTarget.getId().equals(DemoQuestCoordinator.getDestinationPlanet().getId())) {
            return new PluginPick<>(new DemoEndDialog(), PickPriority.MOD_SET);
        }

        // Otherwise, return null to show that some other `CampaignPlugin` class should handle the interaction
        return null;
    }
}
