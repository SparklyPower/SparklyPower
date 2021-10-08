package net.perfectdreams.dreamcore.commands.declarations

import net.perfectdreams.dreamcore.commands.MeninaExecutor
import net.perfectdreams.dreamcore.commands.MeninoExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

object MeninaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("menina", "mulher", "garota", "girl")) {
        executor = MeninaExecutor
    }
}