package org.wisp.gatesawakened.intro

import com.fs.starfarer.api.EveryFrameScript
import org.wisp.gatesawakened.di
import java.util.*

class ShowIntroEpilogueAfterJumpCompletes : EveryFrameScript {
    var wasDialogShown = false
    var playerEnteredSystemTimestamp: Long? = null

    override fun runWhilePaused(): Boolean = false

    override fun isDone(): Boolean = wasDialogShown

    override fun advance(amount: Float) {
        if (!wasDialogShown
            && !di.sector.campaignUI.isShowingDialog
            && !di.sector.campaignUI.isShowingMenu
            && di.sector.playerFleet.containingLocation == IntroQuest.coreGate?.containingLocation
        ) {
            // When player enters system, wait 2 seconds before showing dialog
            if (playerEnteredSystemTimestamp != null
                && Date().time - playerEnteredSystemTimestamp!! > 2000
            ) {
                IntroQuest.displayIntroQuestEpilogueWindow()
                wasDialogShown = true
            } else if(playerEnteredSystemTimestamp == null) {
                playerEnteredSystemTimestamp = Date().time
            }
        }
    }
}