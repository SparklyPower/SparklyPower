package net.perfectdreams.pantufa.interactions.commands

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.commands.server.LSXCommand
import net.perfectdreams.pantufa.dao.Ban
import net.perfectdreams.pantufa.dao.Profile
import net.perfectdreams.pantufa.interactions.components.utils.TransactionCurrency
import net.perfectdreams.pantufa.interactions.components.utils.TransactionType
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.*
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.NumberUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

class LSXExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    inner class Options : ApplicationCommandOptions() {
        val source = optionalString("source", "Fonte dos Sonhos") {
            choice("survival", "SparklyPower Survival")
            choice("loritta", "Loritta :3")
        }

        val destination = optionalString("destination", "Destino dos Sonhos") {
            choice("survival", "SparklyPower Survival")
            choice("loritta", "Loritta :3")
        }

        val quantity = optionalString("quantity", "Quantidade de sonhos que você deseja transferir")
    }

    override val options = Options()

    private fun getLorittaProfile(userId: Long): Profile {
        return transaction(Databases.loritta) {
            Profile.findById(userId)
        } ?: throw RuntimeException()
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val arg0 = args[options.source]
        val arg1 = args[options.destination]
        val arg2 = args[options.quantity]

        val profile = getLorittaProfile(context.senderId.toLong())
        val bannedState = profile.getBannedState()

        if (bannedState != null) {
            context.reply(
                PantufaReply(
                    "Você está banido de utilizar a Loritta!"
                )
            )
            return
        }

        val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()

        // Check if the user is banned
        val userBan = transaction(Databases.sparklyPower) {
            Ban.find {
                Bans.player eq accountInfo.uniqueId and
                        (
                                Bans.temporary eq false or (
                                        Bans.temporary eq true and
                                                (Bans.expiresAt.isNotNull()) and
                                                (Bans.expiresAt greaterEq System.currentTimeMillis())
                                        )
                                )
            }.firstOrNull()
        }

        if (userBan != null) {
            context.reply(
                PantufaReply(
                    "Você está banido do SparklyPower!"
                )
            )
            return
        }

        val survivalOnlineTrackedHours = pantufa.getPlayerTimeOnlineInTheLastXDays(accountInfo.uniqueId, 30)

        if (Duration.ofHours(24) >= survivalOnlineTrackedHours.duration) {
            context.reply(
                PantufaReply(
                    "Você precisa ter mais de 24 horas online no SparklyPower Survival nos últimos 30 dias (desde ${survivalOnlineTrackedHours.since.toInstant().toKotlinInstant().toMessageFormat(DiscordTimestampStyle.ShortDate)}) antes de poder transferir sonhos! Atualmente você tem ${
                        (survivalOnlineTrackedHours.duration.get(
                            ChronoUnit.SECONDS
                        ).div(3_600))
                    } horas.",
                    "\uD83D\uDCB5"
                )
            )
            return
        }

        if (arg0 == null) {
            val playerSonecasBalance = transaction(Databases.sparklyPower) {
                val playerSonecasData = PlayerSonecas.selectAll().where {
                    PlayerSonecas.id eq accountInfo.uniqueId
                }.firstOrNull()

                return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
            }

            context.reply(
                PantufaReply(
                    "**LorittaLand Sonhos Exchange Service (LSX)**",
                    "\uD83D\uDCB5"
                ),
                PantufaReply(
                    "`-transferir Fonte Destino Quantidade`",
                    mentionUser = false
                ),
                PantufaReply(
                    "**Câmbio de Sonhos:**"
                ),
                PantufaReply(
                    "Um sonho da `loritta` equivalem a ${LSXCommand.loriToSparklyExchangeRate} sonecas no `survival`"
                ),
                PantufaReply(
                    "*Locais disponíveis para transferência...*",
                    mentionUser = false
                ),
                PantufaReply(
                    "**Loritta** `loritta` (*${profile.money} sonhos*)",
                    "<:sparklyPower:331179879582269451>",
                    mentionUser = false
                ),
                PantufaReply(
                    "**SparklyPower Survival** `survival` (*$playerSonecasBalance sonecas*)",
                    "<a:pantufa_pickaxe:997671670468853770>",
                    mentionUser = false
                )
            )
            return
        } else {
            if (arg1 != null) {
                LSXCommand.mutex.withLock {
                    /* val isEconomyDisabled = transaction(Databases.loritta) {
                        EconomyState.select {
                            EconomyState.id eq LSXCommand.DISABLED_ECONOMY_ID
                        }.count() == 1L
                    }

                    if (isEconomyDisabled) {
                        context.reply(
                            PantufaReply(
                                "A economia da Loritta está temporariamente desativada, tente novamente mais tarde.",
                                Constants.ERROR
                            )
                        )
                        return@withLock
                    } */

                    // Get the profile again within the mutex, to get the updated money value (if the user spammed the command)
                    val profile = getLorittaProfile(context.senderId.toLong())

                    val from = LSXCommand.TransferOptions.values().firstOrNull { it.codename == arg0 }
                    val to = LSXCommand.TransferOptions.values().firstOrNull { it.codename == arg1 }

                    if (from != null && to != null && arg2 != null) {
                        val quantity = NumberUtils.convertShortenedNumberToLong(arg2)

                        if (quantity == null) {
                            context.reply(
                                PantufaReply(
                                    "Quantidade inválida!",
                                    Constants.ERROR
                                )
                            )
                            return@withLock
                        }

                        if (from == to)
                            return@withLock

                        if (0 >= quantity)
                            return@withLock

                        if (from == LSXCommand.TransferOptions.LORITTA && to == LSXCommand.TransferOptions.PERFECTDREAMS_SURVIVAL) {
                            val sparklyPowerQuantity = quantity * LSXCommand.loriToSparklyExchangeRate

                            val fromBalance = LSXCommand.withdrawFromLoritta(
                                profile,
                                accountInfo.username,
                                accountInfo.uniqueId,
                                quantity,
                                sparklyPowerQuantity
                            )

                            if (!fromBalance) {
                                context.reply(
                                    PantufaReply(
                                        "Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
                                        Constants.ERROR
                                    )
                                )
                                return@withLock
                            }

                            LSXCommand.giveToSparklyPower(
                                accountInfo.uniqueId,
                                sparklyPowerQuantity
                            )

                            context.reply(
                                PantufaReply(
                                    "Você transferiu **${arg2} Sonhos** (Valor final: $sparklyPowerQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
                                    "\uD83D\uDCB8"
                                )
                            )

                            transaction(Databases.sparklyPower) {
                                net.perfectdreams.pantufa.dao.Transaction.new {
                                    this.type = TransactionType.LSX
                                    this.receiver = accountInfo.uniqueId
                                    this.currency = TransactionCurrency.MONEY
                                    this.time = System.currentTimeMillis()
                                    this.amount = quantity.toDouble()
                                    this.extra = context.senderId.toString()
                                }
                            }
                        } else if (from == LSXCommand.TransferOptions.PERFECTDREAMS_SURVIVAL && to == LSXCommand.TransferOptions.LORITTA) {
                            val lorittaQuantity = quantity / LSXCommand.loriToSparklyExchangeRate

                            val fromBalance = LSXCommand.withdrawFromSparklyPower(
                                accountInfo.uniqueId,
                                quantity
                            )

                            if (!fromBalance) {
                                context.reply(
                                    PantufaReply(
                                        "Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
                                        Constants.ERROR
                                    )
                                )
                                return@withLock
                            }

                            LSXCommand.giveToLoritta(
                                profile,
                                accountInfo.username,
                                accountInfo.uniqueId,
                                lorittaQuantity,
                                quantity
                            )

                            context.reply(
                                PantufaReply(
                                    "Você transferiu **${arg2} Sonecas** (Valor final: $lorittaQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
                                    "\uD83D\uDCB8"
                                )
                            )

                            transaction(Databases.sparklyPower) {
                                net.perfectdreams.pantufa.dao.Transaction.new {
                                    this.type = TransactionType.LSX
                                    this.payer = accountInfo.uniqueId
                                    this.currency = TransactionCurrency.MONEY
                                    this.time = System.currentTimeMillis()
                                    this.amount = quantity.toDouble()
                                    this.extra = context.senderId.toString()
                                }
                            }
                        }
                        return@withLock
                    }
                }
            }
        }
    }
}