package org.wisp.gatesawakened.consoleCommands

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.intro.Intro

class GatesAwakenedForceStartIntroQuest : BaseCommand {
    override fun runCommand(args: String?, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        if (Intro.hasQuestBeenStarted) {
            Console.showMessage("Intro quest has already been started!")
            return BaseCommand.CommandResult.ERROR
        }

        if (!Intro.haveGatesBeenTagged()) {
            Intro.findAndTagIntroGatePair()
        }

        return if (Intro.startQuest(
                di.sector.currentLocation?.planets?.firstOrNull()
                    ?: di.sector.starSystems.first { it.planets.any() }.planets.first()
            )
        ) {
            Console.showMessage("Quest started. Enjoy!")
            BaseCommand.CommandResult.SUCCESS
        } else {
            BaseCommand.CommandResult.ERROR
        }
    }
}