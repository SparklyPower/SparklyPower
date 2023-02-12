package net.perfectdreams.dreamsocial.commands.profile

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamsocial.DreamSocial

class ProfileCommand(private val plugin: DreamSocial) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("perfil")) {
        executor = ProfileExecutor(plugin)
    }
}