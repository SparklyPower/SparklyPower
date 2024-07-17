package net.perfectdreams.pantufa.serverresponses.sparklypower

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.PantufaReply
import net.perfectdreams.pantufa.serverresponses.AutomatedSupportResponse
import net.perfectdreams.pantufa.serverresponses.SVMPantufaResponse
import net.perfectdreams.pantufa.utils.Emotes
import net.perfectdreams.pantufa.utils.svm.*

class HowToResetMyPasswordResponse(val m: PantufaBot, svm: SparklySVM) : SVMPantufaResponse(svm) {
    override fun getSupportResponse(author: User, message: String): AutomatedSupportResponse? {
        // Does the user have an SparklyPower account?
        val discordAccount = m.getDiscordAccountFromId(author.idLong)

        if (discordAccount != null && discordAccount.isConnected) {
            val minecraftAccount = m.getMinecraftUserFromUniqueId(discordAccount.minecraftId)

            return AutomatedSupportResponse(
                listOf(
                    PantufaReply(
                        "Como você tem a conta `${minecraftAccount?.username}` do SparklyPower conectada a sua conta do Discord, você pode trocar a sua senha do SparklyPower usando `/changepass` aqui no Discord do SparklyPower!",
                        Emotes.PantufaCoffee.toString(),
                        mentionUser = false
                    ),
                    PantufaReply(
                        "Após alterar, é só você entrar no SparklyPower com a nova senha e se divertir!",
                        Emotes.PantufaLick.toString(),
                        mentionUser = false
                    )
                )
            )
        }

        return null
    }
}