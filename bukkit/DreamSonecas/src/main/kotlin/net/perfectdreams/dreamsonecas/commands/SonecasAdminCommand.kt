package net.perfectdreams.dreamsonecas.commands

import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.exposed.ilike
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamsonecas.DreamSonecas
import net.perfectdreams.dreamsonecas.SonecasUtils
import net.perfectdreams.dreamsonecas.tables.PlayerSonecas
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.*


class SonecasAdminCommand(val m: DreamSonecas) : SparklyCommandDeclarationWrapper {
    companion object {
        fun prefix() = textComponent {
            append("[") {
                color(NamedTextColor.DARK_GRAY)
            }

            append("Divine") {
                color(NamedTextColor.DARK_PURPLE)
                decorate(TextDecoration.BOLD)
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

    override fun declaration() = sparklyCommand(listOf("divinesonecas")) {
        subcommand(listOf("give", "dar")) {
            permission = "dreamsonecas.divinesonecas.give"
            executor = SonecasGiveExecutor(m)
        }

        subcommand(listOf("take", "remover")) {
            permission = "dreamsonecas.divinesonecas.take"
            executor = SonecasTakeExecutor(m)
        }

        subcommand(listOf("set")) {
            permission = "dreamsonecas.divinesonecas.set"
            executor = SonecasSetExecutor(m)
        }
    }

    class SonecasGiveExecutor(val m: DreamSonecas) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = word("player_name") { context, builder ->
                transaction(Databases.databaseNetwork) {
                    PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .select(Users.username)
                        .where { Users.username ilike builder.remaining.replace("%", "") + "%" }
                        .limit(10)
                        .map { it[Users.username] }
                        .distinct()
                        .forEach {
                            builder.suggest(it)
                        }
                }
            }

            val quantity = word("quantity")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val receiverName = args[options.playerName]
            val quantity = SonecasUtils.convertShortenedNumberToLong(args[options.quantity])

            if (quantity == null || quantity == 0.0) {
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
                val result = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    // Does the other account exist?
                    val receiverData = Users.selectAll().where { Users.username eq receiverName }.firstOrNull()

                    // The other user does not exist at all!
                    if (receiverData == null)
                        return@transaction Result.UserDoesNotExist

                    // We need to manually create the account if the user does not have a sonecas account yet
                    // We don't need to create an account for the current user because if the player has != 0.0 then it means that they have an account already
                    if (PlayerSonecas.selectAll().where { PlayerSonecas.id eq receiverData[Users.id].value }.count() == 0L) {
                        PlayerSonecas.insert {
                            it[PlayerSonecas.id] = receiverData[Users.id].value
                            it[PlayerSonecas.money] = BigDecimal.ZERO
                            it[PlayerSonecas.updatedAt] = Instant.now()
                        }
                    }

                    // Give money!!!
                    val receiverUpdateReturningStatement = PlayerSonecas.updateReturning(listOf(PlayerSonecas.money), { PlayerSonecas.id eq receiverData[Users.id] }) {
                        with(SqlExpressionBuilder) {
                            it[PlayerSonecas.money] = PlayerSonecas.money + quantity.toBigDecimal()
                        }
                        it[PlayerSonecas.updatedAt] = Instant.now()
                    }.first()
                    val receiverMoney = receiverUpdateReturningStatement[PlayerSonecas.money]

                    return@transaction Result.Success(
                        receiverData[Users.username],
                        receiverData[Users.id].value,
                        quantity
                    )
                }

                onMainThread {
                    when (result) {
                        Result.UserDoesNotExist -> {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Player não existe! Verifique se você colocou o nome do player corretamente.")
                            }
                            return@onMainThread
                        }
                        is Result.Success -> {
                            context.sendMessage {
                                color(NamedTextColor.GREEN)
                                append(prefix())
                                appendSpace()

                                append("A intervenção divina foi realizada com sucesso! ")
                                append(NamedTextColor.AQUA, result.receiverName)
                                append(" recebeu ")
                                append(NamedTextColor.WHITE, "\uE283")
                                appendSpace()
                                append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.quantityGiven))
                                append("!")
                            }
                            return@onMainThread
                        }
                    }
                }
            }
        }

        sealed class Result {
            data object UserDoesNotExist : Result()
            data class Success(
                val receiverName: String,
                val receiverId: UUID,
                val quantityGiven: Double
            ) : Result()
        }
    }

    class SonecasTakeExecutor(val m: DreamSonecas) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = word("player_name") { context, builder ->
                transaction(Databases.databaseNetwork) {
                    PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .select(Users.username)
                        .where { Users.username ilike builder.remaining.replace("%", "") + "%" }
                        .limit(10)
                        .map { it[Users.username] }
                        .distinct()
                        .forEach {
                            builder.suggest(it)
                        }
                }
            }

            val quantity = word("quantity")
        }


        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val receiverName = args[options.playerName]
            val quantity = SonecasUtils.convertShortenedNumberToLong(args[options.quantity])

            if (quantity == null || quantity == 0.0) {
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
                val result = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    // Does the other account exist?
                    val receiverData = Users.selectAll().where { Users.username eq receiverName }.firstOrNull()

                    // The other user does not exist at all!
                    if (receiverData == null)
                        return@transaction Result.UserDoesNotExist

                    // We need to manually create the account if the user does not have a sonecas account yet
                    // We don't need to create an account for the current user because if the player has != 0.0 then it means that they have an account already
                    if (PlayerSonecas.selectAll().where { PlayerSonecas.id eq receiverData[Users.id].value }.count() == 0L) {
                        PlayerSonecas.insert {
                            it[PlayerSonecas.id] = receiverData[Users.id].value
                            it[PlayerSonecas.money] = BigDecimal.ZERO
                            it[PlayerSonecas.updatedAt] = Instant.now()
                        }
                    }

                    // Take money!!!
                    val receiverUpdateReturningStatement = PlayerSonecas.updateReturning(listOf(PlayerSonecas.money), { PlayerSonecas.id eq receiverData[Users.id] }) {
                        with(SqlExpressionBuilder) {
                            it[PlayerSonecas.money] = PlayerSonecas.money - quantity.toBigDecimal()
                        }
                        it[PlayerSonecas.updatedAt] = Instant.now()
                    }.first()
                    val receiverMoney = receiverUpdateReturningStatement[PlayerSonecas.money]

                    var resettedToZero = false
                    if (BigDecimal.ZERO > receiverMoney) {
                        PlayerSonecas.updateReturning(listOf(PlayerSonecas.money), { PlayerSonecas.id eq receiverData[Users.id] }) {
                            with(SqlExpressionBuilder) {
                                it[PlayerSonecas.money] = BigDecimal.ZERO
                            }
                            it[PlayerSonecas.updatedAt] = Instant.now()
                        }.first()
                        resettedToZero = true
                    }

                    return@transaction Result.Success(
                        receiverData[Users.username],
                        receiverData[Users.id].value,
                        quantity,
                        resettedToZero
                    )
                }

                onMainThread {
                    when (result) {
                        Result.UserDoesNotExist -> {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Player não existe! Verifique se você colocou o nome do player corretamente.")
                            }
                            context.sendMessage {
                                content("Usuário não existe!")
                            }
                            return@onMainThread
                        }
                        is Result.Success -> {
                            context.sendMessage {
                                color(NamedTextColor.GREEN)
                                append(prefix())
                                appendSpace()

                                append("A intervenção divina foi realizada com sucesso! ")
                                append(NamedTextColor.AQUA, result.receiverName)
                                append(" teve ")
                                append(NamedTextColor.WHITE, "\uE283")
                                appendSpace()
                                append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.quantityTaken))
                                append(" removidas!")
                            }

                            if (result.resettedToZero) {
                                context.sendMessage {
                                    color(NamedTextColor.YELLOW)
                                    append(prefix())
                                    appendSpace()

                                    append("Como o player ficaria com sonecas negativas, nós resetamos dele para zero!")
                                }
                            }
                            return@onMainThread
                        }
                    }
                }
            }
        }

        sealed class Result {
            data object UserDoesNotExist : Result()
            data class Success(
                val receiverName: String,
                val receiverId: UUID,
                val quantityTaken: Double,
                val resettedToZero: Boolean
            ) : Result()
        }
    }

    class SonecasSetExecutor(val m: DreamSonecas) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = word("player_name") { context, builder ->
                transaction(Databases.databaseNetwork) {
                    PlayerSonecas.innerJoin(Users, { PlayerSonecas.id }, { Users.id })
                        .select(Users.username)
                        .where { Users.username ilike builder.remaining.replace("%", "") + "%" }
                        .limit(10)
                        .map { it[Users.username] }
                        .distinct()
                        .forEach {
                            builder.suggest(it)
                        }
                }
            }

            val quantity = word("quantity")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val receiverName = args[options.playerName]
            val quantity = SonecasUtils.convertShortenedNumberToLong(args[options.quantity])

            if (quantity == null || quantity == 0.0) {
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
                val result = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    // Does the other account exist?
                    val receiverData = Users.selectAll().where { Users.username eq receiverName }.firstOrNull()

                    // The other user does not exist at all!
                    if (receiverData == null)
                        return@transaction Result.UserDoesNotExist

                    // We need to manually create the account if the user does not have a sonecas account yet
                    // We don't need to create an account for the current user because if the player has != 0.0 then it means that they have an account already
                    if (PlayerSonecas.selectAll().where { PlayerSonecas.id eq receiverData[Users.id].value }.count() == 0L) {
                        PlayerSonecas.insert {
                            it[PlayerSonecas.id] = receiverData[Users.id].value
                            it[PlayerSonecas.money] = BigDecimal.ZERO
                            it[PlayerSonecas.updatedAt] = Instant.now()
                        }
                    }

                    // Give money!!!
                    val receiverUpdateReturningStatement = PlayerSonecas.updateReturning(listOf(PlayerSonecas.money), { PlayerSonecas.id eq receiverData[Users.id] }) {
                        with(SqlExpressionBuilder) {
                            it[PlayerSonecas.money] = quantity.toBigDecimal()
                        }
                        it[PlayerSonecas.updatedAt] = Instant.now()
                    }.first()
                    val receiverMoney = receiverUpdateReturningStatement[PlayerSonecas.money]

                    return@transaction Result.Success(
                        receiverData[Users.username],
                        receiverData[Users.id].value,
                        quantity
                    )
                }

                onMainThread {
                    when (result) {
                        Result.UserDoesNotExist -> {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Player não existe! Verifique se você colocou o nome do player corretamente.")
                            }
                            context.sendMessage {
                                content("Usuário não existe!")
                            }
                            return@onMainThread
                        }
                        is Result.Success -> {
                            context.sendMessage {
                                color(NamedTextColor.GREEN)
                                append(prefix())
                                appendSpace()

                                append("A intervenção divina foi realizada com sucesso! ")
                                append(NamedTextColor.AQUA, result.receiverName)
                                append(" agora tem exatamente ")
                                append(NamedTextColor.WHITE, "\uE283")
                                appendSpace()
                                append(NamedTextColor.GREEN, SonecasUtils.formatSonecasAmountWithCurrencyName(result.quantityGiven))
                                append("!")
                            }
                            return@onMainThread
                        }
                    }
                }
            }
        }

        sealed class Result {
            data object UserDoesNotExist : Result()
            data class Success(
                val receiverName: String,
                val receiverId: UUID,
                val quantityGiven: Double
            ) : Result()
        }
    }
}