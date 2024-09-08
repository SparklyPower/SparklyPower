package net.perfectdreams.dreaminventorysnapshots.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.exposed.ilike
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreaminventorysnapshots.DreamInventorySnapshots
import net.perfectdreams.dreaminventorysnapshots.tables.InventorySnapshots
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class InventorySnapshotsCommand(private val m: DreamInventorySnapshots) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("inventorysnapshots")) {
        permission = "dreaminventorysnapshots.setup"

        subcommand(listOf("list")) {
            executor = InventorySnapshotsListExecutor(m)
        }

        subcommand(listOf("restore")) {
            executor = InventorySnapshotsRestoreExecutor(m)
        }
    }

    class InventorySnapshotsListExecutor(private val m: DreamInventorySnapshots) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = word("player_name") { context, builder ->
                // Yes, accessing Bukkit.getOnlinePlayers() here is thread safe
                // We can't change the autocomplete order sadly, so to make "online" priority, we need to be a bit tricky with it
                val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }

                val onlinePlayersThatMatchTheQuery = onlinePlayers.filter { it.startsWith(builder.remaining, true) }

                if (onlinePlayersThatMatchTheQuery.isNotEmpty()) {
                    onlinePlayersThatMatchTheQuery.forEach {
                        builder.suggest(it)
                    }
                    return@word
                }

                val databasePlayers = transaction(Databases.databaseNetwork) {
                    Users.select(Users.username)
                        .where { Users.username ilike builder.remaining.replace("%", "") + "%" }
                        .limit(10)
                        .map { it[Users.username] }
                        .distinct()
                }

                // Then we recheck it here again...
                databasePlayers.forEach {
                    builder.suggest(it)
                }
            }
            val page = optionalInteger("page")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val playerName = args[options.playerName]
            val pageZeroIndexed = (args[options.page] ?: 1) - 1

            m.launchAsyncThread {
                val result = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    val userData = Users.select(Users.id, Users.username)
                        .where {
                            Users.username eq playerName
                        }
                        .firstOrNull()

                    if (userData == null)
                        return@transaction Result.PlayerNotFound

                    val rows = InventorySnapshots.selectAll()
                        .where {
                            InventorySnapshots.playerId eq userData[Users.id].value
                        }
                        .limit(10, pageZeroIndexed * 10L)
                        .toList()

                    return@transaction Result.Success(rows)
                }

                when (result) {
                    is Result.Success -> {
                        context.sendMessage {
                            color(NamedTextColor.AQUA)
                            appendSpace()

                            append("Inventários:")
                            for (inventorySnapshot in result.snapshots) {
                                append("#${inventorySnapshot[InventorySnapshots.id].value}: ${inventorySnapshot[InventorySnapshots.createdAt]}")
                                appendNewline()
                            }
                        }
                    }

                    Result.PlayerNotFound -> {
                        context.sendMessage {
                            color(NamedTextColor.RED)
                            append("Player não existe! Verifique se você colocou o nome do player corretamente.")
                        }
                    }
                }
            }
        }

        private sealed class Result {
            data object PlayerNotFound : Result()
            class Success(val snapshots: List<ResultRow>) : Result()
        }
    }

    class InventorySnapshotsRestoreExecutor(private val m: DreamInventorySnapshots) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val inventoryId = integer("inventory_id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val inventoryId = args[options.inventoryId].toLong()

            m.launchAsyncThread {
                val result = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    InventorySnapshots.selectAll()
                        .where {
                            InventorySnapshots.id eq inventoryId
                        }
                        .firstOrNull()
                }

                if (result == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        append("Snapshot não existe!")
                    }
                    return@launchAsyncThread
                }

                onMainThread {
                    val inventory = Json.decodeFromString<Map<Int, String?>>(result[InventorySnapshots.content])

                    player.inventory.clear()

                    inventory.forEach { index, itemAsBase64 ->
                        if (itemAsBase64 != null) {
                            player.inventory.setItem(index, ItemUtils.deserializeItemFromBase64(itemAsBase64))
                        }
                    }

                    context.sendMessage {
                        color(NamedTextColor.GREEN)
                        append("Inventário #$inventoryId restaurado!")
                    }
                }
            }
        }
    }
}