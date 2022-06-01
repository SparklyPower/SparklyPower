package net.perfectdreams.dreamcore.utils

import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.permission.Permission
import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

object VaultUtils {
	@JvmStatic
	lateinit var econ: Economy
	@JvmStatic
	lateinit var perms: Permission
	@JvmStatic
	lateinit var chat: Chat

	fun setupEconomy(): Boolean {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false
		}
		val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
		econ = rsp.provider
		return econ != null
	}

	fun setupChat(): Boolean {
		val rsp = Bukkit.getServicesManager().getRegistration(Chat::class.java)
		chat = rsp!!.provider
		return chat != null
	}

	fun setupPermissions(): Boolean {
		val rsp = Bukkit.getServicesManager().getRegistration(Permission::class.java)
		perms = rsp!!.provider
		return perms != null
	}
}

/**
 * Pegar a quantidade de dinheiro que o usuário possui
 *
 * @return a quantidade de dinheiro do usuário
 */
val OfflinePlayer.balance: Double
	get() = VaultUtils.econ.getBalance(this)

/**
 * Retira uma quantidade de dinheiro do usuário
 */
fun OfflinePlayer.withdraw(quantity: Double, transactionContext: TransactionContext): EconomyResponse =
	VaultUtils.econ.withdrawPlayer(this, quantity).also {
		if (it.type == EconomyResponse.ResponseType.SUCCESS && quantity > 0)
			DreamCore.INSTANCE.launchAsyncThread {
				transactionContext.apply {
					amount = quantity
					payer = uniqueId
				}.saveToDatabase()
			}
	}

/**
 * Deposita uma quantidade de dinheiro no usuário
 */
fun OfflinePlayer.deposit(quantity: Double, transactionContext: TransactionContext): EconomyResponse =
	VaultUtils.econ.depositPlayer(this, quantity).also {
		if (it.type == EconomyResponse.ResponseType.SUCCESS && quantity > 0)
			DreamCore.INSTANCE.launchAsyncThread {
				transactionContext.apply {
					amount = quantity
					receiver = uniqueId
				}.saveToDatabase()
			}
	}

/**
 * Verifica se um usuário consegue pagar uma quantidade de dinheiro específica
 *
 * @return se o usuário consegue pagar
 */
fun OfflinePlayer.canPay(quantity: Double): Boolean {
	return VaultUtils.econ.has(this, quantity)
}