package net.perfectdreams.pantufa.commands.server

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.SparklyPowerLSXSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.serializable.StoredSparklyPowerLSXSonhosTransaction
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.dao.Ban
import net.perfectdreams.pantufa.dao.Profile
import net.perfectdreams.pantufa.interactions.components.utils.TransactionCurrency
import net.perfectdreams.pantufa.interactions.components.utils.TransactionType
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.*
import net.perfectdreams.pantufa.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class LSXCommand : AbstractCommand("transferir", listOf("transfer", "lsx", "llsx", "lsxs", "llsxs"), requiresMinecraftAccount = true) {
	companion object {
		val mutex = Mutex()
		var loriToSparklyExchangeRate = 2L
		val DISABLED_ECONOMY_ID = UUID.fromString("3da6d95b-edb4-4ae9-aa56-4b13e91f3844")

		private fun getLorittaProfile(context: CommandContext): Profile {
			return transaction(Databases.loritta) {
				Profile.findById(context.discordAccount!!.discordId)
			} ?: throw RuntimeException()
		}

		fun withdrawFromLoritta(profile: Profile, playerName: String, playerUniqueId: UUID, quantity: Long, sparklyPowerQuantity: Long): Boolean {
			return transaction(Databases.loritta) {
				// Refresh the entity to get if they have money or not
				profile.refresh()

				if (quantity > profile.money)
					return@transaction false

				Profiles.update({ Profiles.id eq profile.userId }) {
					with (SqlExpressionBuilder) {
						it.update(Profiles.money, Profiles.money - quantity)
					}
				}

				val now = Instant.now()

				SimpleSonhosTransactionsLogUtils.insert(
					profile.userId,
					now,
					net.perfectdreams.loritta.common.utils.TransactionType.SPARKLYPOWER_LSX,
					quantity,
					StoredSparklyPowerLSXSonhosTransaction(
						SparklyPowerLSXTransactionEntryAction.EXCHANGED_TO_SPARKLYPOWER,
						sparklyPowerQuantity,
						playerName,
						playerUniqueId.toString(),
						loriToSparklyExchangeRate.toDouble()
					)
				)

				return@transaction true
			}
		}

		fun withdrawFromSparklyPower(playerUniqueId: UUID, quantity: Long): Boolean {
			return transaction(Databases.sparklyPower) {
				val playerSonecasData = PlayerSonecas.selectAll().where {
					PlayerSonecas.id eq playerUniqueId
				}.firstOrNull()

				if (playerSonecasData == null)
					return@transaction false

				if (quantity > playerSonecasData[PlayerSonecas.money].toDouble())
					return@transaction false

				PlayerSonecas.update({ PlayerSonecas.id eq playerUniqueId }) {
					with(SqlExpressionBuilder) {
						it.update(PlayerSonecas.money, PlayerSonecas.money - quantity.toBigDecimal())
					}
				}

				return@transaction true
			}
		}

		fun giveToLoritta(profile: Profile, playerName: String, playerUniqueId: UUID, quantity: Long, sparklyPowerQuantity: Long): Boolean? {
			transaction(Databases.loritta) {
				Profiles.update({ Profiles.id eq profile.userId }) {
					with (SqlExpressionBuilder) {
						it.update(Profiles.money, Profiles.money + quantity)
					}
				}

				val now = Instant.now()

				SimpleSonhosTransactionsLogUtils.insert(
					profile.userId,
					now,
					net.perfectdreams.loritta.common.utils.TransactionType.SPARKLYPOWER_LSX,
					quantity,
					StoredSparklyPowerLSXSonhosTransaction(
						SparklyPowerLSXTransactionEntryAction.EXCHANGED_TO_SPARKLYPOWER,
						sparklyPowerQuantity,
						playerName,
						playerUniqueId.toString(),
						loriToSparklyExchangeRate.toDouble()
					)
				)
			}
			return true
		}

		fun giveToSparklyPower(playerUniqueId: UUID, quantity: Long): Boolean {
			return transaction(Databases.sparklyPower) {
				val playerSonecasData = PlayerSonecas.selectAll().where {
					PlayerSonecas.id eq playerUniqueId
				}.firstOrNull()

				if (playerSonecasData == null)
					return@transaction false

				PlayerSonecas.update({ PlayerSonecas.id eq playerUniqueId }) {
					with(SqlExpressionBuilder) {
						it.update(PlayerSonecas.money, PlayerSonecas.money + quantity.toBigDecimal())
					}
				}

				return@transaction true
			}
		}
	}

	override fun run(context: CommandContext) {
		val arg0 = context.args.getOrNull(0)
		val arg1 = context.args.getOrNull(1)
		val arg2 = context.args.getOrNull(2)

		val profile = getLorittaProfile(context)
		val bannedState = profile.getBannedState()

		if (bannedState != null) {
			context.reply(
				PantufaReply(
					"Você está banido de utilizar a Loritta!"
				)
			)
			return
		}

		// Check if the user is banned
		val userBan = transaction(Databases.sparklyPower) {
			Ban.find {
				Bans.player eq context.minecraftAccountInfo!!.uniqueId and
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

		val survivalOnlineTrackedHours = pantufa.getPlayerTimeOnlineInTheLastXDays(context.minecraftAccountInfo!!.uniqueId, 30)

		if (Duration.ofHours(24) >= survivalOnlineTrackedHours.duration) {
			context.sendMessage(
				PantufaReply(
					"Você precisa ter mais de 24 horas online no SparklyPower Survival nos últimos 30 dias (desde ${survivalOnlineTrackedHours.since.toInstant().toKotlinInstant().toMessageFormat(DiscordTimestampStyle.ShortDate)}) antes de poder transferir sonhos! Atualmente você tem ${(survivalOnlineTrackedHours.duration.get(
						ChronoUnit.SECONDS
					).div(3_600))} horas.",
					"\uD83D\uDCB5"
				)
			)
			return
		}

		if (arg0 == null) {
			val playerSonecasBalance = transaction(Databases.sparklyPower) {
				val playerSonecasData = PlayerSonecas.selectAll().where {
					PlayerSonecas.id eq context.discordAccount!!.minecraftId
				}.firstOrNull()

				return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
			}

			context.sendMessage(
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
					"Um sonho da `loritta` equivalem a $loriToSparklyExchangeRate sonecas no `survival`"
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
					"**SparklyPower Survival** `survival` (*${playerSonecasBalance} sonecas*)",
					"<a:pantufa_pickaxe:997671670468853770>",
					mentionUser = false
				)
			)
			return
		} else {
			if (arg1 != null) {
				runBlocking {
					mutex.withLock {
						/* val isEconomyDisabled = transaction(Databases.loritta) {
							EconomyState.select {
								EconomyState.id eq DISABLED_ECONOMY_ID
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
						val profile = getLorittaProfile(context)

						val from = TransferOptions.values().firstOrNull { it.codename == arg0 }
						val to = TransferOptions.values().firstOrNull { it.codename == arg1 }

						if (from != null && to != null && arg2 != null) {
							val quantity = NumberUtils.convertShortenedNumberToLong(arg2)

							if (quantity == null) {
								context.sendMessage(
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

							if (from == TransferOptions.LORITTA && to == TransferOptions.PERFECTDREAMS_SURVIVAL) {
								val sparklyPowerQuantity = quantity * loriToSparklyExchangeRate

								val fromBalance = withdrawFromLoritta(
									profile,
									context.minecraftAccountInfo!!.username,
									context.minecraftAccountInfo!!.uniqueId,
									quantity,
									sparklyPowerQuantity
								)

								if (!fromBalance) {
									context.sendMessage(
										PantufaReply(
											"Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
											Constants.ERROR
										)
									)
									return@withLock
								}

								giveToSparklyPower(
									context.minecraftAccountInfo!!.uniqueId,
									sparklyPowerQuantity
								)

								context.sendMessage(
									PantufaReply(
										"Você transferiu **${arg2} Sonecas** (Valor final: $sparklyPowerQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
										"\uD83D\uDCB8"
									)
								)

								transaction(Databases.sparklyPower) {
									net.perfectdreams.pantufa.dao.Transaction.new {
										this.type = TransactionType.LSX
										this.receiver = context.minecraftAccountInfo.uniqueId
										this.currency = TransactionCurrency.MONEY
										this.time = System.currentTimeMillis()
										this.amount = quantity.toDouble()
										this.extra = context.user.id
									}
								}
							} else if (from == TransferOptions.PERFECTDREAMS_SURVIVAL && to == TransferOptions.LORITTA) {
								val lorittaQuantity = quantity / loriToSparklyExchangeRate

								val fromBalance = withdrawFromSparklyPower(
									context.minecraftAccountInfo!!.uniqueId,
									quantity
								)

								if (!fromBalance) {
									context.sendMessage(
										PantufaReply(
											"Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
											Constants.ERROR
										)
									)
									return@withLock
								}

								giveToLoritta(
									profile,
									context.minecraftAccountInfo!!.username,
									context.minecraftAccountInfo!!.uniqueId,
									lorittaQuantity,
									quantity
								)

								context.sendMessage(
									PantufaReply(
										"Você transferiu **${arg2} Sonhos** (Valor final: $lorittaQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
										"\uD83D\uDCB8"
									)
								)

								transaction(Databases.sparklyPower) {
									net.perfectdreams.pantufa.dao.Transaction.new {
										this.type = TransactionType.LSX
										this.payer = context.minecraftAccountInfo.uniqueId
										this.currency = TransactionCurrency.MONEY
										this.time = System.currentTimeMillis()
										this.amount = quantity.toDouble()
										this.extra = context.user.id
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

	enum class TransferOptions(val fancyName: String, val codename: String) {
		LORITTA("Loritta", "loritta"),
		PERFECTDREAMS_SURVIVAL("SparklyPower Survival", "survival")
	}
}