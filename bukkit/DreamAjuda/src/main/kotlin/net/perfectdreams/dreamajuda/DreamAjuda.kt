package net.perfectdreams.dreamajuda

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamajuda.commands.AjudaExecutor
import net.perfectdreams.dreamajuda.commands.TransformRulesSignExecutor
import net.perfectdreams.dreamajuda.commands.declarations.AjudaCommand
import net.perfectdreams.dreamajuda.commands.declarations.DreamAjudaCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.adventure.sendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class DreamAjuda : KotlinPlugin(), Listener {
	companion object {
		private val RULES_VERSION = SparklyNamespacedKey("rules_version", PersistentDataType.INTEGER)
		val IS_RULES_SIGN = SparklyNamespacedBooleanKey("is_rules_sign")
	}

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)
		registerCommand(AjudaCommand(this))
		registerCommand(DreamAjudaCommand(this))
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onChat(e: AsyncPlayerChatEvent) {
		if (e.player.hasPermission("sparklypower.soustaff"))
			return

		if (!e.player.location.isWithinRegion("rules_island"))
			return

		e.isCancelled = true
		e.player.sendTextComponent {
			color(NamedTextColor.RED)
			content("Você precisa ler e aceitar as regras antes de poder conversar no chat!")
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		if (e.player.hasPermission("sparklypower.soustaff"))
			return

		if (!e.player.location.isWithinRegion("rules_island"))
			return

		e.isCancelled = true
		e.player.sendTextComponent {
			color(NamedTextColor.RED)
			content("Você precisa ler e aceitar as regras antes de poder usar comandos!")
		}
	}

	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		val rulesVersion = e.player.persistentDataContainer.get(RULES_VERSION)

		if (rulesVersion != config.getInt("rules-version")) {
			// New rules, teleport the player!
			e.player.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1100.5, 174.0, 1000.5, 270f, 0f))
		}
	}

	@EventHandler
	fun onRulesApproval(e: PlayerInteractEvent) {
		val clickedBlock = e.clickedBlock ?: return

		if (e.action != Action.LEFT_CLICK_BLOCK && e.action != Action.RIGHT_CLICK_BLOCK)
			return

		if (!clickedBlock.type.name.contains("_SIGN"))
			return

		val sign = clickedBlock.state as Sign
		val isRulesSign = sign.persistentDataContainer.get(IS_RULES_SIGN)
		if (!isRulesSign)
			return

		// The user accepted the rules, yay! Let's update the "rules version"...
		e.player.persistentDataContainer.set(RULES_VERSION, config.getInt("rules-version"))

		// And teleport it somewhere else!
		e.player.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1011.5, 100.0, 1000.5, 90f, 0f))
	}

	@EventHandler
	fun onMove(e: PlayerMoveEvent) {
		if (!e.displaced)
			return

		if (e.to.world.name == "TutorialIsland" && 0 >= e.to.y) {
			// If the player falls into the voice, we will teleport them somewhere else!
			// And teleport it somewhere else!
			e.player.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1011.5, 100.0, 1000.5, 90f, 0f))
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onTakeLectern(e: PlayerTakeLecternBookEvent) {
		if (e.player.world.name != "TutorialIsland")
			return

		val book = e.book ?: return

		if (book.type == Material.WRITABLE_BOOK) {
			if (e.player.hasPermission("sparklypower.soustaff"))
				return

			e.isCancelled = true

			e.player.sendMessage("§cEste livro não pode ser removido!")
			return
		}

		e.isCancelled = true

		e.player.inventory.addItem(
			book.clone().meta<BookMeta> {
				this.author(
					textComponent("Pantufa, a mascote do servidor") {
						color(NamedTextColor.GOLD)
					}
				)
			}
		)

		e.player.closeInventory()

		e.player.sendMessage("§aGostou do livro? Eu te dei uma cópia dele para que você possa ler ele no seu cafofo!")
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEntityEvent) {
		val entity = e.rightClicked

		// Faço a MÍNIMA IDEIA porque não tem o §a
		if (e.player.world.name == "TutorialIsland" && (entity.name == "Pantufa" || entity.name == "Loritta" || entity.name == "Gabriela")) {
			openMenu(e.player)
		}
	}

	fun openMenu(player: Player) {
		val menu = createMenu(27, "§6§lAjuda do §4§lSparkly§b§lPower") {
			slot(0, 0) {
				item = ItemStack(Material.CHICKEN_SPAWN_EGG)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.RED)
							decorate(TextDecoration.BOLD)
							content("Pets Queridos e Amigáveis")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 939.5, 90f, 0f))
				}
			}

			slot(4, 0) {
				item = ItemStack(Material.NETHER_STAR)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.GOLD)
							decorate(TextDecoration.BOLD)
							content("Ilha Principal da Ajuda")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1000.5, 100.0, 1000.5, 270f, 0f))
				}
			}

			slot(8, 0) {
				item = ItemStack(Material.EMERALD)
					.meta<ItemMeta> {
						setCustomModelData(1)

						displayNameWithoutDecorations {
							color(NamedTextColor.GREEN)
							decorate(TextDecoration.BOLD)
							content("Economia e Ostentações")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1000.5, 100.0, 1058.5, 270f, 0f))
				}
			}

			slot(3, 1) {
				item = ItemStack(Material.GOLDEN_SHOVEL)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.YELLOW)
							decorate(TextDecoration.BOLD)
							content("Proteção de Terrenos")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1000.5, 100.0, 941.5, 270f, 0f))
				}
			}

			slot(4, 1) {
				item = ItemStack(Material.LECTERN)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.GOLD)
							decorate(TextDecoration.BOLD)
							content("Informações Essenciais")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 995.5, 100.0, 1000.5, 90f,  0f))
				}
			}

			slot(5, 1) {
				item = ItemStack(Material.AXOLOTL_BUCKET)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.BLUE)
							decorate(TextDecoration.BOLD)
							content("Registro")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 1059.5, 0f,0f))
				}
			}

			slot(0, 2) {
				item = ItemStack(Material.PAPER)
					.meta<ItemMeta> {
						setCustomModelData(46)

						displayNameWithoutDecorations {
							color(NamedTextColor.AQUA)
							decorate(TextDecoration.BOLD)
							content("Nossos Itens Inovadores")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 971.5, 90f, 0f))
				}
			}

			slot(4, 2) {
				item = ItemStack(Material.BOOK)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.DARK_PURPLE)
							decorate(TextDecoration.BOLD)
							content("Regras do SparklyPower")
						}
					}

				onClick {
					Bukkit.dispatchCommand(it, "warp regras")
				}
			}

			slot(8, 2) {
				item = ItemStack(Material.ENCHANTING_TABLE)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.WHITE)
							decorate(TextDecoration.BOLD)
							content("Sistemas do SparklyPower")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 1029.5, 90f, 0f))
				}
			}
		}

		menu.sendTo(player)
	}
}