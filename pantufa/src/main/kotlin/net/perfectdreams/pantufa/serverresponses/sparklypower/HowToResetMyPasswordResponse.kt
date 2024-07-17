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

            return AutomatedSupportResponse.AutomatedSupportPantufaReplyResponse(
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
        } else {
            return AutomatedSupportResponse.AutomatedSupportMessageResponse {
                embed {
                    title = "Quero trocar a minha senha!"

                    description = """<:nd_or:1013294801044656129> **Para esse suporte é preciso seguir essas informações:**

<:nd_or:1013294801044656129> **Se você é registrado**, você pode usar o comando `/changepass` da Pantufa no <#830658622383980545>.

<:nd_or:1013294801044656129> **Caso não seja registrado**: Você é Minecraft: Bedrock Edition (Mobile, Consoles ou Bedrock Edition no PC) ou Minecraft: Java Edition?

<:nb_or:1013294515932639232><:nde_or:1013294832032153752> **Se você é mobile**, basta conectar a sua conta do Xbox (que tem a sua gamertag) ao seu Discord e avise a Staff.

<:nb_or:1013294515932639232><:nde_or:1013294832032153752> **Se você é PC**, seu Minecraft é original ou pirata?
<:nb_or:1013294515932639232><:nh_or:1013294856593997955><:nde_or:1013294832032153752> __Se ele é original__, você precisa enviar uma print da tela inicial do seu launcher, onde apareça o seu nickname.
<:nb_or:1013294515932639232><:nh_or:1013294856593997955><:nde_or:1013294832032153752> __Se ele é pirata__, você não tem como resetar a senha por falta de autenticação. Você pode criar outra conta para jogar ou provar que a conta é sua."""

                    color = 15567872
                }
            }
        }
    }
}