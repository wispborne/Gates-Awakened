package org.wisp.gatesawakened.createGate

import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.di

object CreateGateQuest {

    fun shouldOfferQuest(): Boolean =
        !hasQuestBeenStarted
                && !wasQuestCompleted
                && (1..10).random() == 7 // 10% chance lol

    fun placeGateAtPlayerLocation() {
        // TODO
    }

    val hasQuestBeenStarted: Boolean
        get() = di.memory[Memory.CREATE_GATE_QUEST_IN_PROGRESS] == true
                || di.memory[Memory.CREATE_GATE_QUEST_DONE] == true

    val wasQuestCompleted: Boolean
        get() = di.memory[Memory.CREATE_GATE_QUEST_DONE] == true
}