package net.perfectdreams.dreammochilas.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.transactions.transaction

class GetPlayerMochilasByItemExecutor(val m: DreamMochilas) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val playerName = quotableString("player_name")
        val page = optionalInteger("page")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val playerName = args[options.playerName]
        val player = context.requirePlayer()
        val page = args[options.page] ?: 1
        val pageZeroIndexed = page - 1
        val itemInMainHand = player.inventory.itemInMainHand

        m.launchAsyncThread {
            val uniqueId = DreamUtils.retrieveUserUniqueId(playerName)

            // Because we need to filter on the server, we can't limit/offset the mochilas on here
            val mochilas = transaction(Databases.databaseNetwork) {
                Mochila.find {
                    Mochilas.owner eq uniqueId
                }
                    .toMutableList()
            }

            onMainThread {
                // Now we parse the items in the mochila
                val mochilasThatContainTheItem = mochilas.filter {
                    val itemIndexToBase64ItemStack = Json.decodeFromString<Map<Int, String?>>(it.content)

                    var hasItem = false

                    for ((_, base64ItemStack) in itemIndexToBase64ItemStack) {
                        if (base64ItemStack != null) {
                            if (itemInMainHand.isSimilar(ItemUtils.deserializeItemFromBase64(base64ItemStack))) {
                                hasItem = true
                                break
                            }
                        }
                    }

                    hasItem
                }

                val mochilasThatContainTheItemFilteredByPage = mochilasThatContainTheItem.drop(pageZeroIndexed * 54).take(54)

                if (mochilasThatContainTheItemFilteredByPage.isEmpty()) {
                    context.sendMessage("§cNão tem nenhuma mochila na página $page nas mochilas de $uniqueId!")
                    return@onMainThread
                }

                context.sendMessage("§aCriando inventário com mochilas de $uniqueId")

                val inventory = Bukkit.createInventory(null, 54)
                mochilasThatContainTheItemFilteredByPage.forEach {
                    inventory.addItem(
                        it.createItem()
                    )
                }

                player.openInventory(inventory)

                context.sendMessage("§7É possível pular entradas usando §6/mochila playermochilaitem $playerName Página")
            }
        }
    }
}