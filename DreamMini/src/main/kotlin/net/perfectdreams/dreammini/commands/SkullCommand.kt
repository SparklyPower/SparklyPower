package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class SkullCommand(val m: DreamMini) : SparklyCommand(arrayOf("skull", "cabeça"), permission = "dreammini.skull"){

	@Subcommand
	fun root(sender: Player){
		sender.sendMessage(generateCommandInfo("skull <player>"))
	}

	@Subcommand
	fun skull(sender: Player, owner: String){
		val item = sender.inventory.itemInMainHand
		val type = item?.type

		if (type == Material.PLAYER_HEAD) {
			// É necessário "clonar" o item, se não for "clonado", não será possível usar "meta.owner" caso a skull já tenha
			// um owner anterior
			val skull = ItemStack(Material.PLAYER_HEAD, 1)
			skull.amount = item.amount
			skull.addEnchantments(item.enchantments)
			val meta = item.itemMeta as SkullMeta
			val skullMeta = skull.itemMeta as SkullMeta

			skullMeta.addItemFlags(*meta.itemFlags.toTypedArray())
			skullMeta.setDisplayName(meta.displayName)
			skullMeta.lore = meta.lore
			skullMeta.owner = owner

			skull.itemMeta = skullMeta
			sender.inventory.setItemInMainHand(skull)

			sender.sendMessage("§aAgora o dono da cabeça do player atual é §b${skullMeta.owner}§a!")
		} else {
			sender.sendMessage("§cSegure uma cabeça de um player na sua mão antes de usar!")
		}
	}
}