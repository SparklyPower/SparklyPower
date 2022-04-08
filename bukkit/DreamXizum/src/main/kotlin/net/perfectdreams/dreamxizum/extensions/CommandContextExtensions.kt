package net.perfectdreams.dreamxizum.extensions

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamvanish.DreamVanishAPI
import net.perfectdreams.dreamxizum.battle.Battle
import net.perfectdreams.dreamxizum.battle.BattleStage
import org.bukkit.entity.Player

fun CommandContext.requireAuthoringBattle(player: Player): Battle {
    val battle = player.battle ?: fail("§cVocê precisa estar em um xizum para usar esse comando.")
    if (battle.stage == BattleStage.CREATING_BATTLE) fail("§cTermine de criar o xizum primeiro.")
    if (battle.author != player) fail("§cVocê não tem permissão para usar esse comando nesse xizum.")
    if (battle.stage > BattleStage.WAITING_PLAYERS) fail("§cTarde de mais para isso.")
    return battle
}

fun CommandContext.requireAvailablePlayer(player: Player) {
    player.battle?.let {
        if (it.stage == BattleStage.CREATING_BATTLE) fail("§c${player.name} está criando um xizum.")
        fail("§c${player.name} está lutando em um xizum.")
    }
    if (!player.available) fail("§c${player.name} não está disponível para ser convidad${player.artigo}.")
}

fun CommandContext.requireDifferentPlayers(firstPlayer: Player, secondPlayer: Player) {
    if (firstPlayer == secondPlayer) fail("§cVocê não pode usar esse comando em você mesmo.")
}

fun CommandContext.requireUnvanishedPlayer(player: Player, message: String) {
    if (DreamVanishAPI.isQueroTrabalhar(player)) {
        player.sendMessage("§c$message")
        fail(CommandArguments.PLAYER_NOT_FOUND.invoke())
    }
}