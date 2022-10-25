package net.perfectdreams.dreamenchant

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomCraftingRecipe
import net.perfectdreams.dreamenchant.listeners.BlockListener
import net.perfectdreams.dreamenchant.utils.PlayerEnchantmentTable
import net.perfectdreams.dreamenchant.utils.SpawnEnchantmentTable
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class DreamEnchant : KotlinPlugin() {
	val spawnEnchantmentTable = SpawnEnchantmentTable(this)
	val playerEnchantmentTable = PlayerEnchantmentTable(this)

	fun createSpecialEnchantmentTableItemStack(credits: Int) = ItemStack(Material.ENCHANTING_TABLE)
		.meta<ItemMeta> {
			addEnchant(Enchantment.ARROW_INFINITE, 1, true)
			displayName(
				Component.text("Super Mesa de Encantamento")
					.color(TextColor.color(147, 101, 204))
					.decoration(TextDecoration.ITALIC, false)
					.decoration(TextDecoration.BOLD, true)
			)
			lore(
				listOf(
					Component.text("Créditos: ")
						.color(NamedTextColor.GREEN)
						.decoration(TextDecoration.ITALIC, false)
						.append(
							Component.text(credits.toString())
								.color(NamedTextColor.YELLOW)
						)
				)
			)
			addItemFlags(
				ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_ATTRIBUTES
			)
			persistentDataContainer.set(PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.INTEGER, credits)
		}

	override fun softEnable() {
		super.softEnable()

		registerEvents(BlockListener(this))

		DreamCustomItems.registerCustomRecipe(
			CustomCraftingRecipe(
				this,
				itemRemapper = CustomCraftingRecipe.RUBY_REMAP,
				recipe = addRecipe(
					"super_enchanting_table",
					createSpecialEnchantmentTableItemStack(0),
					listOf(
						"DRD",
						"RER",
						"ANA"
					)
				) {
					it.setIngredient('D', Material.DIAMOND_BLOCK)
					it.setIngredient('R', Material.PRISMARINE_SHARD)
					it.setIngredient('E', Material.ENCHANTING_TABLE)
					it.setIngredient('A', Material.AMETHYST_BLOCK)
					it.setIngredient('N', Material.NETHERITE_INGOT)
				}
			)
		)

		val bedrockIntegrations = Bukkit.getPluginManager().getPlugin("DreamBedrockIntegrations") as DreamBedrockIntegrations
		bedrockIntegrations.registerInventoryTitleTransformer(
			this,
			{ PlainTextComponentSerializer.plainText().serialize(it).contains("\uE253") },
			{ Component.text("Super Mesa de Encantamento") }
		)

		bedrockIntegrations.registerInventoryTitleTransformer(
			this,
			{ PlainTextComponentSerializer.plainText().serialize(it).contains("\uE254") },
			{ Component.text("Você não tem créditos!") }
		)
	}

	fun getLevelMultiplierForPlayer(player: Player): Int {
		return when {
			player.hasPermission("dreamenchant.vip++") -> 4
			player.hasPermission("dreamenchant.vip+") -> 5
			player.hasPermission("dreamenchant.vip") -> 6
			else -> 7
		}
	}
}