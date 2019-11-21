package org.wisp.gatesawakened.createGate

import org.wisp.gatesawakened.questLib.InteractionDefinition

class GateCreatedDialog : InteractionDefinition<GateCreatedDialog>(
    onInteractionStarted = {
        addPara { "It hardly seems possible, but there is now a Gate where there was none before." }
    },
    pages = listOf(
        Page(
            id = 1,
            image = Image(
                category = "illustrations",
                id = "dead_gate"
            ),
            onPageShown = {

            },
            options = listOf()
        )
    )
) {
    override fun createInstanceOfSelf() = GateCreatedDialog()
}