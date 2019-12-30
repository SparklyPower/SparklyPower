package net.perfectdreams.dreammini.commands

import me.lucko.luckperms.LuckPerms
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class StaffCommand : SparklyCommand(arrayOf("staff", "equipe")) {

    @Subcommand
    fun root(sender: CommandSender) {
        val api = LuckPerms.getApi()

        val allStaffMembers = api.users.filter { it.hasPermission(api.buildNode("sparklypower.soustaff").build()).asBoolean() }

        val helpers = api.users.filter { it.inheritsGroup(api.getGroup("suporte")!!) }
        val moderators = api.users.filter { it.inheritsGroup(api.getGroup("mod")!!) }
        val admins = api.users.filter { it.inheritsGroup(api.getGroup("admin")!!) }
        val owners = api.users.filter { it.inheritsGroup(api.getGroup("dono")!!) }

        val onlineStaffMembers = allStaffMembers.filter { Bukkit.getPlayer(it.uuid) != null }

        sender.sendMessage("""
            Membros da equipe (${allStaffMembers.size}):
            §6§lSuportes:
            ${helpers.joinToString(", ", transform = { it.name!! })}

            §9§lModeradores:
            ${moderators.joinToString(", ", transform = { it.name!! })}

            §c§lAdministradores:
            ${admins.joinToString(", ", transform = { it.name!! })}

            §4§lDono:
            ${owners.joinToString(", ", transform = { it.name!! })}

            Membros da equipe §aonline§r (${onlineStaffMembers.size}):
            ${onlineStaffMembers.map { Bukkit.getPlayer(it.uuid) }.joinToString(", ", transform = { "${it.displayName}§r" })}
        """.trimIndent())
    }
}