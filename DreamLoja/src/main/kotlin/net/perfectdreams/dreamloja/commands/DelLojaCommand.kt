package net.perfectdreams.dreamloja.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DelLojaCommand(val m: DreamLoja) : SparklyCommand(arrayOf("delloja")) {
	@Subcommand
	fun root(player: Player) {
		deleteShop(player, "loja")
	}

	@Subcommand
	fun root(player: Player, shopName: String) {
		deleteShop(player, shopName)
	}

	fun deleteShop(player: Player, shopName: String) {
		// All shop names are in lowercase
		val shopName = shopName.toLowerCase()
		
		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction(Databases.databaseNetwork) {
				Shops.deleteWhere {
					(Shops.owner eq player.uniqueId) and (Shops.shopName eq shopName)
				}
			}

			switchContext(SynchronizationContext.SYNC)

			player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi deletada com sucesso!")
		}
	}
}