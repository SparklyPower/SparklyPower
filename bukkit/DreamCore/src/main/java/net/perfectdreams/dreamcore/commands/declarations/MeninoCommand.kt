package net.perfectdreams.dreamcore.commands.declarations

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.commands.MeninoExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class MeninoCommand(val plugin: DreamCore) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("menino", "homem", "garoto", "boy")) {
        executor = MeninoExecutor(plugin)
    }
}