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
		val location = player.location
		if (location.isUnsafe) {
			player.sendMessage("${DreamLoja.PREFIX} §cA sua localização atual é insegura! Vá para um lugar mais seguro antes de marcar a sua loja!")
			return
		}

		var createdNew = false

		transaction(Databases.databaseNetwork) {
			val shop = Shop.find {
				(Shops.owner eq player.uniqueId) and (Shops.shopName eq "loja")
			}.firstOrNull()

			if (shop == null) {
				createdNew = true
				Shop.new {
					this.owner = player.uniqueId
					this.shopName = "loja"
					setLocation(location)
				}
			} else {
				shop.setLocation(location)
			}
		}

		if (createdNew) {
			player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi criada com sucesso! Outros jogadores podem ir até ela utilizando §6/loja ${player.name}§a!")
		} else {
			player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi atualizada com sucesso! Outros jogadores podem ir até ela utilizando §6/loja ${player.name}§a!")
		}
	}
}