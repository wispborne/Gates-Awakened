package org.wisp.gatesawakened.createGate

import com.fs.starfarer.api.EveryFrameScript
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.di

class CountdownToGateHaulerScript : EveryFrameScript {
    private var isDone = false

    override fun runWhilePaused(): Boolean = false

    override fun isDone(): Boolean = isDone

    override fun advance(amount: Float) {
        (di.memory[Memory.CREATE_GATE_HAULER_SUMMON_TIMESTAMP] as? Long)?.let { timeHaulerSummoned ->
            if (!isDone && di.sector.clock.getElapsedDaysSince(timeHaulerSummoned) >= CreateGateQuest.numberOfDaysToDeliverGate) {
                CreateGateQuest.spawnGateAtDesignatedLocation()
                isDone = true
            }
        }
    }
}