package net.perfectdreams.dreamxizum.battle

import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import org.bukkit.entity.Player

object DeathMessage {
    private val types = setOf(DeathMessageTypes.SIMPLE, DeathMessageTypes.COMPLEX)

    private val complexReasons = setOf<Message>(
        { "${highlight(it.victim.name)} teve a vida ceifada por ${highlight(it.killer.name)}" },
        { "${highlight(it.victim.name)} chegou ao seu fim nas mãos de ${highlight(it.killer.name)}" },
        { "${highlight(it.victim.name)} foi enviad${it.victim.artigo} para a estratosfera por ${highlight(it.killer.name)}" },
        { "${highlight(it.victim.name)} foi enterrad${it.victim.artigo} após lutar com ${highlight(it.killer.name)}" }
    )

    private val verbs = setOf(
        "mort", "destruíd", "humilhad", "destroçad", "apagad", "encerrad", "obliterad",
        "amassad", "aniquilad", "pulverizad", "deletad"
    )

    fun getRandomMessage(victim: Player, killer: Player) =
        if (types.random() == DeathMessageTypes.COMPLEX) complexReasons.random().invoke(DeathContext(victim, killer)) + "."
        else "${highlight(victim.name)} foi ${verbs.random() + victim.artigo} por ${highlight(killer.name)}."
}

private class DeathContext(val victim: Player, val killer: Player)
private typealias Message = (DeathContext) -> String

enum class DeathMessageTypes {
    SIMPLE,
    COMPLEX
}