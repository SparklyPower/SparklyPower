package net.perfectdreams.dreamblockparty.commands

import net.perfectdreams.dreamblockparty.DreamBlockParty
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DreamBlockPartyStartCommand(val m: DreamBlockParty) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamblockparty")) {
        permission = "dreamblockparty.setup"

        executor = StartBlockPartyExecutor(m)
    }

    class StartBlockPartyExecutor(val m: DreamBlockParty) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.eventoBlockParty.preStart()
        }
    }
}