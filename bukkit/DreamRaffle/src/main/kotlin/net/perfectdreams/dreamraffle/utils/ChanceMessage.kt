package net.perfectdreams.dreamraffle.utils

import net.perfectdreams.dreamcore.utils.extensions.percentage
import net.perfectdreams.dreamraffle.raffle.Colors

fun Double.createMessage(colors: Colors, pronoun: String) =
    when {
        this == 1.0 -> "Tendo comprado ${colors.highlight("todos")} os tickets da rifa, estranho seria se $pronoun perdesse."
        this >= 0.9 -> "Comprando um monte de tickets até ter ${colors.highlight(percentage)} fica fácil."
        this >= 0.65 -> "Tendo comprado ${colors.highlight(percentage)} dos tickets ajuda bastante."
        this >= 0.51 -> "Nem pra deixar uma chance pros outros, para que comprar ${colors.highlight(percentage)} dos tickets?"
        this >= 0.5 -> "Se eu tivesse comprado ${colors.highlight("metade")} dos tickets eu também ganharia."
        this >= 0.4 -> "Mas que gosto por apostas, hein? Comprou ${colors.highlight(percentage)} dos tickets."
        this >= 0.3 -> "Diria que foi sorte, mas ${colors.highlight(percentage)} de chance de vencer é até que alto."
        this >= 0.25 -> "${colors.highlight(percentage)} dos tickets? Haja dinheiro para bancar esse vício."
        this >= 0.15 -> "Tenho pena de quem comprou muitos tickets, imagina vencer com só ${colors.highlight(percentage)} de chance?"
        this >= 0.1 -> "Quando ninguém acreditava, $pronoun conseguiu vencer com ${colors.highlight(percentage)} de chance!"
        this >= 0.05 -> "Só ${colors.highlight(percentage)} de chance de vitória?! Vá usar essa sorte para algo mais produtivo, como jogar na mega-sena."
        else -> "Venceu com ${colors.highlight(percentage)} de chance? Com essa sorte toda deveria estar apostando na loteria."
    }