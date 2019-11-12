package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.EveryFrameScript
import org.wisp.gatesawakened.di

class CountdownToGateHaulerScript : EveryFrameScript {
    private var isDone = false

    override fun runWhilePaused(): Boolean = false

    override fun isDone(): Boolean = isDone

    override fun advance(amount: Float) {
        CreateGateQuest.gateSummonedTimestamp?.let { timeHaulerSummoned ->
            val isPlayerInSameSystemAsTargetLocation =
                di.sector.playerFleet.containingLocation == CreateGateQuest.summonLocation?.containingLocation
            if (!isDone
                && di.sector.clock.getElapsedDaysSince(timeHaulerSummoned) >= CreateGateQuest.numberOfDaysToDeliverGate
                && !isPlayerInSameSystemAsTargetLocation
            ) {
                CreateGateQuest.spawnGateAtDesignatedLocation()
                CreateGateQuest.completeQuest()

                isDone = true
            }
        }
    }
}