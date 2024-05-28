package net.perfectdreams.dreamsonecas

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamsonecas.tables.PlayerSonecas
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.util.*

// VaultAPI implementation of DreamSonecas' economy
class SonecasEconomy(private val m: DreamSonecas) : Economy {
    private val brazilLocale = Locale("pt", "BR")
    private val numberFormat = NumberFormat.getNumberInstance(brazilLocale)
        .apply {
            this.minimumFractionDigits = 2
            this.maximumFractionDigits = 2
        }

    override fun isEnabled() = true

    override fun getName() = "DreamSonecas"

    override fun hasBankSupport() = false

    override fun fractionalDigits(): Int {
        TODO("Not yet implemented")
    }

    override fun format(amount: Double) = SonecasUtils.formatSonecasAmountWithCurrencyName(amount)

    override fun currencyNamePlural() = "Sonecas"

    override fun currencyNameSingular() = "Soneca"

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return transaction(Databases.databaseNetwork) {
            PlayerSonecas.select(PlayerSonecas.money)
                .where { PlayerSonecas.id eq player.uniqueId }
                .count() == 1L
        }
    }

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String, world: String) = hasAccount(playerName)

    override fun hasAccount(player: OfflinePlayer, world: String) = hasAccount(player)

    @Deprecated("Deprecated in Java")
    override fun getBalance(p0: String): Double {
        TODO("Not yet implemented")
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return transaction(Databases.databaseNetwork) {
            PlayerSonecas.select(PlayerSonecas.money)
                .where { PlayerSonecas.id eq player.uniqueId }
                .firstOrNull()
                ?.get(PlayerSonecas.money)
                ?.toDouble()
        } ?: 0.0
    }

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String, world: String): Double {
        TODO("Not yet implemented")
    }

    override fun getBalance(player: OfflinePlayer, world: String): Double {
        return transaction(Databases.databaseNetwork) {
            PlayerSonecas.select(PlayerSonecas.money)
                .where { PlayerSonecas.id eq player.uniqueId }
                .firstOrNull()
                ?.get(PlayerSonecas.money)
                ?.toDouble()
        } ?: 0.0
    }

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, amount: Double): Boolean {
        TODO("Not yet implemented")
    }

    override fun has(player: OfflinePlayer, amount: Double) = getBalance(player) >= amount

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, world: String, amount: Double) = has(playerName, amount)

    override fun has(player: OfflinePlayer, world: String, amount: Double) = has(player, amount)

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        assert(amount >= 0.0) { "Withdrawn amount cannot be negative!" }

        transaction(Databases.databaseNetwork) {
            val updatedRows = PlayerSonecas.update({ PlayerSonecas.id eq player.uniqueId }) {
                with(SqlExpressionBuilder) {
                    it[PlayerSonecas.money] = PlayerSonecas.money - amount.toBigDecimal()
                }
                it[PlayerSonecas.updatedAt] = Instant.now()
            }

            // Actually we don't have any account! So let's create one
            if (updatedRows == 0) {
                PlayerSonecas.insert {
                    it[PlayerSonecas.id] = player.uniqueId
                    it[PlayerSonecas.money] = amount.toBigDecimal()
                    it[PlayerSonecas.updatedAt] = Instant.now()
                }
            }
        }

        return EconomyResponse(
            amount,
            // This is an optimization: We don't want to get the player's balance, so we just return 0.0
            // TODO: Update Exposed and use updateReturning!
            0.0,
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, world: String, amount: Double) = withdrawPlayer(playerName, amount)

    override fun withdrawPlayer(player: OfflinePlayer, world: String, amount: Double) = withdrawPlayer(player, amount)

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(player: String, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        transaction(Databases.databaseNetwork) {
            val updatedRows = PlayerSonecas.update({ PlayerSonecas.id eq player.uniqueId }) {
                with(SqlExpressionBuilder) {
                    it[PlayerSonecas.money] = PlayerSonecas.money + amount.toBigDecimal()
                }
                it[PlayerSonecas.updatedAt] = Instant.now()
            }

            // Actually we don't have any account! So let's create one
            if (updatedRows == 0) {
                PlayerSonecas.insert {
                    it[PlayerSonecas.id] = player.uniqueId
                    it[PlayerSonecas.money] = amount.toBigDecimal()
                    it[PlayerSonecas.updatedAt] = Instant.now()
                }
            }
        }

        return EconomyResponse(
            amount,
            // This is an optimization: We don't want to get the player's balance, so we just return 0.0
            // TODO: Update Exposed and use updateReturning!
            0.0,
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(player: String, world: String, amount: Double) = depositPlayer(player, amount)

    override fun depositPlayer(player: OfflinePlayer, world: String, amount: Double) = depositPlayer(player, amount)

    @Deprecated("Deprecated in Java")
    override fun createBank(p0: String?, p1: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun createBank(p0: String?, p1: OfflinePlayer?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun deleteBank(p0: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankBalance(p0: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankHas(p0: String?, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankWithdraw(p0: String?, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankDeposit(p0: String?, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun isBankOwner(p0: String?, p1: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun isBankOwner(p0: String?, p1: OfflinePlayer?): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun isBankMember(p0: String?, p1: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun isBankMember(p0: String?, p1: OfflinePlayer?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun getBanks(): MutableList<String> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPlayerAccount(player: OfflinePlayer): Boolean {
        return transaction(Databases.databaseNetwork) {
            if (PlayerSonecas.selectAll().where { PlayerSonecas.id eq player.uniqueId }.count() != 0L) {
                m.logger.warning("Account for player ${player.name} (${player.uniqueId}) already exists!")
                return@transaction false
            }

            m.logger.info("Creating account for ${player.name} (${player.uniqueId})")
            PlayerSonecas.insert {
                it[PlayerSonecas.id] = player.uniqueId
                it[PlayerSonecas.money] = BigDecimal.ZERO
                it[PlayerSonecas.updatedAt] = Instant.now()
            }
            return@transaction true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String, world: String) = createPlayerAccount(playerName)

    override fun createPlayerAccount(player: OfflinePlayer, world: String) = createPlayerAccount(player)
}