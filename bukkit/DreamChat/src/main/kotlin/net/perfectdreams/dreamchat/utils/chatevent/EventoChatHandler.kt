package net.perfectdreams.dreamchat.utils.chatevent

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.cash.NightmaresCashRegister
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.getTranslatedDisplayName
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamcore.utils.preferences.broadcastMessage
import net.perfectdreams.dreamcorreios.utils.addItemIfPossibleOrAddToPlayerMailbox
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class EventoChatHandler : ServerEvent("Chat", "") {
	val prizes = mutableListOf<ItemStack>()
	var currentPrize: ItemStack = ItemStack(Material.AIR)
	lateinit var event: IEventoChat
	var start = 0L
	var lastWinner: UUID? = null
	val randomMessagesEvent = EventoChatMensagem()
	val unshuffleWordEvent = EventoChatDesembaralhar()
	val calculateEvent = EventoChatCalcular()
	val events = listOf(
		randomMessagesEvent,
		unshuffleWordEvent,
		calculateEvent
	)
	var willGiveOutPesadelos = false

	init {
		this.delayBetween = 900_000 // 15 minutes
		this.requiredPlayers = 7

		prizes.apply {
			add(ItemStack(Material.DIAMOND, 2))
			add(ItemStack(Material.EMERALD))
			add(ItemStack(Material.IRON_INGOT, 8))
			add(ItemStack(Material.GOLD_INGOT, 4))
			add(ItemStack(Material.COAL, 48))
			add(ItemStack(Material.CAKE, 4))
			add(ItemStack(Material.SPONGE, 4))
			add(ItemStack(Material.MELON, 16))
			add(ItemStack(Material.PUMPKIN, 16))
			add(ItemStack(Material.APPLE, 32))
			add(ItemStack(Material.PUMPKIN_PIE, 32))
			add(ItemStack(Material.COOKIE, 32))
			add(ItemStack(Material.BREAD, 32))
		}
	}

	override fun preStart() {
		running = true
		start()
	}

	override fun start() {
		super.start()
		currentPrize = prizes.random()

		event = events.random()
		event.preStart()

		start = System.currentTimeMillis()

		willGiveOutPesadelos = DreamUtils.random.nextInt(0, 3) == 0 // 25% chance

		broadcastMessage(BroadcastType.CHAT_EVENT) { DreamUtils.HEADER_LINE }
		broadcastMessage(BroadcastType.CHAT_EVENT) { ("§6Quem " + event.getToDoWhat() + " primeiro").centralize() }
		broadcastMessage(BroadcastType.CHAT_EVENT) { ("§e" + event.getAnnouncementMessage()).centralize() }

		var message = "§6Irá ganhar §9" + currentPrize.amount + " " + ChatColor.stripColor(currentPrize.getTranslatedDisplayName("pt_BR"))
		if (willGiveOutPesadelos) message += "§6 e §cum pesadelo"
		message += "§6!"

		broadcastMessage(BroadcastType.CHAT_EVENT) { message.centralize() }
		broadcastMessage(BroadcastType.CHAT_EVENT) { DreamUtils.HEADER_LINE }

		scheduler().schedule(DreamChat.INSTANCE) {
			waitFor(3600)

			if (!running) return@schedule

			running = false

			broadcastMessage(BroadcastType.CHAT_EVENT) {
				"§cSério mesmo que NINGUÉM participou do evento chat? Que triste... estava tão animado para alguém ganhar e todos apenas desistiram..."
			}
		}
	}

	fun finish(player: Player) {
		running = false
		lastTime = System.currentTimeMillis()
		val diff = lastTime - start

		Bukkit.broadcastMessage(DreamUtils.HEADER_LINE)
		event.sendWinnerMessages(player, diff)
		Bukkit.broadcastMessage(DreamUtils.HEADER_LINE)

		lastWinner = player.uniqueId
		DreamChat.INSTANCE.userData.set("last-chat-winner", player.uniqueId.toString())

		DreamChat.INSTANCE.launchMainThread {
			player.addItemIfPossibleOrAddToPlayerMailbox(currentPrize)
		}

		scheduler().schedule(DreamChat.INSTANCE, SynchronizationContext.ASYNC) {
			DreamCore.INSTANCE.dreamEventManager.addEventVictory(player, "Chat")
			if (willGiveOutPesadelos)
				NightmaresCashRegister.INSTANCE.giveCash(player, 1L, TransactionContext(type = TransactionType.EVENTS, extra = "Chat"))
			DreamChat.INSTANCE.userData.save(DreamChat.INSTANCE.dataYaml)
			event.postEndAsync(player, diff)
		}
	}
}
