package org.wisp.demo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

import java.util.Random;

/**
 * Coordinates and tracks the state of Demo quest.
 */
class DemoQuestCoordinator {
    /**
     * The tag that is applied to the planet the player must travel to.
     */
    private static String TAG_DESTINATION_PLANET = "Demo_destination_planet";

    private static String FLAG_IS_COMPLETE = "Demo_isQuestComplete";

    static SectorEntityToken getDestinationPlanet() {
        return Global.getSector().getEntityById(TAG_DESTINATION_PLANET);
    }

    static boolean shouldOfferQuest() {
        return true; // Set some conditions
    }

    /**
     * Called when player starts the bar event.
     */
    static void initQuest() {
        chooseAndTagDestinationPlanet();
    }

    /**
     * Player has accepted quest.
     */
    static void startQuest(SectorEntityToken sourceLocation) {
        Global.getSector().getIntelManager().addIntel(new DemoIntel(sourceLocation, getDestinationPlanet()));
    }

    /**
     * Very dumb method that idempotently tags a random planet as the destination.
     */
    private static void chooseAndTagDestinationPlanet() {
        if (getDestinationPlanet() == null) {
            StarSystemAPI randomSystem = Global.getSector().getStarSystems()
                    .get(new Random().nextInt(Global.getSector().getStarSystems().size()));
            PlanetAPI randomPlanet = randomSystem.getPlanets()
                    .get(new Random().nextInt(randomSystem.getPlanets().size()));
            randomPlanet.addTag(TAG_DESTINATION_PLANET);
        }
    }

    public static void completeQuest() {
        // Update the persistent data (stored in save game) to flag the quest as complete
        Global.getSector().getPersistentData().put(FLAG_IS_COMPLETE, true);

        // Get the instance of our intel and tell the game to end it after a few (3) days
        DemoIntel demoIntel = (DemoIntel) Global.getSector().getIntelManager().getFirstIntel(DemoIntel.class);

        if (demoIntel != null) {
            demoIntel.endAfterDelay();
        }
    }

    /**
     * Whether the quest is complete or not.
     */
    public static Boolean isComplete() {
        // The "false" is the default value; if the flag doesn't exist, the quest will be considered incomplete
        return (Boolean) Global.getSector().getPersistentData().getOrDefault(FLAG_IS_COMPLETE, false);
    }
}
