package org.wisp.demo;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DemoIntel extends BreadcrumbIntel {
    public DemoIntel(SectorEntityToken foundAt, SectorEntityToken target) {
        super(foundAt, target);
    }

    @Override
    public String getIcon() {
        return "graphics/icons/intel/player.png";
    }

    /**
     * The name/title of the intel. This will automatically update once the quest is complete
     * to read "Demo Quest - Completed".
     */
    @Override
    public String getName() {
        return "Demo Quest" + (DemoQuestCoordinator.isComplete() ? " - Completed" : "");
    }

    /**
     * The small list entry on the left side of the Intel Manager
     *
     * @param info the text area that shows the info
     * @param mode where the info is being shown
     */
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        // The call to super will add the quest name, so we just need to add the summary
        super.createIntelInfo(info, mode);

        info.addPara("Destination: %s", // text to show. %s is highlighted.
                3f, // padding on left side of text. Vanilla hardcodes these values so we will too
                super.getBulletColorForMode(mode), // color of text
                Misc.getHighlightColor(), // color of highlighted text
                DemoQuestCoordinator.getDestinationPlanet().getName()); // highlighted text

        // This will display like:
        // Demo Quest
        //     Destination: Ancyra
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addImage("graphics/illustrations/fly_away.jpg", // path to sprite
                width,
                128, // height
                10f); // left padding

        info.addPara("You learned a little about quest design at a bar on " + foundAt.getName() +
                        " and are traveling to %s to learn more.", // text to show. %s is highlighted.
                10f, // padding on left side of text. Vanilla hardcodes these values so we will too
                Misc.getHighlightColor(), // color of highlighted text
                target.getName()); // highlighted text

        // The super call adds the text from `getText()` (which we'll leave empty)
        // and then adds the number of days since the quest was acquired, which is
        // typically the bottom-most thing shown. Therefore, we'll make the call to
        // super as the last thing in this method.
        super.createSmallDescription(info, width, height);
    }

    /**
     * Return whatever tags your quest should have. You can also create your own tags.
     */
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        return new HashSet<>(Arrays.asList(Tags.INTEL_EXPLORATION, Tags.INTEL_STORY));
    }
}
