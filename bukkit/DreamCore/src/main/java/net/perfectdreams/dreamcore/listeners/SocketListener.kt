package net.perfectdreams.dreamcore.listeners

import com.github.salomonbrys.kotson.*
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.perfectdreams.dreamcore.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcore.network.socket.SocketUtils
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.DreamUtils.gson
import net.perfectdreams.dreamcore.utils.SocketCode
import net.perfectdreams.dreamcore.utils.VaultUtils
import net.perfectdreams.dreamcore.utils.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import java.util.*

class SocketListener : Listener {
	@EventHandler
	fun onSocketReceived(e: SocketReceivedEvent) {
		val type = e.json["type"].nullString ?: return

		// Exemplo de JSON...
		// { "type": "ping" }

		// Caso aconteça algum erro, { "error": { ... } }
		when (type) {
			"getOnlinePlayers" -> {
				val players = onlinePlayers().map { it.name }
				e.response["players"] = DreamUtils.gson.toJsonTree(players)
			}
			"getBalance" -> {
				val player = e.json["player"].string
				e.response["balance"] = VaultUtils.econ.getBalance(player)
			}
			"setBalance" -> {
				val player = e.json["player"].string
				val quantity = e.json["quantity"].double
				VaultUtils.econ.withdrawPlayer(player, VaultUtils.econ.getBalance(player))
				VaultUtils.econ.depositPlayer(player, quantity)
				e.response["balance"] = VaultUtils.econ.getBalance(player)
			}
			"transferBalance" -> {
				val from = e.json["from"].string
				val to = e.json["to"].string
				val quantity = e.json["quantity"].double
				if (!VaultUtils.econ.has(from, quantity)) {
					e.response = SocketUtils.createErrorPayload(
							SocketCode.INSUFFICIENT_FUNDS,
							"Player $from não possui fundos suficientes para a transação!"
					)
				} else {
					VaultUtils.econ.withdrawPlayer(from, quantity)
					VaultUtils.econ.depositPlayer(to, quantity)
					e.response = jsonObject(
							"fromBalance" to VaultUtils.econ.getBalance(from),
							"toBalance" to VaultUtils.econ.getBalance(to)
					)
				}
			}
			"giveBalance" -> {
				val player = e.json["player"].string
				val quantity = e.json["quantity"].double
				VaultUtils.econ.depositPlayer(player, quantity)
				e.response = jsonObject(
						"balance" to VaultUtils.econ.getBalance(player)
				)
			}
			"giveBalanceUuid" -> {
				val player = e.json["player"].string
				val quantity = e.json["quantity"].double
				VaultUtils.econ.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(player)), quantity)
				e.response = jsonObject(
						"balance" to VaultUtils.econ.getBalance(player)
				)
			}
			"withdrawBalance" -> {
				val player = e.json["player"].string
				val quantity = e.json["quantity"].double
				if (quantity > VaultUtils.econ.getBalance(player)) {
					e.response = SocketUtils.createErrorPayload(
							SocketCode.INSUFFICIENT_FUNDS,
							"Player $player não possui fundos suficientes para a transação!"
					)
					return
				}
				VaultUtils.econ.withdrawPlayer(player, quantity)
				e.response = jsonObject("balance" to VaultUtils.econ.getBalance(player))
			}
			"executeCommand" -> {
				val command = e.json["command"].string
				val useFakePlayer = e.json["pipeOutput"].nullBool ?: false
				val playerName = e.json["player"].nullString

				if (useFakePlayer) {
					val commandSender = FakeCommandSender("PantufaRelay")
					Bukkit.dispatchCommand(commandSender, command)
					e.response = jsonObject(
							"messages" to gson.toJsonTree(commandSender.receivedMessages)
					)
					return
				}

				if (playerName != null) {
					val player = Bukkit.getPlayerExact(playerName) ?: run {
						e.response = SocketUtils.createErrorPayload(
								SocketCode.UNKNOWN_PLAYER,
								"Player ${playerName} não existe ou está offline!"
						)
						return
					}

					Bukkit.dispatchCommand(player, command)
				} else {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
				}

				e.response = jsonObject()
			}
			"getTps" -> {
				e.response = jsonObject("tps" to gson.toJsonTree(Bukkit.getServer().tps))
			}
			"info" -> {
				val players = onlinePlayers().map { it.name }

				e.response = jsonObject(
						"name" to Bukkit.getName(),
						"version" to Bukkit.getVersion(),
						"apiVersion" to Bukkit.getBukkitVersion(),
						"tps" to jsonObject("tps" to gson.toJsonTree(Bukkit.getServer().tps)),
						"nmsVersion" to DreamUtils.nmsVersion,
						"players" to DreamUtils.gson.toJsonTree(players),
						"plugins" to gson.toJsonTree(Bukkit.getPluginManager().plugins.map {
							jsonObject(
									"name" to it.name,
									"version" to it.description.version
							)
						}
						)
				)
			}
		}
	}

	class FakeCommandSender(val fakeName: String) : CommandSender {
		override fun isPermissionSet(p0: Permission): Boolean {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun hasPermission(p0: Permission): Boolean {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun addAttachment(p0: Plugin, p1: String, p2: Boolean): PermissionAttachment {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun addAttachment(p0: Plugin): PermissionAttachment {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun addAttachment(p0: Plugin, p1: String, p2: Boolean, p3: Int): PermissionAttachment? {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun addAttachment(p0: Plugin, p1: Int): PermissionAttachment? {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun removeAttachment(p0: PermissionAttachment) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		val receivedMessages = mutableListOf<String>()
		val spigot = Spigot(this)

		override fun isOp() = true

		override fun setOp(p0: Boolean) {}

		override fun sendMessage(p0: String) {
			receivedMessages.add(p0)
		}

		override fun sendMessage(p0: Array<out String>) {
			receivedMessages.addAll(p0)
		}

		override fun sendMessage(p0: UUID?, p1: String) {
			TODO("Not yet implemented")
		}

		override fun sendMessage(p0: UUID?, p1: Array<out String>) {
			TODO("Not yet implemented")
		}

		override fun sendMessage(component: BaseComponent) {
			receivedMessages.add(component.toLegacyText())
		}

		override fun sendMessage(vararg components: BaseComponent) {
			receivedMessages.addAll(components.map { it.toLegacyText() })
		}

		override fun spigot() = spigot

		override fun name(): Component {
			TODO("Not yet implemented")
		}

		override fun getName() = fakeName

		override fun getServer() = Bukkit.getServer()

		override fun isPermissionSet(p0: String) = true

		override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun recalculatePermissions() {}

		override fun hasPermission(p0: String): Boolean {
			return true
		}

		class Spigot(val sender: FakeCommandSender) : CommandSender.Spigot() {
			override fun sendMessage(component: BaseComponent) {
				sender.receivedMessages.add(component.toLegacyText())
			}

			override fun sendMessage(vararg components: BaseComponent) {
				sender.receivedMessages.addAll(components.map { it.toLegacyText() })
			}
		}
	}
}