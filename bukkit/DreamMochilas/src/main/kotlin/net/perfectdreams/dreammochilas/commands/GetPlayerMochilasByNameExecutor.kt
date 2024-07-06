package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class GetPlayerMochilasByNameExecutor(val m: DreamMochilas) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val playerName = quotableString("player_name")
        val page = optionalInteger("page")
        val name = optionalGreedyString("mochila_name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val playerName = args[options.playerName]
        val name = args[options.name]
        val player = context.requirePlayer()
        val page = args[options.page] ?: 1
        val pageZeroIndexed = page - 1

        m.launchAsyncThread {
            val uniqueId = DreamUtils.retrieveUserUniqueId(playerName)

            val mochilas = transaction(Databases.databaseNetwork) {
                Mochila.find {
                    Mochilas.owner eq uniqueId and (Mochilas.funnyId eq name)
                }
                    .limit(54, pageZeroIndexed * 54L)
                    .toMutableList()
            }

            onMainThread {
                if (mochilas.isEmpty()) {
                    context.sendMessage("§cNão tem nenhuma mochila com o nome \"$name\" na página $page nas mochilas de $uniqueId!")
                    return@onMainThread
                }

                context.sendMessage("§aCriando inventário com mochilas que possuem o nome \"$name\" de $uniqueId")

                val inventory = Bukkit.createInventory(null, 54)
                mochilas.forEach {
                    inventory.addItem(
                        it.createItem()
                    )
                }

                player.openInventory(inventory)

                context.sendMessage("§7É possível pular entradas usando §6/mochila playermochilaname $playerName Página $name")
            }
        }
    }
}