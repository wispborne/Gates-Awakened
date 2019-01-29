package org.toast.activegates

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.toast.activegates.intro.Intro
import org.toast.activegates.intro.IntroQuestCompletedDialog

/**
 * Instead of using `rules.csv`, use this plugin to trigger dialog choices and conversations.
 */
class CampaignPlugin : BaseCampaignPlugin() {

    override fun getId() = "g8_GateInteractionPlugin"

    /**
     * When player interacts with a valid gate, tell Starsector to let the appropriate dialog handle it.
     */
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return if (interactionTarget in Common.getGates(GateFilter.All, excludeCurrentGate = false).map { it.gate }) {
            if (Intro.wasIntroQuestStarted
                && !Intro.wasIntroQuestCompleted
                && interactionTarget == Intro.fringeGate
            ) {
                PluginPick<InteractionDialogPlugin>(
                    IntroQuestCompletedDialog(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            } else {
                null
            }
        } else {
            null
        }
    }
}