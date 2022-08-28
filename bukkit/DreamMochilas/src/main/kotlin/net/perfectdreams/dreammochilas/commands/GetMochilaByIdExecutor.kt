package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.dao.Mochila
import org.jetbrains.exposed.sql.transactions.transaction

class GetMochilaByIdExecutor(val m: DreamMochilas) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
            val mochilaId = integer("mochila_id")
        }

        override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val mochilaId = args[options.mochilaId]

        m.launchAsyncThread {
            val mochila = transaction(Databases.databaseNetwork) {
                Mochila.findById(mochilaId.toLong())
            }

            onMainThread {
                if (mochila == null) {
                    context.sendMessage("§cMochila desconhecida!")
                    return@onMainThread
                }

                player.inventory.addItem(mochila.createItem())

                context.sendMessage("§aMochila recebida!")
            }
        }
    }
}