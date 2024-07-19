package net.perfectdreams.pantufa.serverresponses.sparklypower

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.PantufaReply
import net.perfectdreams.pantufa.serverresponses.AutomatedSupportResponse
import net.perfectdreams.pantufa.serverresponses.SVMPantufaResponse
import net.perfectdreams.pantufa.utils.Emotes
import net.perfectdreams.pantufa.utils.svm.*

class HowToBuyPesadelosResponse(val m: PantufaBot, svm: SparklySVM) : SVMPantufaResponse(svm) {
    override fun getSupportResponse(author: User, message: String): AutomatedSupportResponse {
        return AutomatedSupportResponse.AutomatedSupportPantufaReplyResponse(
            listOf(
                PantufaReply(
                    "Você pode comprar pesadelos no SparklyPower em nossa loja: https://sparklypower.net/loja",
                    Emotes.Pesadelos.toString(),
                    mentionUser = false
                ),
                PantufaReply(
                    "Após comprar pesadelos, você pode comprar VIP e outras vantagens na `/lojacash`, dentro do SparklyPower",
                    Emotes.PantufaHi.toString(),
                    mentionUser = false
                ),
                PantufaReply(
                    "Nós aceitamos Pix, Cartão de Crédito, Cartão de Débito e Boleto!",
                    Emotes.CreditCard.toString(),
                    mentionUser = false
                ),
                PantufaReply(
                    "Você também pode conseguir pesadelos jogando no SparklyPower! Você ganha pesadelos votando no servidor, ganhando eventos, e muito mais!",
                    Emotes.PantufaLurk.toString(),
                    mentionUser = false
                )
            )
        )
    }
}