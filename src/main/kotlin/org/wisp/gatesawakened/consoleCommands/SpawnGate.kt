package org.wisp.gatesawakened.consoleCommands

import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.di

class SpawnGate : BaseCommand {
    override fun runCommand(args: String?, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign || di.sector.playerFleet.isInHyperspace) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        if (di.sector.playerFleet.containingLocation.getEntitiesWithTag(Tags.GATE).any()) {
            Console.showMessage("Multiple Gates in a system are not supported at the moment.")
            return BaseCommand.CommandResult.ERROR
        }

        return if (Common.spawnGateAtLocation(di.sector.playerFleet, activateAfterSpawning = false) != null) {
            Console.showMessage("Gate created!")
            BaseCommand.CommandResult.SUCCESS
        } else {
            BaseCommand.CommandResult.ERROR
        }
    }
}