package net.perfectdreams.dreamsonecas.commands

import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.TransactionContext
import net.perfectdreams.dreamcore.utils.TransactionType
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.runCommandOnClick
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.exposed.ilike
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamsonecas.DreamSonecas
import net.perfectdreams.dreamsonecas.SonecasUtils
import net.perfectdreams.dreamsonecas.tables.PlayerSonecas
import org.bukkit.Bukkit
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SonecasCommand(val m: DreamSonecas) : SparklyCommandDeclarationWrapper {
    companion object {
        private val HANGLOOSE_EMOJIS = listOf(
            "\ue27f",
            "\ue280",
            "\ue281",
            "\ue282"
        )

        private const val AUTOCOMPLETE_PLAYER_NAME_TARGET = 10

        fun prefix() = textComponent {
            append("[") {
                color(NamedTextColor.DARK_GRAY)
            }

            append("Sonecas") {
                color(NamedTextColor.GREEN)
                decorate(TextDecoration.BOLD)
            }

            append("]") {
                color(NamedTextColor.DARK_GRAY)
            }
        }
    }

    override fun declaration() = sparklyCommand(listOf("sonecas", "money", "atm", "dinheiro")) {
        executor = SonecasSelfBalanceExecutor(m, this@SonecasCommand)

        subcommand(listOf("balance", "atm", "saldo", "ver", "olhar")) {
            executor = SonecasBalanceExecutor(m, this@SonecasCommand)
        }

        subcommand(listOf("pay", "pagar")) {
            executor = SonecasPayExecutor(m)
        }

        subcommand(listOf("rank", "top")) {
            executor = SonecasRankExecutor()
        }
    }

    fun showBalance(context: CommandContext, executorUniqueId: UUID?, queryType: QueryType, afterCallback: () -> (Unit)) {
        m.launchAsyncThread {
            val result = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                val userData = Users.select(Users.id, Users.username)
                    .where {
                        when (queryType) {
                            is QueryType.GetByCommandExecutor -> Users.id eq queryType.player.uniqueId
                            is QueryType.GetByName -> Users.username eq queryType.playerName
                        }
                    }
                    .firstOrNull()

                if (userData == null)
                    return@transaction Result.PlayerNotFound

                val playerUniqueId = userData[Users.id].value
                val playerName = userData[Users.username]

                val money = PlayerSonecas.selectAll()
                    .where { PlayerSonecas.id eq playerUniqueId }
                    .firstOrNull()
                    ?.get(PlayerSonecas.money)
                    ?.toDouble() ?: 0.0

                val ranking = if (money > 0.0) {
                    PlayerSonecas.selectAll().where { PlayerSonecas.money greaterEq money.toBigDecimal() }
                        .count()
                } else null

                return@transaction Result.Success(
                    playerUniqueId == executorUniqueId,
                    playerName,
                    money,
                    ranking
                )
            }

            onMainThread {
                when (result) {
                    Result.PlayerNotFound -> {
                        context.sendMessage {
                            color(NamedTextColor.RED)
                            append(prefix())
                            appendSpace()

                            append("Player não existe! Verifique se você colocou o nome do player corretamente.")
                        }
                    }
                    is Result.Success -> {
                        context.sendMessage {
                            color(NamedTextColor.YELLOW)
                            append(prefix())
                            appendSpace()

                            if (result.isSelf) {
                                append("Você possui ")

                                append(NamedTextColor.WHITE, "\uE283")
                                appendSpace()

                                append(
                                    textComponent {
                                        color(NamedTextColor.GREEN)
                                        append(SonecasUtils.formatSonecasAmountWithCurrencyName(result.money))
                                    }
                                )

                                append("!")

                                if (result.sonecasRankPosition != null) {
                                    appendSpace()

                                    append("Você está em #${result.sonecasRankPosition} lugar do ranking, veja outros ostentadores com ")
                                    appendCommand("/sonecas rank")
                                    append("!")
                                }
                            } else {
                                append(NamedTextColor.AQUA, result.playerName)
                                append(" possui ")

                                append(NamedTextColor.WHITE, "\uE283")
                                appendSpace()

                                append(
                                    textComponent {
                                        color(NamedTextColor.GREEN)
                                        append(SonecasUtils.formatSonecasAmountWithCurrencyName(result.money))
                                    }
                                )

                                append("!")

                                if (result.sonecasRankPosition != null) {
                                    appendSpace()

                                    append("E você sabia que ")
                                    append(NamedTextColor.AQUA, result.playerName)
                                    append(" está em #${result.sonecasRankPosition} lugar do ranking? Veja outros ostentadores com ")
                                    appendCommand("/sonecas rank")
                                    append("!")
                                }
                            }
                        }

                        afterCallback.invoke()
                    }
                }
            }
        }
    }

    sealed class QueryType {
        data class GetByCommandExecutor(val player: Player) : QueryType()
        data class GetByName(val playerName: String) : QueryType()
    }

    sealed class Result {
        data object PlayerNotFound : Result()
        data class Success(val isSelf: Boolean, val playerName: String, val money: Double, val sonecasRankPosition: Long?) : Result()
    }

    class SonecasBalanceExecutor(val m: DreamSonecas, val command: SonecasCommand) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = optionalWord("player_name") { context, builder ->
                // Yes, accessing Bukkit.getOnlinePlayers() here is thread safe
                // We can't change the autocomplete order sadly, so to make "online" priority, we need to be a bit tricky with it
                val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }

                val onlinePlayersThatMatchTheQuery = onlinePlayers.filter { it.startsWith(builder.remaining, true) }

                if (onlinePlayersThatMatchTheQuery.isNotEmpty()) {
                    onlinePlayersThatMatchTheQuery.forEach {
                        builder.suggest(it)
                    }
                    return@optionalWord
                }

                val databasePlayers = transaction(Databases.databaseNetwork) {
                    PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .select(Users.username)
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
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val queryType = args[options.playerName]?.let { QueryType.GetByName(it) } ?: QueryType.GetByCommandExecutor(context.requirePlayer())
            val executorUniqueId = (context.sender as? Player)?.uniqueId

            command.showBalance(context, executorUniqueId, queryType) {}
        }
    }

    class SonecasSelfBalanceExecutor(val m: DreamSonecas, val command: SonecasCommand) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val queryType = QueryType.GetByCommandExecutor(context.requirePlayer())
            val executorUniqueId = (context.sender as? Player)?.uniqueId

            command.showBalance(context, executorUniqueId, queryType) {
                context.sendMessage {
                    color(NamedTextColor.YELLOW)
                    append(prefix())
                    appendSpace()

                    append("Veja as sonecas de outro jogador com ")
                    appendCommand("/sonecas atm NomeDoJogador")
                }

                context.sendMessage {
                    color(NamedTextColor.YELLOW)
                    append(prefix())
                    appendSpace()

                    append("Envie sonecas para outro jogador com ")
                    appendCommand("/sonecas pagar NomeDoJogador Quantidade")
                }
            }
        }
    }

    class SonecasPayExecutor(val m: DreamSonecas) : SparklyCommandExecutor() {
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
                    PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .select(Users.username)
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

            val quantity = word("quantity")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val receiverName = args[options.playerName]
            val quantity = SonecasUtils.convertShortenedNumberToLong(args[options.quantity])

            if (quantity == null || quantity == 0.0 || SonecasUtils.formatSonecasAmount(quantity) == "0,00") {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(prefix())
                    appendSpace()

                    append("Uau, incrível! Você vai transferir *zero* sonecas, maravilha! Menos trabalho para mim, porque isso significa que não preciso preparar uma transação para você.")
                }
                return
            }

            if (0.0 > quantity) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(prefix())
                    appendSpace()
                    append("Uau, excelente! Você vai transferir sonecas *negativas*, extraordinário! Será que sonecas negativas seriam... *pesadelos*? Aí que medo não gosto dessas coisas sobrenaturais não... ")
                    appendCommand("/pesadelos")
                }
                return
            }

            // TODO: Check if the quantity is actually valid
            if (quantity.isInfinite() || quantity.isNaN()) {
                context.sendMessage {
                    content("Número inválido!")
                }
                return
            }

            m.launchAsyncThread {
                val result = SonecasUtils.transferSonecasFromPlayerToPlayer(player.uniqueId, receiverName, quantity)

                onMainThread {
                    when (result) {
                        is SonecasUtils.TransferSonhosResult.NotEnoughSonecas -> {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Você não tem ")
                                append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(quantity))
                                append(" para fazer isso! Você precisa conseguir mais ")
                                append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(quantity - result.currentUserMoney))
                                append(" para continuar.")
                            }
                            return@onMainThread
                        }
                        SonecasUtils.TransferSonhosResult.UserDoesNotExist -> {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Player não existe! Verifique se você colocou o nome do player corretamente.")
                            }
                            return@onMainThread
                        }
                        SonecasUtils.TransferSonhosResult.CannotTransferSonecasToSelf -> {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Transferência concluída com sucesso! Você recebeu *nada* de si mesmo, porque você está tentando transferir sonecas para si mesmo! Se você quer uma soneca de verdade, vá dormir.")
                            }
                            return@onMainThread
                        }
                        is SonecasUtils.TransferSonhosResult.Success -> {
                            // This sucks
                            TransactionContext(
                                payer = player.uniqueId,
                                receiver = result.receiverId,
                                type = TransactionType.PAYMENT,
                                amount = result.quantityGiven
                            ).saveToDatabase()

                            context.sendMessage {
                                color(NamedTextColor.GREEN)
                                append(prefix())
                                appendSpace()

                                append("Transferência realizada com sucesso! ")
                                append(NamedTextColor.AQUA, result.receiverName)
                                append(" recebeu ")
                                append(NamedTextColor.WHITE, "\uE283")
                                appendSpace()
                                append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.quantityGiven))
                                append("!")
                            }

                            context.sendMessage {
                                append(prefix())
                                appendSpace()
                                append(HANGLOOSE_EMOJIS.random())
                                append(
                                    textComponent {
                                        color(NamedTextColor.YELLOW)

                                        appendSpace()
                                        append("Você agora possui ")
                                        append(NamedTextColor.WHITE, "\uE283")
                                        appendSpace()
                                        append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.selfMoney))
                                        append(" e está em #${result.selfRanking} lugar no ranking!")
                                    }
                                )
                            }

                            player.playSound(player.location, "sparklypower.sfx.money", SoundCategory.RECORDS, 1.0f, DreamUtils.random.nextFloat(0.9f, 1.1f))

                            // Is the player online?
                            val receiverPlayer = Bukkit.getPlayerExact(result.receiverName)
                            if (receiverPlayer != null) {
                                receiverPlayer.sendMessage(
                                    textComponent {
                                        color(NamedTextColor.GREEN)
                                        append(prefix())
                                        appendSpace()

                                        append("Você recebeu ")
                                        append(NamedTextColor.WHITE, "\uE283")
                                        appendSpace()
                                        append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.quantityGiven))
                                        append(" de ")
                                        append(NamedTextColor.AQUA, player.name)
                                        append("!")
                                    }
                                )

                                receiverPlayer.sendMessage(
                                    textComponent {
                                        color(NamedTextColor.YELLOW)
                                        append(prefix())
                                        appendSpace()

                                        append("Você agora possui ")
                                        append(NamedTextColor.WHITE, "\uE283")
                                        appendSpace()
                                        append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.receiverMoney))
                                        append(" e está em #${result.receiverRanking} lugar no ranking!")
                                    }
                                )

                                receiverPlayer.playSound(receiverPlayer.location, "sparklypower.sfx.money", SoundCategory.RECORDS, 1.0f, DreamUtils.random.nextFloat(0.9f, 1.1f))
                            }
                            return@onMainThread
                        }
                    }
                }
            }
        }
    }

    inner class SonecasRankExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val page = optionalInteger("page")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            m.launchAsyncThread {
                val page = (args[options.page]?.minus(1))?.coerceAtLeast(0) ?: 0
                val humanizedPage = page + 1
                val offset = (page * 10).toLong()

                val (topSonecas, totalPagesHumanized) = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    val topSonecas = PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .selectAll()
                        .orderBy(PlayerSonecas.money, SortOrder.DESC)
                        .limit(10, (page * 10).toLong())
                        .toList()

                    val totalPagesZeroIndexed = PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .selectAll()
                        .orderBy(PlayerSonecas.money, SortOrder.DESC)
                        .count() / 10

                    Pair(topSonecas, totalPagesZeroIndexed + 1)
                }

                onMainThread {
                    context.sendMessage {
                        if (topSonecas.isEmpty()) {
                            color(NamedTextColor.RED)
                            append(prefix())
                            appendSpace()

                            append("A página que você quer olhar não existe!")
                        } else {
                            append(LegacyComponentSerializer.legacySection().deserialize("§8[ §bOstentadores do SparklyPower §8- §6Página $humanizedPage §8]".centralizeHeader()))
                            appendNewline()
                            appendNewline()

                            for ((index, playerInfo) in topSonecas.withIndex()) {
                                color(NamedTextColor.YELLOW)
                                append(NamedTextColor.DARK_GRAY, "#${offset + index + 1} ")
                                append(playerInfo[Users.username]) {
                                    color(NamedTextColor.AQUA)
                                }
                                append(": ")
                                append(SonecasUtils.formatSonecasAmountWithCurrencyName(playerInfo[PlayerSonecas.money].toDouble())) {
                                    color(NamedTextColor.GREEN)
                                }
                                appendNewline()
                            }

                            if (humanizedPage != 1) {
                                append(
                                    textComponent {
                                        content("«")
                                        color(NamedTextColor.GOLD)
                                        runCommandOnClick("/sonecas rank ${humanizedPage - 1}")
                                    }
                                )
                            } else {
                                appendSpace()
                            }

                            appendSpace()

                            if (totalPagesHumanized >= humanizedPage + 1) {
                                append(
                                    textComponent {
                                        content("»")
                                        color(NamedTextColor.GOLD)
                                        runCommandOnClick("/sonecas rank ${humanizedPage + 1}")
                                    }
                                )
                            } else {
                                appendSpace()
                            }

                            appendNewline()
                            appendNewline()
                            append(LegacyComponentSerializer.legacySection().deserialize("§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-"))
                        }
                    }
                }
            }
        }
    }
}