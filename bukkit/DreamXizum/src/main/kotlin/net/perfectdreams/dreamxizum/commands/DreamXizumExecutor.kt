package net.perfectdreams.dreamxizum.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.battle.BattleStage
import net.perfectdreams.dreamxizum.battle.BattleType
import net.perfectdreams.dreamxizum.dao.Combatant
import net.perfectdreams.dreamxizum.extensions.battle
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player

class DreamXizumReloadExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamXizumReloadExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        plugin.loadConfig()
        context.sendMessage("${DreamXizum.PREFIX} A configuração do plugin foi recarregada com sucesso.")
    }
}

class DreamXizumCancelExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamXizumCancelExecutor::class) {
        object Options : CommandOptions() {
            val player = player("player").register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        with (args.getAndValidate(options.player)) {
            battle?.let {
                if (it.stage == BattleStage.CREATING_BATTLE) context.fail("§c$name ainda está criando seu xizum.")
                else if (it.stage == BattleStage.FINISHED) context.fail("§cO xizum de $name já terminou.")
                Matchmaker.cancelBattle(it)
                context.sendMessage("${DreamXizum.PREFIX} Você cancelou o xizum de ${highlight(name)}.")
            } ?: context.fail("§c$name não está em um xizum.")
        }
    }
}

class DreamXizumBanExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamXizumBanExecutor::class) {
        object Options : CommandOptions() {
            val player = word("player").register()
        }

        override val options = Options
    }

    private val cooldowns = mutablePlayerMapOf<Long>()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        plugin.schedule {
            val name = args[options.player]
            val target = Bukkit.getOfflinePlayer(name)
            val onlinePlayer = target.player

            if (!target.hasPlayedBefore()) return@schedule context.sendMessage("§c$name nunca jogou no SparklyPower.")

            switchContext(SynchronizationContext.ASYNC)

            val combatant = Combatant.fetch(target.uniqueId)

            switchContext(SynchronizationContext.SYNC)

            if (combatant.banned) return@schedule context.sendMessage("§c$name já está banido do modo competitivo.")

            val lastUse = cooldowns[player] ?: 0L
            val now = System.currentTimeMillis()

            if (now - lastUse < 10_000) {
                switchContext(SynchronizationContext.ASYNC)

                combatant.ban()

                switchContext(SynchronizationContext.SYNC)

                plugin.server.onlinePlayers.forEach { p ->
                    for (i in 0..10) p.playSound(p.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10F, 1F)
                    p.sendMessage("${DreamXizum.PREFIX} ${highlight(name)} foi permanentemente banido de participar do modo competitivo.")
                }

                onlinePlayer?.let {
                    it.battle?.let { battle -> if (battle.type == BattleType.RANKED) Matchmaker.cancelBattle(battle) }
                    Matchmaker.removeFromQueue(it)
                }
            } else {
                cooldowns[player] = now
                context.sendMessage("§eTem certeza que deseja banir §n$name§e do modo competitivo? " +
                            "As vitórias e pontos da temporada atual serão deletados. Essa ação é irreversível. Digite o comando novamente para confirmar.")
            }
        }
    }
}
