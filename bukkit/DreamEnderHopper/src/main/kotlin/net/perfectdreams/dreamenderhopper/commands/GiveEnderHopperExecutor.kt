package net.perfectdreams.dreamenderhopper.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamenderhopper.DreamEnderHopper
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class GiveEnderHopperExecutor(private val m: DreamEnderHopper) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        context.requirePlayer().inventory.addItem(m.createEnderHopper())
    }
}