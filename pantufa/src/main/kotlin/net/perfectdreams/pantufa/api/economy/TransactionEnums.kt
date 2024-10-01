package net.perfectdreams.pantufa.api.economy

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

enum class TransactionType(val title: String, val description: String) {
    PAYMENT("Pagamentos", "transferências entre usuários"),
    BUY_SHOP_ITEM("Compras em Lojas", "compras em lojas"),
    SELL_SHOP_ITEM("Vendas em Lojas", "vendas em lojas"),
    VOTE_REWARDS("Recompensa de Votos", "recompensa por votações"),
    BETTING("Apostas", "gastos e ganhos em apostas"),
    EVENTS("Eventos", "ganhos em eventos do servidor"),
    SECRET_BOXES("Caixas Secretas", "recompensas em caixas secretas"),
    LSX("LSX", "conversões entre sonhos e sonecas"),
    UNSPECIFIED("Não específicado", "sem motivo específico"),
    PESADELOS_BUNDLE_PURCHASE("Compra de Pesadelos", "compras de pesadelos")
}