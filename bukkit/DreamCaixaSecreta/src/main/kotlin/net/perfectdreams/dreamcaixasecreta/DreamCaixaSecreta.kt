package net.perfectdreams.dreamcaixasecreta

import club.minnced.discord.webhook.WebhookClient
import com.xxmicloxx.NoteBlockAPI.model.Song
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcaixasecreta.listeners.BlockListener
import net.perfectdreams.dreamcaixasecreta.listeners.CraftListener
import net.perfectdreams.dreamcaixasecreta.utils.RandomItem
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.meta.ItemMeta
import java.io.File

class DreamCaixaSecreta : KotlinPlugin() {
	lateinit var itemReceived: Song
	var prizes = mutableListOf<RandomItem>()

	val nitroNotifyWebhook = WebhookClient.withUrl(config.getString("nitro-notify")!!)
	val COMBINE_BOXES_KEY = SparklyNamespacedKey("combine_secret_boxes")

	override fun softEnable() {
		super.softEnable()

		addRecipe(
			COMBINE_BOXES_KEY,
			ShapelessRecipe(
				COMBINE_BOXES_KEY, Material.CHEST.toItemStack()
			).addIngredient(2, Material.CHEST)
		)

		itemReceived = NBSDecoder.parse(File(dataFolder, "item-received.nbs"))
		registerEvents(BlockListener(this))
		registerEvents(CraftListener(this))

		registerCommand(object: SparklyCommand(arrayOf("caixasecretagen"), permission = "sparkly.anybox") {
			@Subcommand
			fun test(player: Player, level: String) {
				player.sendMessage("§eGerando caixa secreta...")
				player.inventory.addItem(generateCaixaSecreta(level.toIntOrNull()))
				player.sendMessage("§aCaixa gerada e adicionada!")
			}
		})

		var chance = 0.1
		prizes.add(
			RandomItem(
				ItemStack(
					Material.BEACON
				), chance
			)
		)
		chance = 0.2
		prizes.add(
			RandomItem(
				ItemStack(
					Material.NETHER_STAR
				), chance
			)
		)
		chance = 0.5
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_HELMET
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_CHESTPLATE
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_LEGGINGS
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_BOOTS
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_SWORD
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_PICKAXE
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND_SHOVEL
				), chance, true
			)
		)
		chance = 1.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_HELMET
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_CHESTPLATE
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_LEGGINGS
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_BOOTS
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_SWORD
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_PICKAXE
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_SHOVEL
				), chance, true
			)
		)
		chance = 2.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_HELMET
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_CHESTPLATE
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_LEGGINGS
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_BOOTS
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_SWORD
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_PICKAXE
				), chance, true
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_SHOVEL
				), chance, true
			)
		)
		chance = 1.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.ANVIL
				), chance
			)
		)
		chance = 1.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLDEN_APPLE,
					1
				), chance
			)
		)
		for (material in Material.values().filter { !it.isLegacy }) {
			if (material.name.startsWith("MUSIC_DISC_")) {
				prizes.add(
					RandomItem(
						ItemStack(
							material,
							1
						), chance
					)
				)
			}

			if (material.name.endsWith("STAINED_GLASS")) {
				prizes.add(
					RandomItem(
						ItemStack(
							material,
							4
						), chance
					)
				)
			}

			if (material.name.endsWith("STAINED_GLASS_PANE")) {
				prizes.add(
					RandomItem(
						ItemStack(
							material,
							8
						), chance
					)
				)
			}

			if (material.name.endsWith("TERRACOTTA")) {
				prizes.add(
					RandomItem(
						ItemStack(
							material,
							8
						), chance
					)
				)
			}
		}

		chance = 2.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.SLIME_BLOCK
				), chance
			)
		)
		chance = 2.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.CHAINMAIL_CHESTPLATE
				).rename("§6§lJetpack")
					.storeMetadata("isJetpack", "true")
				, chance
			)
		)
		chance = 3.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.HOPPER
				), chance
			)
		)
		chance = 3.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.SPONGE,
					2
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.EMERALD
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.QUARTZ_BLOCK,
					16
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.CHISELED_QUARTZ_BLOCK,
					16
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.QUARTZ_PILLAR,
					16
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.JUKEBOX
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.NOTE_BLOCK
				), chance
			)
		)
		chance = 4.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.EXPERIENCE_BOTTLE,
					16
				), chance
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.OAK_LOG,
					64
				), chance
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.ACACIA_LOG,
					64
				), chance
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.BIRCH_LOG,
					64
				), chance
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DARK_OAK_LOG,
					64
				), chance
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.SPRUCE_LOG,
					64
				), chance
			)
		)
		prizes.add(
			RandomItem(
				ItemStack(
					Material.JUNGLE_LOG,
					64
				), chance
			)
		)

		chance = 5.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.END_STONE,
					16
				), chance
			)
		)
		chance = 5.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.DIAMOND,
					1
				), chance
			)
		)
		chance = 6.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GOLD_INGOT,
					16
				), chance
			)
		)
		chance = 7.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.IRON_INGOT,
					32
				), chance
			)
		)
		chance = 7.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.NOTE_BLOCK,
					4
				), chance
			)
		)
		chance = 8.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.REDSTONE,
					64
				), chance
			)
		)
		chance = 12.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.CAKE
				), chance
			)
		)
		chance = 12.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.GLOWSTONE,
					12
				), chance
			)
		)
		chance = 12.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.SEA_LANTERN,
					4
				), chance
			)
		)
		chance = 12.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.PRISMARINE,
					4
				), chance
			)
		)
		chance = 12.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.PRISMARINE,
					4,
					1.toShort()
				), chance
			)
		)
		chance = 12.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.PRISMARINE,
					4,
					2.toShort()
				), chance
			)
		)
		chance = 14.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.PAINTING,
					8
				), chance
			)
		)
		chance = 15.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.COBBLESTONE,
					64
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.COOKED_BEEF,
					4
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.COOKED_CHICKEN,
					4
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.COOKED_MUTTON,
					4
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.COOKED_RABBIT,
					4
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.COOKED_COD,
					4
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.BREAD,
					4
				), chance
			)
		)
		chance = 16.0
		prizes.add(
			RandomItem(
				ItemStack(
					Material.APPLE,
					4
				), chance
			)
		)

		logger.info("${prizes.size} registered prizes!")
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun generateCaixaSecreta(_level: Int? = null, worldName: String? = null): ItemStack {
		val level = _level ?: DreamUtils.random.nextInt(0, 5)

		var caixa = ItemStack(Material.CHEST)
			.rename("§6✪ §a§lCaixa Secreta §6✪")
			.meta<ItemMeta> {
				setCustomModelData(level + 1)
			}

		val rarityLevel = when (level) {
			4 -> "§8[§a|||||§8]"
			3 -> "§8[§a||||§4|§8]"
			2 -> "§8[§a|||§4||§8]"
			1 -> "§8[§a||§4|||§8]"
			0 -> "§8[§a|§4||||§8]"
			else -> throw RuntimeException("Trying to create secret box with invalid level! Level: $level")
		}

		caixa = caixa.lore("§7Mas... o que será que tem aqui dentro?", "§7", "§3Coloque no chão e descubra!", "§7", "§7Nível de raridade: ${rarityLevel}")
		caixa = caixa.storeMetadata("caixaSecretaLevel", level.toString())
		if (worldName != null)
			caixa = caixa.storeMetadata("caixaSecretaWorld", worldName)

		return caixa
	}
}