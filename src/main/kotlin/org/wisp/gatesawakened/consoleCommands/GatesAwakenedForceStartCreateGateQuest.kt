package org.wisp.gatesawakened.consoleCommands

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.gatesawakened.createGate.CreateGateQuest

class GatesAwakenedForceStartCreateGateQuest : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        if (CreateGateQuest.hasQuestBeenStarted == true) {
            Console.showMessage("CreateGateQuest has already been started!")
            return BaseCommand.CommandResult.ERROR
        }

        CreateGateQuest.startQuest()
        Console.showMessage("Quest started. Enjoy!")
        return BaseCommand.CommandResult.SUCCESS
    }
}