package org.wisp.gatesawakened

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignPlugin
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.intro.Intro
import org.wisp.gatesawakened.intro.IntroQuestFinishedDialog
import org.wisp.gatesawakened.jumping.JumpDialog
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.midgame.MidgameQuestFinishedDialog

/**
 * Instead of using `rules.csv`, use this plugin to trigger dialog choices and conversations.
 */
class CampaignPlugin : BaseCampaignPlugin() {

    override fun getId() = "g8_GateInteractionPlugin"

    override fun isTransient(): Boolean = false

    /**
     * When the player interacts with a dialog, override the default interaction with a
     * mod-specific one if necessary.
     */
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            // Interacting with a gate
            interactionTarget in Common.getGates(
                GateFilter.All,
                excludeCurrentGate = false
            )
                .map { it.gate } -> {
                when {
                    Intro.hasQuestBeenStarted
                            && !Intro.wasQuestCompleted
                            && interactionTarget == Intro.fringeGate -> {
                        // Show dialog to complete the intro quest
                        PluginPick<InteractionDialogPlugin>(
                            IntroQuestFinishedDialog(),
                            CampaignPlugin.PickPriority.MOD_SPECIFIC
                        )
                    }
                    interactionTarget.isActive -> {
                        // Show dialog to jump via an active gate
                        PluginPick<InteractionDialogPlugin>(
                            JumpDialog(),
                            CampaignPlugin.PickPriority.MOD_SET
                        )
                    }
                    !interactionTarget.isActive
                            && (org.wisp.gatesawakened.di.memory[Memory.GATE_ACTIVATION_CODES_REMAINING]
                            as? Int ?: 0) > 0 -> {
                        // Show dialog to activate a new gate
                        PluginPick<InteractionDialogPlugin>(
                            JumpDialog(),
                            CampaignPlugin.PickPriority.MOD_SET
                        )
                    }
                    else -> null
                }
            }
            interactionTarget is PlanetAPI
                    && org.wisp.gatesawakened.di.memory[Memory.MID_QUEST_IN_PROGRESS] == true
                    && org.wisp.gatesawakened.di.memory[Memory.MID_QUEST_DONE] != true
                    && interactionTarget == Midgame.planetWithCache -> {
                // Show dialog to finish the midgame quest
                PluginPick(
                    MidgameQuestFinishedDialog(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }
            else -> null
        }
    }
}