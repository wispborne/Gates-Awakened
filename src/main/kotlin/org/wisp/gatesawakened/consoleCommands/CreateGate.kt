package org.wisp.gatesawakened.consoleCommands

import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.gatesawakened.createGate.CreateGateQuest
import org.wisp.gatesawakened.di

class CreateGate : BaseCommand {
    override fun runCommand(args: String?, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign || di.sector.playerFleet.isInHyperspace) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        if (di.sector.playerFleet.containingLocation.getEntitiesWithTag(Tags.GATE).any()) {
            Console.showMessage("Multiple Gates in a system are not supported at the moment.")
            return BaseCommand.CommandResult.ERROR
        }

        return if (CreateGateQuest.spawnGateAtLocation(di.sector.playerFleet, activateAfterSpawning = false)) {
            Console.showMessage("Gate created!")
            Console.showMessage("- Wisp")
            BaseCommand.CommandResult.SUCCESS
        } else {
            BaseCommand.CommandResult.ERROR
        }
    }
}