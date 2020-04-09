package net.perfectdreams.dreamheads

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class DreamHeads : KotlinPlugin(), Listener {
    val cachedOfflinePlayers = mutableMapOf<String, OfflinePlayer>()

    fun getOfflinePlayer(name: String) = cachedOfflinePlayers.getOrPut(name) { Bukkit.getOfflinePlayer(name) }

    override fun softEnable() {
        super.softEnable()

        registerEvents(this)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDeath(e: EntityDeathEvent) {
        val chance = if (e.entity.killer?.name == "MrPowerGamerBR") 100.0 else 1.0

        if (chance(chance)) {
            var skull = ItemStack(Material.PLAYER_HEAD)
            val meta = skull.itemMeta as SkullMeta
            var forceUpdate = false

            when (e.entity) {
                is Blaze -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Blaze")
                    meta.setDisplayName("§rCabeça de Blaze")
                }
                is Chicken -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Chicken")
                    meta.setDisplayName("§rCabeça de Galinha")
                }
                is Cow -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Cow")
                    meta.setDisplayName("§rCabeça de Vaca")
                }
                is Enderman -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Enderman")
                    meta.setDisplayName("§rCabeça de Enderman")
                }
                is Ghast -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Ghast")
                    meta.setDisplayName("§rCabeça de Ghast")
                }
                is Golem -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Golem")
                    meta.setDisplayName("§rCabeça de Golem")
                }
                is MagmaCube -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_LavaSlime")
                    meta.setDisplayName("§rCabeça de Cubo de Magma")
                }
                is MushroomCow -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_MushroomCow")
                    meta.setDisplayName("§rCabeça de Coguvaca")
                }
                is Ocelot -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Ocelot")
                    meta.setDisplayName("§rCabeça de Jaguatirica")
                }
                is Pig -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Pig")
                    meta.setDisplayName("§rCabeça de Porco")
                }
                is PigZombie -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_PigZombie")
                    meta.setDisplayName("§rCabeça de Homem-Porco Zumbi")
                }
                is Sheep -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Sheep")
                    meta.setDisplayName("§rCabeça de Ovelha")
                }
                is Slime -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Slime")
                    meta.setDisplayName("§rCabeça de Slime")
                }
                is Spider -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Spider")
                    meta.setDisplayName("§rCabeça de Aranha")
                }
                is CaveSpider -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_CaveSpider")
                    meta.setDisplayName("§rCabeça de Caverna")
                }
                is Wither -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Wither")
                    meta.setDisplayName("§rCabeça de Wither")
                }
                is Squid -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Squid")
                    meta.setDisplayName("§rCabeça de Lula")
                }
                is Villager -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Villager")
                    meta.setDisplayName("§rCabeça de Villager")
                }
                is Guardian -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Guardian")
                    meta.setDisplayName("§rCabeça de Guardião")
                }
                is Vex -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Vex")
                    meta.setDisplayName("§rCabeça de Vex")
                }
                is Illager -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Illager")
                    meta.setDisplayName("§rCabeça de Illager")
                }
                is Wolf -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Wolf")
                    meta.setDisplayName("§rCabeça de Lobo")
                }
                is Witch -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Witch")
                    meta.setDisplayName("§rCabeça de Bruxa")
                }
                is Stray -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Stray")
                    meta.setDisplayName("§rCabeça de Stray")
                }
                is Silverfish -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Silverfish")
                    meta.setDisplayName("§rCabeça de Traça")
                }
                is Shulker -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Shulker")
                    meta.setDisplayName("§rCabeça de Shulker")
                }
                is Rabbit -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Rabbit")
                    meta.setDisplayName("§rCabeça de Coelho")
                }
                is Parrot -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Parrot")
                    meta.setDisplayName("§rCabeça de Papagaio")
                }
                is Llama -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Llama")
                    meta.setDisplayName("§rCabeça de Lhama")
                }
                is Evoker -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Evoker")
                    meta.setDisplayName("§rCabeça de Evoker")
                }
                is Endermite -> {
                    meta.owningPlayer = getOfflinePlayer("MHF_Endermite")
                    meta.setDisplayName("§rCabeça de Endermite")
                }
                is PolarBear -> {
                    meta.playerProfile = Bukkit.createProfile(UUID.fromString("32eb8a61-6e66-4dfd-874e-7481190014a9"))
                        .apply {
                            this.setProperty(
                                ProfileProperty(
                                    "textures",
                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1ZDYwYTRkNzBlYzEzNmE2NTg1MDdjZTgyZTM0NDNjZGFhMzk1OGQ3ZmNhM2Q5Mzc2NTE3YzdkYjRlNjk1ZCJ9fX0="
                                )
                            )
                        }
                    meta.setDisplayName("§rCabeça de Urso Polar")
                    forceUpdate = true
                }
                is Ravager -> {

                }
                is Pillager -> {

                }
                is Fox -> {
                    meta.playerProfile = Bukkit.createProfile(UUID.fromString("237a2651-7da8-457a-aaea-3714bcc196a2"))
                        .apply {
                            this.setProperty(
                                ProfileProperty(
                                    "textures",
                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDg5NTRhNDJlNjllMDg4MWFlNmQyNGQ0MjgxNDU5YzE0NGEwZDVhOTY4YWVkMzVkNmQzZDczYTNjNjVkMjZhIn19fQ=="
                                )
                            )
                        }
                    meta.setDisplayName("§rCabeça de Raposa")
                    forceUpdate = true
                }
                is Bee -> {
                    meta.playerProfile = Bukkit.createProfile(UUID.fromString("658b47f7-fdfd-4a77-bb35-77c0f9ed2ba8"))
                        .apply {
                            this.setProperty(
                                ProfileProperty(
                                    "textures",
                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3NDc4N2UzNjE1OWU0ZDc0ZGI1ZTI1YmFiYTk4N2I2NjVkY2M4OTBiZTlmMjYyYmIwY2JjZjVkMDFiODJiNiJ9fX0="
                                )
                            )
                        }
                    meta.setDisplayName("§rCabeça de Abelha")
                    forceUpdate = true
                }
                is Dolphin -> {
                    meta.playerProfile = Bukkit.createProfile(UUID.fromString("8b7ccd6d-36de-47e0-8d5a-6f6799c6feb8"))
                        .apply {
                            this.setProperty(
                                ProfileProperty(
                                    "textures",
                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ=="
                                )
                            )
                        }
                    meta.setDisplayName("§rCabeça de Golfinho")
                    forceUpdate = true
                }
                is Turtle -> {
                    meta.playerProfile = Bukkit.createProfile(UUID.fromString("245f22b4-2c7c-4a9c-86fa-9ec64c54e4fa"))
                        .apply {
                            this.setProperty(
                                ProfileProperty(
                                    "textures",
                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ=="
                                )
                            )
                        }
                    meta.setDisplayName("§rCabeça de Tartaruga")
                    forceUpdate = true
                }
                is Panda -> {
                    meta.playerProfile = Bukkit.createProfile(UUID.fromString("bf7435c9-b7eb-49e9-8887-60697f8081b9"))
                        .apply {
                            this.setProperty(
                                ProfileProperty(
                                    "textures",
                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNhMDk2ZWVhNTA2MzAxYmVhNmQ0YjE3ZWUxNjA1NjI1YTZmNTA4MmM3MWY3NGE2MzljYzk0MDQzOWY0NzE2NiJ9fX0="
                                )
                            )
                        }
                    meta.setDisplayName("§rCabeça de Tartaruga")
                    forceUpdate = true
                }
                is Skeleton -> {
                    skull = ItemStack(Material.SKELETON_SKULL)
                    forceUpdate = true
                }
                is Zombie -> {
                    skull = ItemStack(Material.ZOMBIE_HEAD)
                    forceUpdate = true
                }
                is Creeper -> {
                    skull = ItemStack(Material.CREEPER_HEAD)
                    forceUpdate = true
                }
            }

            if (meta.hasOwner() || forceUpdate) {
                skull.itemMeta = meta
                e.drops.add(skull)
            }
        }
    }
}