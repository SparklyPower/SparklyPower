package net.perfectdreams.dreamloja.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SetLojaCommand(val m: DreamLoja) : SparklyCommand(arrayOf("setloja")) {
	@Subcommand
	fun root(player: Player) {
		createShop(player, "loja")
	}

	@Subcommand
	fun root(player: Player, shopName: String) {
		createShop(player, shopName)
	}

	fun createShop(player: Player, shopName: String) {
		// All shop names are in lowercase
		val shopName = shopName.toLowerCase()

		val location = player.location
		if (location.isUnsafe) {
			player.sendMessage("${DreamLoja.PREFIX} §cA sua localização atual é insegura! Vá para um lugar mais seguro antes de marcar a sua loja!")
			return
		}

		var createdNew = false
		var valid = true

		val shopCountForPlayer = getMaxAllowedShops(player)

		transaction(Databases.databaseNetwork) {
			val shop = Shop.find {
				(Shops.owner eq player.uniqueId) and (Shops.shopName eq shopName)
			}.firstOrNull()

			val isNew = shop == null

			val shopCount = transaction(Databases.databaseNetwork) {
				Shop.find { (Shops.owner eq player.uniqueId) }.count()
			}

			if (isNew) {
				if (shopCount + 1 > shopCountForPlayer) {
					player.sendMessage("${DreamLoja.PREFIX} §cVocê já tem muitas lojas! Delete algumas usando §6/delloja ${player.name}§a!")
					valid = false
					return@transaction
				}
				createdNew = true
				Shop.new {
					this.owner = player.uniqueId
					this.shopName = shopName
					setLocation(location)
				}
			} else {
				shop!!.setLocation(location)
			}
		}

		if (!valid)
			return

		if (shopName == "loja") {
			if (createdNew) {
				player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi criada com sucesso! Outros jogadores podem ir até ela utilizando §6/loja ${player.name}§a!")
			} else {
				player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi atualizada com sucesso! Outros jogadores podem ir até ela utilizando §6/loja ${player.name}§a!")
			}
		} else {
			if (createdNew) {
				player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi criada com sucesso! Outros jogadores podem ir até ela utilizando §6/loja ${player.name} $shopName§a!")
			} else {
				player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi atualizada com sucesso! Outros jogadores podem ir até ela utilizando §6/loja ${player.name} $shopName§a!")
			}
		}

		if (shopCountForPlayer != 1) {
			player.sendMessage("${DreamLoja.PREFIX} §eSabia que é possível alterar o ícone da sua loja na §6/loja§e? Use §6/setlojaicon $shopName§e com o item na mão!")
		}
	}

	/**
	 * Gets the max allowed homes for the [player]
	 */
	fun getMaxAllowedShops(player: Player): Int {
		return when {
			player.hasPermission("dreamloja.lojaplusplusplus") -> 7
			player.hasPermission("dreamloja.lojaplusplus") -> 5
			player.hasPermission("dreamloja.lojaplus") -> 3
			else -> 1
		}
	}
}