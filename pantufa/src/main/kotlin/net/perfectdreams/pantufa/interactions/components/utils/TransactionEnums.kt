package net.perfectdreams.pantufa.interactions.components.utils

import net.perfectdreams.pantufa.dao.Transaction
import net.perfectdreams.pantufa.utils.extensions.username
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val formatter =  DecimalFormat("###,##0.##", DecimalFormatSymbols(Locale.GERMAN))

enum class TransactionCurrency(val displayName: String) {
    MONEY("sonecas"),
    CASH("pesadelos");

    fun format(amount: Double, name: String = displayName) =
        "**${formatter.format(amount)} ${(if (amount == 1.0) name.dropLast(1) else name)}**"
}

data class TransactionContext(private val transaction: Transaction) {
    val extra = transaction.extra
    val amount = transaction.amount
    val currency = transaction.currency
    val money = currency.format(amount)
    val payer = transaction.payer?.let { "`${it.username}`" }
    fun receiver(pronoun: String = "") = transaction.receiver?.let {  "`${it.username}`" }
}

enum class TransactionType(
    val displayName: String,
    val description: String,
    //val emoji: DiscordPartialEmoji,
    val buildDisplayMessage: (TransactionContext) -> String
) {
    PAYMENT("Pagamentos", "transferências entre usuários", {
        ":money_with_wings: ${it.payer} transferiu ${it.money} para ${it.receiver("você")}"
    }),

    BUY_SHOP_ITEM("Compras em Lojas", "compras em lojas", {
        // Player Shop
        it.receiver("sua")?.run {
            ":moneybag: ${it.payer} comprou `${it.extra}` por ${it.money} em uma loja de $this"
        } ?:
        // Admin Shop
            ":moneybag: ${it.payer} comprou `${it.extra}` por ${it.money} numa loja oficial do servidor"
    }),

    SELL_SHOP_ITEM("Vendas em Lojas", "vendas em lojas", {
        // Player Shop
        it.receiver("sua")?.run {
            ":credit_card: ${it.receiver("Você")} vendeu `${it.extra}` por ${it.money} em uma loja de $this"
        } ?:
        // Admin Shop
            ":credit_card: ${it.receiver("Você")} vendeu `${it.extra}` por ${it.money} numa loja oficial do servidor"
    }),

    VOTE_REWARDS("Recompensas de Votos", "recompensas por votações", {
        ":envelope: `${it.receiver("Você")}` recebeu ${it.money} votando"
    }),

    BETTING("Apostas", "gastos e ganhos em apostas", {
        // If there is a receiver in this transaction, then they have won something
        it.receiver("Você")?.run {
            ":tickets: $this ganhou ${it.money} ${it.extra}"
        } ?:
        // If there is a payer in this transaction, then they have bought some tickets
            ":tickets: ${it.payer} gastou ${it.money} apostando ${it.extra}"
    }),

    EVENTS("Eventos", "ganhos em eventos do servidor", {
        if (it.extra!!.split(' ').size < 4)
            ":dart: ${it.receiver("Você")} recebeu ${it.money} jogando no evento `${it.extra}`"
        else
            ":dart: ${it.receiver("Você")} recebeu ${it.money} ${it.extra}"
    }),

    SECRET_BOXES("Caixas Secretas", "recompensas em caixas secretas", {
       ":gift: ${it.receiver("Você")} recebeu ${it.money} em uma caixa secreta!"
    }),

    LSX("LSX", "conversões entre sonhos e sonecas", {
        // If there is a receiver, then the player has sent money from Loritta to SparklyPower
        it.receiver("Você")?.run {
            val sonhos = it.currency.format(it.amount, "sonhos")
            val sonecas = it.currency.format(it.amount * 2)

            ":euro: $this transferiu $sonhos da Loritta [ID da conta: `${it.extra}`] para o SparklyPower ($sonecas)"
        } ?:
        // If there is a payer, then the player has sent money from SparklyPower to Loritta
        it.payer!!.run {
            val sonhos = it.currency.format(it.amount / 2, "sonhos")

            ":euro: $this transferiu ${it.money} do SparklyPower para a Loritta ($sonhos) [ID da conta: `${it.extra}`]"
        }
    }),

    UNSPECIFIED("Não específicado", "sem motivo específico", {
        "<a:lori_pensando_muito:977282157993140245> ${it.receiver("Você")} recebeu ${it.money} ao ${it.extra}"
    })
}