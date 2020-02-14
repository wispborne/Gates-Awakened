## Showing a Dialog

We now have a dialog that nearly wraps up the quest; all that's left is to show it to the player when they interact with the destination planet.

There are two main options for doing this; `CampaignPlugin` and `rules.csv`.

- [`BaseCampaignPlugin`](https://fractalsoftworks.com/starfarer.api/com/fs/starfarer/api/campaign/BaseCampaignPlugin.html) is the programmatic way that trades the complexity of `rules.csv` for the complexity of code. This method makes the quest mod arguably easier to understand, as there's less "magic"; any IDE will be able to see that an interaction dialog is being created in this class, whereas it's harder to determine how a dialog is being launched if it's done by `rules.csv`.

- [`rules.csv`](https://starsector.fandom.com/wiki/Rules.csv); ah, rules.csv. Launching a dialog using this is the best choice for mod compatibility, as it will allow other modders to override your dialog launch trigger by the other mod using `rules.csv`.
  - To give an example, the Gates Awakened mod originally showed a dialog  whenever the player interacted with a gate, triggered using a `CampaignPlugin`. However, Vayra's Sector had a special interaction that could occurr occasionally when the player interacted with a gate, triggered using `rules.csv`. Because the `CampaignPlugin` was always overriding anything in `rules.csv`, the Vayra's Sector interaction was never triggered as long as both mods were enabled. The fix was for Gates Awakened to trigger its dialog using `rules.csv`, and for Vayra's Sector to set the dialog trigger to a higher priority than the one in Gates Awakened.

The game chooses which interaction dialog to use to handle a player interaction by:

1. First looking at all `CampaignPlugin`s and seeing if any can handle the interaction.
2. If multiple `CampaignPlugin`s can handle the interaction, it chooses the one with the highest priority, as determined by [`PickPriority`](https://fractalsoftworks.com/starfarer.api/com/fs/starfarer/api/campaign/CampaignPlugin.PickPriority.html).
3. If multiple plugins have the same priority, it chooses one _somehow_ ("in an undefined way").
4. If the plugin that is picked is [`RuleBasedInteractionDialogPluginImpl`](https://fractalsoftworks.com/starfarer.api/com/fs/starfarer/api/impl/campaign/RuleBasedInteractionDialogPluginImpl.html), then it will look at all possibilities in `rules.csv` and pick the one with the highest [score](https://starsector.fandom.com/wiki/Rules.csv).

### Campaign Plugin

A [`CampaignPlugin`](https://fractalsoftworks.com/starfarer.api/com/fs/starfarer/api/campaign/CampaignPlugin.html) is the programmatic way, good for keeping everything in one place (not split across both code and csv) as long as you don't want it to be overridable through `rules.csv`.

An abstract implementation, [`BaseCampaignPlugin`](https://fractalsoftworks.com/starfarer.api/com/fs/starfarer/api/campaign/BaseCampaignPlugin.html), has been provided for convenience, allowing us to only implement the methods we need.

```
public class DemoCampaignPlugin extends BaseCampaignPlugin {
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
```