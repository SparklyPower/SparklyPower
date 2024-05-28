package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import net.perfectdreams.pantufa.utils.DateUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.select

object VIPInfoCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "VIPInfoCommand", listOf("vip", "vipinfo")) {
		executes {
			val accountInfo = retrieveConnectedMinecraftAccountOrFail()
			val playerUniqueId = accountInfo.uniqueId

			val userPerms = pantufa.transactionOnLuckPermsDatabase {
				LuckPermsUserPermissions.select {
					LuckPermsUserPermissions.uuid eq playerUniqueId.toString()
				}.toList()
			}

			// Get the user perms matching the VIP groups
			val vipPlusPlusPermission = userPerms.firstOrNull { it[LuckPermsUserPermissions.permission] == "group.vip++" }
			val vipPlusPermission = userPerms.firstOrNull { it[LuckPermsUserPermissions.permission] == "group.vip+" }
			val vipPermission = userPerms.firstOrNull { it[LuckPermsUserPermissions.permission] == "group.vip" }

			// Make a very pretty name from it, and get the expires too
			val data = when {
				vipPlusPlusPermission != null -> Pair("VIP++", vipPlusPlusPermission[LuckPermsUserPermissions.expiry])
				vipPlusPermission != null -> Pair("VIP+", vipPlusPermission[LuckPermsUserPermissions.expiry])
				vipPermission != null -> Pair("VIP", vipPermission[LuckPermsUserPermissions.expiry])
				else -> null
			}

			// Check if the data is null and also check if the expiration date is still active
			if (data != null && System.currentTimeMillis() >= (data.second * 1000)) {
				// And then send the message!
				reply(
					PantufaReply(
						content = "Seu **${data.first}** irá expirar em *${
							DateUtils.formatDateDiff(data.second.toLong() * 1000)
						}*",
						prefix = "<a:cooldoge:543220304223404042>"
					)
				)
			} else {
				reply(
					PantufaReply(
						content = "Você não tem nenhum **VIP** no momento... Então que tal comprar pesadelos em nossa loja para você poder comprar um VIP lindíssimo? https://sparklypower.net/loja",
						prefix = "<:lori_sob:556524143281963008>"
					)
				)
			}
		}
	}
}