package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import org.bukkit.entity.Player

interface WithClubeSubCommand {
    fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>)
}