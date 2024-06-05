package net.perfectdreams.dreamcorreios

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcorreios.commands.CorreiosGiveExecutor
import net.perfectdreams.dreamcorreios.commands.CorreiosOpenExecutor
import net.perfectdreams.dreamcorreios.commands.CorreiosTransformCaixaPostalExecutor
import net.perfectdreams.dreamcorreios.commands.declarations.DreamCorreiosCommand
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import net.perfectdreams.dreamcorreios.listeners.CaixaPostalListener
import net.perfectdreams.dreamcorreios.tables.ContaCorreios
import net.perfectdreams.dreamcorreios.utils.CaixaPostal
import net.perfectdreams.dreamcorreios.utils.CaixaPostalAccessHolder
import net.perfectdreams.dreamcorreios.utils.CaixaPostalHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DreamCorreios : KotlinPlugin(), Listener {
	companion object {
		val PREFIX = textComponent {
			append("[") {
				color(NamedTextColor.DARK_GRAY)
			}

			append("Correios") {
				color(TextColor.color(253, 220, 1))
				decorate(TextDecoration.BOLD)
			}

			append("]") {
				color(NamedTextColor.DARK_GRAY)
			}
		}

		val SKIPPED_SLOTS = setOf(
			8,
			17,
			53
		)

		const val MAX_ITEMS_PER_PAGE = 51
		val IS_CAIXA_POSTAL = SparklyNamespacedKey("is_caixa_postal")

		fun getInstance() = Bukkit.getPluginManager().getPlugin("DreamCorreios") as DreamCorreios
	}

	val loadedCaixaPostais = ConcurrentHashMap<UUID, CaixaPostal>()
	val loadingAndUnloadingCaixaPostalMutex = Mutex()

	override fun softEnable() {
		super.softEnable()
		dataFolder.mkdirs()

		val hasMigratedFile = File(dataFolder, "has_migrated_items_to_new_item_serialization_format")

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				ContaCorreios
			)

			// Convert Correios to new ItemStack data
			if (!hasMigratedFile.exists()) {
				logger.info("Updating Correios...")
				ContaCorreios.selectAll()
					.forEach {
						val oldItems = it[ContaCorreios.items]

						if (oldItems.isNotEmpty()) {
							val itemStacks = oldItems.split(";").mapNotNull { item ->
								item.fromBase64Item()
							}

							// Now we insert it using the PROPER way
							val newItems = itemStacks.map { ItemUtils.serializeItemToBase64(it) }.joinToString(";")

							// And now update!
							ContaCorreios.update({ ContaCorreios.id eq it[ContaCorreios.id] }) {
								it[ContaCorreios.items] = newItems
							}
						}
					}

				logger.info("Updated Correios!")
				hasMigratedFile.createNewFile()
			}
		}

		registerEvents(CaixaPostalListener(this))
		registerCommand(DreamCorreiosCommand(this))

		val bedrockIntegrations = Bukkit.getPluginManager().getPlugin("DreamBedrockIntegrations") as DreamBedrockIntegrations
		bedrockIntegrations.registerInventoryTitleTransformer(
			this,
			{ PlainTextComponentSerializer.plainText().serialize(it).contains("\uE262") },
			{ Component.text("Caixa Postal") }
		)

		launchAsyncThread {
			while (true) {
				logger.info { "Loaded Caixa Postais: ${loadedCaixaPostais.size} ${loadedCaixaPostais.keys}" }
				delay(60_000)
			}
		}
	}

	fun createCaixaPostalInventoryOfPlayer(player: Player, caixaPostalAccessHolder: CaixaPostalAccessHolder, pageTarget: Int): Inventory {
		DreamUtils.assertAsyncThread(true)

		val itemsPerPages = caixaPostalAccessHolder.items.chunked(MAX_ITEMS_PER_PAGE).map { it.toMutableList() }
			.toMutableList()
		if (itemsPerPages.size == 0) // Add empty list if there aren't any items
			itemsPerPages.add(mutableListOf())

		val pageIndex = pageTarget.coerceIn(itemsPerPages.indices)
		val inventory = Bukkit.createInventory(
			CaixaPostalHolder(caixaPostalAccessHolder, itemsPerPages, pageIndex),
			54,
			textComponent {
				color(NamedTextColor.WHITE)
				append("ꈉ\ue262陇")
				append("Caixa Postal [${pageIndex + 1}/${itemsPerPages.size}]") {
					color(NamedTextColor.BLACK)
				}
			}
		)

		val page = itemsPerPages[pageIndex]
		var i = 0
		if (pageIndex != 0) {
			inventory.setItem(
				8,
				ItemStack(Material.PAPER)
					.meta<ItemMeta> {
						displayName(textComponent {
							append("Voltar página") {
								color(NamedTextColor.AQUA)
								decoration(TextDecoration.ITALIC, false)
								decorate(TextDecoration.BOLD)
							}
						})
						setCustomModelData(47)
					}
			)
		}
		if (pageIndex != itemsPerPages.size - 1) {
			inventory.setItem(
				17,
				ItemStack(Material.PAPER)
					.meta<ItemMeta> {
						displayName(textComponent {
							append("Ir para a próxima página") {
								color(NamedTextColor.AQUA)
								decoration(TextDecoration.ITALIC, false)
								decorate(TextDecoration.BOLD)
							}
						})
						setCustomModelData(48)
					}
			)
		}

		inventory.setItem(
			53,
			ItemStack(Material.BARRIER)
				.meta<ItemMeta> {
					displayName(textComponent {
						append("Sair") {
							color(NamedTextColor.RED)
							decoration(TextDecoration.ITALIC, false)
							decorate(TextDecoration.BOLD)
						}
					})
				}
		)

		for (itemStack in page) {
			if (i in SKIPPED_SLOTS) {
				i++ // skip side bar
			}

			inventory.setItem(i, itemStack)
			i++
		}

		return inventory
	}

	suspend fun retrieveCaixaPostalOfPlayerAndHold(player: Player) = retrieveCaixaPostalOfPlayerAndHold(player.uniqueId)

	suspend fun retrieveCaixaPostalOfPlayerAndHold(playerId: UUID): CaixaPostalAccessHolder = loadingAndUnloadingCaixaPostalMutex
		.also { logger.info { "Trying to retrieve caixa postal for $playerId, is the mutex locked? ${it.isLocked}"} }
		.withLock {
			val caixaPostal = loadedCaixaPostais[playerId]
			logger.info { "Caixa Postal $playerId is $caixaPostal" }
			if (caixaPostal != null)
				return caixaPostal.createAccess()

			val itemsInBase64 = onAsyncThread {
				transaction(Databases.databaseNetwork) {
					ContaCorreios.select {
						ContaCorreios.id eq playerId
					}.firstOrNull()?.get(ContaCorreios.items)
				}
			}

			val items = if (itemsInBase64 == null || itemsInBase64.isEmpty())
				mutableListOf()
			else
				itemsInBase64.split(";")
					.map { ItemUtils.deserializeItemFromBase64(it) }
					.toMutableList()

			return CaixaPostal(this, playerId, items)
				.also {
					loadedCaixaPostais[playerId] = it
				}.createAccess()
		}

	/**
	 * Adds [items] to the [player]'s inventory if...
	 * * The player is online
	 * * The item can fit in their inventory
	 * * The [CorreiosItemReceivingEvent.playerIsAbleToReceiveItemOnTheirInventory] wasn't cancelled
	 * If any of the above fail, the item will be added to the player's mailbox
	 */
	fun addItem(player: Player, vararg items: ItemStack) = addItem(player.uniqueId, *items)

	/**
	 * Adds [items] to the [playerId]'s inventory if...
	 * * The player is online
	 * * The item can fit in their inventory
	 * * The [CorreiosItemReceivingEvent.playerIsAbleToReceiveItemOnTheirInventory] wasn't cancelled
	 * If any of the above fail, the item will be added to the player's mailbox
	 */
	fun addItem(
		playerId: UUID,
		vararg items: ItemStack
	) {
		DreamUtils.assertMainThread(true)

		val player = Bukkit.getPlayer(playerId)

		val itemsThatMustBeAddedToTheCaixaPostal = items.toMutableList()

		val event = CorreiosItemReceivingEvent(
			playerId,
			player,
			itemsThatMustBeAddedToTheCaixaPostal,
			CorreiosItemReceivingEvent.PlayerIsAbleToReceiveItemsOnTheirInventoryResult
		)

		Bukkit.getPluginManager().callEvent(event)

		if (player != null && event.result is CorreiosItemReceivingEvent.PlayerIsAbleToReceiveItemsOnTheirInventoryResult) {
			// Okay, player isn't null! Let's filter the items that they can hold
			for (item in itemsThatMustBeAddedToTheCaixaPostal.toImmutableList()) { // Clone it to avoid a ConcurrentModificationException
				// If the player can hold it...
				if (player.inventory.canHoldItem(item)) {
					// Then add it to the inventory!
					player.inventory.addItem(item)

					// And remove it from the list
					itemsThatMustBeAddedToTheCaixaPostal.remove(item)
				}
			}
		}

		// Everything went fine, so let's quit :3
		if (itemsThatMustBeAddedToTheCaixaPostal.isEmpty())
			return

		// Oh no... we need to add things to the caixa postal... We need to do this on an async thread!
		launchAsyncThread {
			val accessHolder = retrieveCaixaPostalOfPlayerAndHold(playerId)
			accessHolder.addItem(*itemsThatMustBeAddedToTheCaixaPostal.toTypedArray())
			accessHolder.release()

			onMainThread {
				player?.sendMessage(
					textComponent {
						append(PREFIX)
						append(" ")
						append(event.result.message)
					}
				)
			}
		}
	}
}