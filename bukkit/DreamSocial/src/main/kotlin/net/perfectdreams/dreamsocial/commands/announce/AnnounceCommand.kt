package net.perfectdreams.dreamsocial.commands.announce

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamsocial.DreamSocial

class AnnounceCommand(private val plugin: DreamSocial, private val dreamChat: DreamChat) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("anunciar")) {
        executor = AnnounceExecutor(plugin, dreamChat)

        subcommand(listOf("salvar")) {
            executor = SaveAnnouncementExecutor(plugin)
        }
    }
}