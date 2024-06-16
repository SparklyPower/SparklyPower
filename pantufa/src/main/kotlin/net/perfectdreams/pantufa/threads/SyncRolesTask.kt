package net.perfectdreams.pantufa.threads

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.LuckPermsPlayers
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SyncRolesTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		val m = PantufaBot.INSTANCE
		val sparklyPower = m.config.sparklyPower
	}

	private fun getPlayersWithGroup(vararg primaryGroup: String): List<UUID> {
		logger.info { "Getting all players that have $primaryGroup as their primary group..." }

		val primaryGroupPlayers = transaction(Databases.sparklyPowerLuckPerms) {
			LuckPermsPlayers.selectAll().where { LuckPermsPlayers.primaryGroup inList primaryGroup.toMutableList() }.map {
				UUID.fromString(it[LuckPermsPlayers.id])
			}.toMutableList()
		}

		val secondaryGroupPlayers = transaction(Databases.sparklyPowerLuckPerms) {
			LuckPermsUserPermissions.select {
				(LuckPermsUserPermissions.permission inList primaryGroup.map { "group.$it" }) and
						((LuckPermsUserPermissions.expiry greater (System.currentTimeMillis() / 1000).toInt())
								or (LuckPermsUserPermissions.expiry eq 0))
			}.map {
				UUID.fromString(it[LuckPermsUserPermissions.uuid])
			}.toMutableList()
		}

		val list = (primaryGroupPlayers + secondaryGroupPlayers).distinct()

		logger.info { "Players $list have $primaryGroup as their primary group!" }

		return list
	}

	private fun syncRolesEligibleForUsers(guild: Guild, roleId: Long, eligibleUniqueIds: List<UUID>) {
		logger.info { "Synchronizing role $roleId in $guild for $eligibleUniqueIds" }

		val adminRole = guild.getRoleById(roleId)!!
		val membersWithAdminRole = guild.getMembersWithRoles(adminRole)
		val discordAccountsOfTheUsers = transaction(Databases.sparklyPower) {
			DiscordAccount.find {
				DiscordAccounts.discordId inList membersWithAdminRole.map { it.user.idLong }
			}.toList()
		}

		membersWithAdminRole.forEach {
			val accountOfTheUser = discordAccountsOfTheUsers.firstOrNull { account -> account.discordId == it.user.idLong }

			if (accountOfTheUser == null || !accountOfTheUser.isConnected || !eligibleUniqueIds.contains(accountOfTheUser.minecraftId)) {
				guild.removeRoleFromMember(it, adminRole).queue()
			}
		}

		val discordAccountsOfEligibleUniqueIds = transaction(Databases.sparklyPower) {
			DiscordAccount.find {
				DiscordAccounts.minecraftId inList eligibleUniqueIds
			}.toList()
		}

		eligibleUniqueIds.forEach {
			val accountOfTheUser = discordAccountsOfEligibleUniqueIds.firstOrNull { account -> account.minecraftId == it }

			if (accountOfTheUser?.isConnected == true) {
				val member = guild.getMemberById(accountOfTheUser.discordId)

				if (member != null && !member.roles.contains(adminRole))
					guild.addRoleToMember(member, adminRole).queue()
			}
		}
	}

	override fun run() {
		try {
			val guild = m.mainLandGuild

			if (guild != null) {
				logger.info { "Synchronizing roles in $guild..." }

				val discordAccounts = transaction(Databases.sparklyPower) {
					DiscordAccount.find {
						DiscordAccounts.isConnected eq true
					}.toMutableList()
				}

				val role = guild.getRoleById(sparklyPower.guild.memberRoleId)!!

				val usersWithSparklyMemberRole = guild.getMembersWithRoles(role)

				usersWithSparklyMemberRole.forEach {
					if (!discordAccounts.any { account -> account.isConnected && account.discordId == it.user.idLong })
						guild.removeRoleFromMember(it, role).queue()
				}

				for (discordAccount in discordAccounts) {
					val member = guild.getMemberById(discordAccount.discordId) ?: continue

					if (!member.roles.contains(role))
						guild.addRoleToMember(member, role).queue()
				}

				val owners = getPlayersWithGroup("dono")
				val admins = getPlayersWithGroup("admin")
				val moderators = getPlayersWithGroup("moderador")
				val coordenators = getPlayersWithGroup("coordenador")
				val supports = getPlayersWithGroup("suporte")
				val builders = getPlayersWithGroup("construtor")
				val developers = getPlayersWithGroup("developer")
				val vips = getPlayersWithGroup("vip", "vip+", "vip++", "sonhador", "sonhador+", "sonhador++")
				val influencer = getPlayersWithGroup("influencer")
				val estrelinha = getPlayersWithGroup("estrelinha")

				syncRolesEligibleForUsers(guild, sparklyPower.guild.ownerRoleId, owners)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.adminRoleId, admins)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.coordRoleId, coordenators)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.modRoleId, moderators)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.supportRoleId, supports)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.builderRoleId, builders)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.devRoleId, developers)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.staffRoleId, owners.toMutableList() + admins.toMutableList() + coordenators.toMutableList() + moderators.toMutableList() + supports.toMutableList() + builders.toMutableList() + developers.toMutableList())
				syncRolesEligibleForUsers(guild, sparklyPower.guild.vipRoleId, vips)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.influencerRoleId, influencer)
				syncRolesEligibleForUsers(guild, sparklyPower.guild.starRoleId, estrelinha)

			} else {
				logger.warn { "Guild ${sparklyPower.guild.idLong} does not exist or isn't loaded yet! Skipping role synchronization..." }
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}