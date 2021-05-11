package net.perfectdreams.dreamloja.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.InventoryUtils
import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SchemaUtils.withDataBaseLock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SetLojaIconCommand(val m: DreamLoja) : SparklyCommand(arrayOf("setlojaicon")) {
	@Subcommand
	fun root(player: Player) {
		setIconOfShop(player, "loja")
	}

	@Subcommand
	fun root(player: Player, shopName: String) {
		setIconOfShop(player, shopName)
	}

	fun setIconOfShop(player: Player, shopName: String) {
		val itemInHand = player.inventory.itemInMainHand
		if (itemInHand.type.isAir) {
			player.sendMessage("§cSegure o item que você deseja que fique no ícone da sua §6/loja§c!")
			return
		}

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			val shop = transaction(Databases.databaseNetwork) {
				Shop.find {
					(Shops.owner eq player.uniqueId) and (Shops.shopName eq shopName)
				}.firstOrNull()
			}

			if (shop == null) {
				switchContext(SynchronizationContext.SYNC)
				player.sendMessage("§cLoja desconhecida!")
				return@schedule
			}

			transaction(Databases.databaseNetwork) {
				shop.iconItemStack = itemInHand.clone()
					.apply {
						if (!this.itemMeta.hasDisplayName())
							this.itemMeta = this.itemMeta.apply {
								this.setDisplayName("§a${shop.shopName}")
							}
					}
					.toBase64()
			}

			player.sendMessage("§aÍcone alterado com sucesso! Veja o novo look da sua loja em §6/loja")
		}
	}
}