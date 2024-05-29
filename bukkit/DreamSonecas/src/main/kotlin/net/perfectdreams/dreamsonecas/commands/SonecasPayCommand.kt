package net.perfectdreams.dreamsonecas.commands

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamsonecas.DreamSonecas

class SonecasPayCommand(val m: DreamSonecas) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("pay", "pagar")) {
        executor = SonecasCommand.SonecasPayExecutor(m)
    }
}