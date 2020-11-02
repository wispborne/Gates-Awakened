package org.wisp.gatesawakened.consoleCommands

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.gatesawakened.Common
import org.wisp.gatesawakened.createGate.CreateGateQuest
import org.wisp.gatesawakened.di
import org.wisp.gatesawakened.intro.IntroQuest
import org.wisp.gatesawakened.midgame.Midgame

class GatesAwakenedViewInfo : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        val info = StringBuilder()

        info.appendln("Gates Awakened Debug Info")
        info.appendln("-------")
        info.appendln("Intro quest gates chosen? ${IntroQuest.haveGatesBeenTagged()}")
        info.appendln("Intro quest started? ${IntroQuest.hasQuestBeenStarted}")
        info.appendln("Intro quest done? ${IntroQuest.wasQuestCompleted}")
        info.appendln("Intro quest core gate: ${IntroQuest.coreGate?.fullName} in ${IntroQuest.coreGate?.containingLocation?.name}")
        info.appendln("Intro quest fringe gate: ${IntroQuest.fringeGate?.fullName} in ${IntroQuest.fringeGate?.containingLocation?.name}")
        info.appendln("-------")
        info.appendln("Is considered midgame? ${Midgame.isMidgame()}")
        info.appendln("  For the second quest to appear, one of the below criteria must be met:")
        info.appendln("  - You have ${di.sector.playerFleet.fleetPoints} of ${Midgame.PREREQ_FLEET_POINTS} fleet points.")
        info.appendln(
            "  - You have ${Common.establishedPlayerColonyCount(Midgame.PREREQ_COLONY_ESTABLISHED_DAYS)}" +
                    " of ${Midgame.PREREQ_COLONIES} colonies 60 days or older."
        )
        info.appendln("Midgame quest started? ${Midgame.hasQuestBeenStarted}")
        info.appendln("Midgame quest done? ${Midgame.wasQuestCompleted}")
        info.appendln("Midgame quest planet chosen? ${Midgame.hasPlanetWithCacheBeenTagged()}")
        info.appendln("Midgame quest planet: ${Midgame.planetWithCache?.fullName} in ${Midgame.planetWithCache?.containingLocation?.name}")
        info.appendln("Midgame quest total codes allowed: ${Midgame.midgameRewardActivationCodeCount}")
        info.appendln("Midgame quest total codes remaining: ${Midgame.remainingActivationCodes}")
        info.appendln("-------")
        info.appendln("Is considered endgame? ${CreateGateQuest.isEndgame()}")
        info.appendln("  For the third quest to appear, one of the below criteria must be met:")
        info.appendln("  - You have ${di.sector.playerFleet.fleetPoints} of ${CreateGateQuest.PREREQ_FLEET_POINTS} fleet points.")
        info.appendln(
            "  - You have ${Common.establishedPlayerColonyCount(CreateGateQuest.PREREQ_COLONY_ESTABLISHED_DAYS)}" +
                    " of ${CreateGateQuest.PREREQ_COLONIES} colonies 60 days or older."
        )
        info.appendln("Create gate quest started? ${CreateGateQuest.hasQuestBeenStarted}")
        info.appendln("Create gate quest done? ${CreateGateQuest.wasQuestCompleted}")
        info.appendln("Create gate quest days needed to deliver a gate: ${CreateGateQuest.numberOfDaysToDeliverGate}")
        info.appendln("Create gate quest timestamp of gate summoning: ${CreateGateQuest.gateSummonedTimestamp} (Current timestamp: ${di.sector.clock.timestamp})")
        info.appendln("Create gate quest location to place gate: ${CreateGateQuest.summonLocation?.fullName} in ${CreateGateQuest.summonLocation?.containingLocation?.name}")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}