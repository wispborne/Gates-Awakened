package org.wisp.gatesawakened

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.JumpDialog
import org.wisp.gatesawakened.constants.MOD_PREFIX
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.constants.Tags
import org.wisp.gatesawakened.createGate.CreateGateQuest
import org.wisp.gatesawakened.createGate.CreateGateQuestStart
import org.wisp.gatesawakened.createGate.GateCreatedDialog
import org.wisp.gatesawakened.intro.IntroQuest
import org.wisp.gatesawakened.intro.IntroQuestFinishedDialog
import org.wisp.gatesawakened.midgame.Midgame
import org.wisp.gatesawakened.midgame.MidgameQuestFinishedDialog

/**
 * Instead of using `rules.csv`, use this plugin to trigger dialog choices and conversations.
 */
class CampaignPlugin : BaseCampaignPlugin() {

    override fun getId() = "${MOD_PREFIX}_GateInteractionPlugin"

    // No need to add to saves
    override fun isTransient(): Boolean = true

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
                    IntroQuest.hasQuestBeenStarted
                            && !IntroQuest.wasQuestCompleted
                            && interactionTarget == IntroQuest.fringeGate -> {
                        // Show dialog to complete the intro quest
                        PluginPick<InteractionDialogPlugin>(
                            IntroQuestFinishedDialog(),
                            CampaignPlugin.PickPriority.MOD_SPECIFIC
                        )
                    }
                    CreateGateQuest.hasQuestBeenStarted == true
                            && CreateGateQuest.wasGateDelivered == true
                            && CreateGateQuest.wasQuestCompleted != true
                            && interactionTarget.hasTag(Tags.TAG_NEWLY_CONSTRUCTED_GATE) -> {
                        // Show dialog to complete the final, create gate quest
                        PluginPick<InteractionDialogPlugin>(
                            GateCreatedDialog().build(),
                            CampaignPlugin.PickPriority.MOD_SPECIFIC
                        )
                    }
                    interactionTarget.isActive -> {
                        if (Midgame.wasQuestCompleted && CreateGateQuest.shouldOfferQuest()) {
                            PluginPick<InteractionDialogPlugin>(
                                CreateGateQuestStart().build(),
                                CampaignPlugin.PickPriority.MOD_SET
                            )
                        } else {
                            // Show dialog to jump via an active gate
                            // Now triggered by rules.csv so that gates can be used by other mods.
                            // Using rules.csv allows other mods to override GA's jump dialog with their own.
                            // Note that this doesn't allow other mods to merge with GA's jump dialog; it's all or nothing.
                            // I can do that if needed; it's just more work/complexity to move all that logic into rules.

//                            PluginPick<InteractionDialogPlugin>(
//                                JumpDialog(),
//                                CampaignPlugin.PickPriority.MOD_SET
//                            )
                            null
                        }
                    }
                    !interactionTarget.isActive
                            && (di.memory[Memory.GATE_ACTIVATION_CODES_REMAINING]
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
                    && di.memory[Memory.MID_QUEST_IN_PROGRESS] == true
                    && di.memory[Memory.MID_QUEST_DONE] != true
                    && interactionTarget == Midgame.planetWithCache -> {
                // Show dialog to finish the midgame quest
                PluginPick(
                    MidgameQuestFinishedDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }
            else -> null
        }
    }
}