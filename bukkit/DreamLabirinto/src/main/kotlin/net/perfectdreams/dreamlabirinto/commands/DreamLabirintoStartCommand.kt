package net.perfectdreams.dreamlabirinto.commands

import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamlabirinto.DreamLabirinto
import net.perfectdreams.dreamlabirinto.utils.MazeGenerator
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign

object DreamLabirintoStartCommand : DSLCommandBase<DreamLabirinto> {
    override fun command(plugin: DreamLabirinto) = create(listOf("dreamlabirinto start")) {
        permission = "dreamlabirinto.setup"

        executes {
            plugin.event.preStart()
        }
    }
}