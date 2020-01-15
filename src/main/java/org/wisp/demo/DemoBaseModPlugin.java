package org.wisp.demo;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;

public class DemoBaseModPlugin extends BaseModPlugin {
    public static final String MOD_PREFIX = "Demo_";

    /**
     * Called when the player loads a saved game.
     *
     * @param newGame true if the save game was just created for the first time.
     *                Note that there are a few `onGameLoad` methods that may be a better choice than using this parameter
     */
    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        BarEventManager barEventManager = BarEventManager.getInstance();

        // If the prerequisites for the quest have been met (optional) and the game isn't already aware of the bar event,
        // add it to the BarEventManager so that it shows up in bars
        if (DemoQuestCoordinator.shouldOfferQuest() && !barEventManager.hasEventCreator(DemoBarEventCreator.class)) {
            barEventManager.addEventCreator(new DemoBarEventCreator());
        }

        Global.getSector().registerPlugin(new DemoCampaignPlugin());
    }
}
