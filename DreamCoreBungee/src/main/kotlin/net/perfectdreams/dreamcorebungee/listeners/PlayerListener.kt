package net.perfectdreams.dreamcorebungee.listeners

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.perfectdreams.dreamcorebungee.DreamCoreBungee
import net.perfectdreams.dreamcorebungee.dao.User
import net.perfectdreams.dreamcorebungee.utils.Databases
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerListener(val m: DreamCoreBungee) : Listener {
	@EventHandler
	fun onLogin(e: LoginEvent) {
		e.registerIntent(m)
		ProxyServer.getInstance().scheduler.runAsync(m) {
			transaction(Databases.databaseNetwork) {
				val user = User.findById(e.connection.uniqueId) ?: User.new(e.connection.uniqueId) {
					this.username = e.connection.name
				}
				user.username = e.connection.name
			}
			e.completeIntent(m)
		}
	}
}