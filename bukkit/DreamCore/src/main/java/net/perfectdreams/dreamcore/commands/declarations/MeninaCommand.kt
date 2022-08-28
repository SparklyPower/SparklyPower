package net.perfectdreams.dreamcore.commands.declarations

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.commands.MeninaExecutor
import net.perfectdreams.dreamcore.commands.MeninoExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class MeninaCommand(val plugin: DreamCore) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("menina", "mulher", "garota", "girl")) {
        executor = MeninaExecutor(plugin)
    }
}