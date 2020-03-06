package org.wisp.demo;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;

public class DemoBarEvent extends BaseBarEventWithPerson {
    /**
     * True if this event may be selected to be offered to the player,
     * or false otherwise.
     */
    public boolean shouldShowAtMarket(MarketAPI market) {
        return super.shouldShowAtMarket(market) && !getMarket().getFactionId().equals("luddic_path"); // add any conditions you want
    }

    /**
     * Set up the text that appears when the player goes to the bar
     * and the option for them to start the conversation.
     */
    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog) {
        // Calling super does nothing in this case, but is good practice because a subclass should
        // implement all functionality of the superclass (and usually more)
        super.addPromptAndOption(dialog);
        regen(dialog.getInteractionTarget().getMarket()); // Sets field variables and creates a random person

        // Display the text that will appear when the player first enters the bar and looks around
        dialog.getTextPanel().addPara("A small crowd has gathered around a " + getManOrWoman() + " who looks to be giving " +
                "some sort of demonstration.");

        // Display the option that lets the player choose to investigate our bar event
        dialog.getOptionPanel().addOption("See what the demonstration is about", this);
    }

    /**
     * Called when the player chooses this event from the list of options shown when they enter the bar.
     */
    @Override
    public void init(InteractionDialogAPI dialog) {
        super.init(dialog);
        // Choose where the player has to travel to
        DemoQuestCoordinator.initQuest();

        // If player starts our event, then backs out of it, `done` will be set to true.
        // If they then start the event again without leaving the bar, we should reset `done` to false.
        done = false;

        // The boolean is for whether to show only minimal person information. True == minimal
        dialog.getVisualPanel().showPersonInfo(person, true);

        // Launch into our event by triggering the "INIT" option, which will call `optionSelected()`
        this.optionSelected(null, OptionId.INIT);
    }

    /**
     * This method is called when the player has selected some option for our bar event.
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
                case INIT:
                    // The player has chosen to walk over to the crowd, so let's tell them what happens.
                    dialog.getTextPanel().addPara("You walk over and see that the " + getManOrWoman() +
                            " is showing the crowd how to create quest mods for a video game.");
                    dialog.getTextPanel().addPara("It seems that you can learn more by traveling to " +
                            DemoQuestCoordinator.getDestinationPlanet().getName());

                    // And give them some options on what to do next
                    dialog.getOptionPanel().addOption("Take notes and decide to travel to learn more", OptionId.TAKE_NOTES);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case TAKE_NOTES:
                    // Tell our coordinator class that the player just started the quest
                    DemoQuestCoordinator.startQuest(dialog.getInteractionTarget());

                    dialog.getTextPanel().addPara("You take some notes. Quest mods sure seem like a lot of work...");
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case LEAVE:
                    // They've chosen to leave, so end our interaction. This will send them back to the bar.
                    // If noContinue is false, then there will be an additional "Continue" option shown
                    // before they are returned to the bar. We don't need that.
                    noContinue = true;
                    done = true;

                    // Removes this event from the bar so it isn't offered again
                    BarEventManager.getInstance().notifyWasInteractedWith(this);
                    break;
            }
        }
    }

    enum OptionId {
        INIT,
        TAKE_NOTES,
        LEAVE
    }
}