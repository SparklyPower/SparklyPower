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
		logger.info { "Getting all players that have ${primaryGroup.joinToString()} as their primary group..." }

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

		for (memberWithAdminRole in membersWithAdminRole) {
			val accountOfTheUser = discordAccountsOfTheUsers.firstOrNull { account -> account.discordId == memberWithAdminRole.idLong }

			if (accountOfTheUser == null || !accountOfTheUser.isConnected || !eligibleUniqueIds.contains(accountOfTheUser.minecraftId)) {
				try {
					logger.info { "Attempting to remove $adminRole from $adminRole..." }
					guild.removeRoleFromMember(memberWithAdminRole, adminRole).complete()
					logger.info { "Successfully removed $adminRole from $adminRole!" }
				} catch (e: Exception) {
					logger.warn(e) { "Something went wrong while trying to remove role $adminRole from $memberWithAdminRole! Ignoring..." }
				}
			}
		}

		val discordAccountsOfEligibleUniqueIds = transaction(Databases.sparklyPower) {
			DiscordAccount.find {
				DiscordAccounts.minecraftId inList eligibleUniqueIds
			}.toList()
		}

		for (eligibleUniqueId in eligibleUniqueIds) {
			val accountOfTheUser = discordAccountsOfEligibleUniqueIds.firstOrNull { account -> account.minecraftId == eligibleUniqueId }

			if (accountOfTheUser?.isConnected == true) {
				val member = guild.getMemberById(accountOfTheUser.discordId)

				if (member != null) {
					if (!member.roles.contains(adminRole)) {
						try {
							logger.info { "Attempting to give $adminRole to $member..." }
							guild.addRoleToMember(member, adminRole).complete()
							logger.info { "Successfully given $adminRole to $member!" }
						} catch (e: Exception) {
							logger.warn(e) { "Something went wrong while trying to add role $member to $adminRole! Ignoring..." }
						}
					} else {
						logger.info { "Not giving $adminRole to $member because they already have that role..." }
					}
				} else {
					logger.info { "Not giving $adminRole to $member because they aren't in the server..." }
				}
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

				for (userWithSparklyMemberRole in usersWithSparklyMemberRole) {
					if (!discordAccounts.any { account -> account.isConnected && account.discordId == userWithSparklyMemberRole.user.idLong }) {
						try {
							guild.removeRoleFromMember(userWithSparklyMemberRole, role).complete()
						} catch (e: Exception) {
							logger.warn(e) { "Something went wrong while trying to add role $role from $userWithSparklyMemberRole! Ignoring..." }
						}
					}
				}

				for (discordAccount in discordAccounts) {
					val member = guild.getMemberById(discordAccount.discordId) ?: continue

					if (!member.roles.contains(role)) {
						try {
							guild.addRoleToMember(member, role).complete()
						} catch (e: Exception) {
							logger.warn(e) { "Something went wrong while trying to add role $role to $member! Ignoring..." }
						}
					}
				}

				val owners = getPlayersWithGroup("dono")
				val admins = getPlayersWithGroup("admin")
				val moderators = getPlayersWithGroup("moderador")
				val coordenators = getPlayersWithGroup("coordenador")
				val supports = getPlayersWithGroup("suporte")
				val trialSupports = getPlayersWithGroup("trialsuporte")
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
				syncRolesEligibleForUsers(guild, sparklyPower.guild.trialSupportRoleId, trialSupports)
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
			logger.warn(e) { "Something went wrong while trying to synchronize roles!" }
		}
	}
}