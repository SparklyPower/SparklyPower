package net.perfectdreams.dreamcorreios

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import net.perfectdreams.dreamcorreios.utils.CaixaPostal
import net.perfectdreams.dreamcorreios.utils.ContaCorreios
import net.perfectdreams.dreamcorreios.utils.Holders
import net.perfectdreams.libs.com.mongodb.client.MongoCollection
import net.perfectdreams.libs.com.mongodb.client.model.Filters
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayerExact
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class DreamCorreios : KotlinPlugin(), Listener {
	val cache = hashMapOf<String, ContaCorreios>() // Contas no correios carregadas, para evitar bugs de "salvar quando não deveria", nós salvamos as contas e deixamos ela aí por um tempinho
	lateinit var contas: MongoCollection<ContaCorreios>

	override fun softEnable() {
		super.softEnable()

		contas = DreamUtils.getMongoDatabase("perfectdreams_survival")
				.getCollection("correios", ContaCorreios::class.java)

		INSTANCE = this

		registerEvents(this)

		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			while (true) {
				waitFor(20 * (5 * 60))
				val diff = System.currentTimeMillis() - lastUpdate

				if (diff > 300000) {
					for ((_, conta) in cache) {
						contas.save(conta, Filters.eq("_id", conta.username))
					}
					cache.clear()

					lastUpdate = System.currentTimeMillis()
				}
			}
		}

		registerCommand(object: AbstractCommand("correios") {
			@Subcommand
			fun onCommand(p0: CommandSender): Boolean {
				for ((_, conta) in cache) {
					contas.save(conta, Filters.eq("_id", conta.username))
				}
				cache.clear()

				val conta = INSTANCE.contas.find(Filters.eq("_id", "loritta")).first()

				conta.caixasPostais.onEach {
					it.transformToInventory()
				}

				p0 as Player

				p0.openInventory(conta.caixasPostais[0].postalItems)
				return true
			}
		})
	}

	override fun softDisable() {
		super.softDisable()

		for ((_, conta) in cache) {
			contas.save(conta, Filters.eq("_id", conta.username))
		}
	}

	@EventHandler
	fun onLeftClick(e: NPCLeftClickEvent) {
		if (e.npc.name == "§a§lCaixa Postal") {
			handleCaixaPostal(e.clicker)
		}
	}

	@EventHandler
	fun onRightClick(e: NPCRightClickEvent) {
		if (e.npc.name == "§a§lCaixa Postal") {
			handleCaixaPostal(e.clicker)
		}
	}

	@EventHandler
	fun onClick(e: InventoryClickEvent) {
		val clickedInventory = e.clickedInventory ?: return
		val currentItem = e.currentItem ?: return

		if (currentItem.type == Material.AIR)
			return

		if (clickedInventory.holder !is Holders.CorreiosMenuHolder)
			return

		val data = currentItem.getStorageData(STORAGE_KEY) ?: return // Pegar o data do item, caso o data seja null, retorne
		val holder = clickedInventory.holder as Holders.CorreiosMenuHolder
		val caixaPostal = holder.correios.caixasPostais.getOrNull(data.toInt())
		val player = e.whoClicked as Player

		if (caixaPostal == null) { // Ok, pelo visto o cara ficou com o inventário aperto por tanto tempo que o plugin até tinha limpado as caixas postais dele
			player.sendMessage(PREFIX + "§cVocê demorou tanto tempo para escolher que essa caixa postal nem existe mais!")
			player.closeInventory()
			return
		} else {
			openCaixaPostal(player, holder.correios, caixaPostal)
		}
	}

	@EventHandler
	fun onClose(e: InventoryCloseEvent) {
		if (e.inventory.holder !is Holders.CorreiosHolder)
			return

		val holder = e.inventory.holder as Holders.CorreiosHolder

		scheduler().schedule(INSTANCE, SynchronizationContext.ASYNC) {
			holder.correios.caixasPostais.onEach {
				it.transformToBase64()
			}
		}
	}

	fun handleCaixaPostal(player: Player) {
		val username = player.name.toLowerCase()
		scheduler().schedule(INSTANCE, SynchronizationContext.ASYNC) {
			var conta = INSTANCE.cache[username]

			if (conta == null) {
				switchContext(SynchronizationContext.ASYNC)

				conta = INSTANCE.contas.find(Filters.eq("_id", username)).firstOrNull()

				if (conta == null) {
					conta = ContaCorreios(username)
				}
				switchContext(SynchronizationContext.SYNC)
			}

			INSTANCE.cache[username] = conta

			if (conta.caixasPostais.isEmpty()) {
				player.sendMessage(PREFIX + "§cVocê não possui nenhuma caixa postal... Você irá automaticamente ganhar uma caixa postal ao receber um item quando o seu inventário estiver cheio!")
				return@schedule
			}

			conta.caixasPostais.onEach {
				it.transformToInventory()
			}

			if (conta.caixasPostais.size == 1) { // Se só existe uma caixa postal, então abra ela mesmo
				// Hora de abrir a caixa postal! :)
				val caixaPostal = conta.caixasPostais.first()

				openCaixaPostal(player, conta, caixaPostal)
			} else { // Se existe > 1 caixa postal, nós precisamos mostrar um menu com todas elas
				openCaixaPostalMenu(player, conta)
			}
		}
	}

	fun openCaixaPostalMenu(player: Player, conta: ContaCorreios) {
		val inventory = Bukkit.createInventory(Holders.CorreiosMenuHolder(player, conta), 54, "§5§lCaixas Postais")

		for ((idx, caixaPostal) in conta.caixasPostais.withIndex()) {
			var item = ItemStack(Material.CHEST)
			item.rename("§5§lCaixa Postal ${idx + 1}")
			var count = 0
			caixaPostal.postalItems.forEach {
				if (it != null) {
					count += it.amount
				}
			}
			item.lore("§7Existem $count itens dentro desta caixa postal!")
			item = item.setStorageData("$idx", STORAGE_KEY)
			inventory.setItem(idx, item)
		}

		player.openInventory(inventory)
	}

	fun openCaixaPostal(player: Player, conta: ContaCorreios, caixaPostal: CaixaPostal) {
		caixaPostal.transformToInventory(Holders.CorreiosHolder(player, conta))

		player.openInventory(caixaPostal.postalItems)
	}

	companion object {
		lateinit var INSTANCE: DreamCorreios
		var lastUpdate = 0L;
		const val PREFIX = "§8[§9§lCorreios§8] §a"
		val STORAGE_KEY = UUID.fromString("c80b74e9-02b6-47ea-8656-9f26eb860c37")

		fun addItems(player: Player, giveToPlayer: Boolean = false, sendMessage: Boolean = true, vararg itemStacks: ItemStack) {
			addItems(player.name, giveToPlayer, sendMessage, *itemStacks)
		}

		fun addItems(playerName: String, giveToPlayer: Boolean = false, sendMessage: Boolean = true, vararg itemStacks: ItemStack) {
			scheduler().schedule(INSTANCE, SynchronizationContext.SYNC) {
				val username = playerName.toLowerCase()
				val player = getPlayerExact(playerName)

				val event = CorreiosItemReceivingEvent(player, arrayOf(*itemStacks))
				event.giveToPlayer = giveToPlayer

				Bukkit.getPluginManager().callEvent(event)

				if (event.isCancelled)
					return@schedule

				lastUpdate = System.currentTimeMillis()

				var conta = INSTANCE.cache[username]

				if (conta == null) {
					switchContext(SynchronizationContext.ASYNC)
					conta = INSTANCE.contas.find(Filters.eq("_id", username)).firstOrNull()

					if (conta == null) {
						conta = ContaCorreios(username)
					}
					switchContext(SynchronizationContext.SYNC)
				}

				INSTANCE.cache[username] = conta

				conta.addItems(player, event.giveToPlayer, sendMessage, *itemStacks)
			}
		}
	}
}