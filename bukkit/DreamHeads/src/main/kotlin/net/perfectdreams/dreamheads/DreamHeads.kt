package net.perfectdreams.dreamheads

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.meta
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
            var skull = when (e.entity) {
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Blaze\"}"},SkullOwner:{Id:"df81d31d-881a-49fb-bfec-68ec5cab5f7d",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIwNjU3ZTI0YjU2ZTFiMmY4ZmMyMTlkYTFkZTc4OGMwYzI0ZjM2Mzg4YjFhNDA5ZDBjZDJkOGRiYTQ0YWEzYiJ9fX0="}]}}} 1
                is Blaze -> createCustomSkull(
                    "Cabeça de Blaze",
                    UUID.fromString("df81d31d-881a-49fb-bfec-68ec5cab5f7d"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIwNjU3ZTI0YjU2ZTFiMmY4ZmMyMTlkYTFkZTc4OGMwYzI0ZjM2Mzg4YjFhNDA5ZDBjZDJkOGRiYTQ0YWEzYiJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Chicken\"}"},SkullOwner:{Id:"7d3a8ace-e045-4eba-ab71-71dbf525daf1",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ=="}]}}} 1
                is Chicken -> createCustomSkull(
                    "Cabeça de Galinha",
                    UUID.fromString("7d3a8ace-e045-4eba-ab71-71dbf525daf1"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ=="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Cow\"}"},SkullOwner:{Id:"dea3aea6-c69b-4eeb-9a18-0e3792fa37a8",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0="}]}}} 1
                is Cow -> createCustomSkull(
                    "Cabeça de Vaca",
                    UUID.fromString("dea3aea6-c69b-4eeb-9a18-0e3792fa37a8"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Enderman\"}"},SkullOwner:{Id:"0de98464-1274-4dd6-bba8-370efa5d41a8",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0="}]}}} 1
                is Enderman -> createCustomSkull(
                    "Cabeça de Enderman",
                    UUID.fromString("0de98464-1274-4dd6-bba8-370efa5d41a8"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Ghast\"}"},SkullOwner:{Id:"807f287f-6499-4e93-a887-0a298ab3091f",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI2YTcyMTM4ZDY5ZmJiZDJmZWEzZmEyNTFjYWJkODcxNTJlNGYxYzk3ZTVmOTg2YmY2ODU1NzFkYjNjYzAifX19"}]}}} 1
                is Ghast -> createCustomSkull(
                    "Cabeça de Ghast",
                    UUID.fromString("807f287f-6499-4e93-a887-0a298ab3091f"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI2YTcyMTM4ZDY5ZmJiZDJmZWEzZmEyNTFjYWJkODcxNTJlNGYxYzk3ZTVmOTg2YmY2ODU1NzFkYjNjYzAifX19"
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Iron Golem\"}"},SkullOwner:{Id:"e8811207-bad5-40c3-b833-f87b621c8971",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTEzZjM0MjI3MjgzNzk2YmMwMTcyNDRjYjQ2NTU3ZDY0YmQ1NjJmYTlkYWIwZTEyYWY1ZDIzYWQ2OTljZjY5NyJ9fX0="}]}}} 1
                is Golem -> createCustomSkull(
                    "Cabeça de Golem",
                    UUID.fromString("e8811207-bad5-40c3-b833-f87b621c8971"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTEzZjM0MjI3MjgzNzk2YmMwMTcyNDRjYjQ2NTU3ZDY0YmQ1NjJmYTlkYWIwZTEyYWY1ZDIzYWQ2OTljZjY5NyJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Magma Cube\"}"},SkullOwner:{Id:"d14e8ee6-ed93-4e0e-8861-342b76e3fd37",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFjOTdhMDZlZmRlMDRkMDAyODdiZjIwNDE2NDA0YWIyMTAzZTEwZjA4NjIzMDg3ZTFiMGMxMjY0YTFjMGYwYyJ9fX0="}]}}} 1
                is MagmaCube -> createCustomSkull(
                    "Cabeça de Magma Cube",
                    UUID.fromString("d14e8ee6-ed93-4e0e-8861-342b76e3fd37"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFjOTdhMDZlZmRlMDRkMDAyODdiZjIwNDE2NDA0YWIyMTAzZTEwZjA4NjIzMDg3ZTFiMGMxMjY0YTFjMGYwYyJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Mooshroom Cow\"}"},SkullOwner:{Id:"93368024-3a7a-4644-aa79-3dcdebf41ea1",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI1Mjg0MWYyZmQ1ODllMGJjODRjYmFiZjllMWMyN2NiNzBjYWM5OGY4ZDZiM2RkMDY1ZTU1YTRkY2I3MGQ3NyJ9fX0="}]}}} 1
                is MushroomCow -> createCustomSkull(
                    "Cabeça de Coguvaca",
                    UUID.fromString("93368024-3a7a-4644-aa79-3dcdebf41ea1"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI1Mjg0MWYyZmQ1ODllMGJjODRjYmFiZjllMWMyN2NiNzBjYWM5OGY4ZDZiM2RkMDY1ZTU1YTRkY2I3MGQ3NyJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Ocelot\"}"},SkullOwner:{Id:"664dd492-3fcd-443b-9e61-4c7ebd9e4e10",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY1N2NkNWMyOTg5ZmY5NzU3MGZlYzRkZGNkYzY5MjZhNjhhMzM5MzI1MGMxYmUxZjBiMTE0YTFkYjEifX19"}]}}} 1
                is Ocelot -> createCustomSkull(
                    "Cabeça de Jaguatirica",
                    UUID.fromString("664dd492-3fcd-443b-9e61-4c7ebd9e4e10"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY1N2NkNWMyOTg5ZmY5NzU3MGZlYzRkZGNkYzY5MjZhNjhhMzM5MzI1MGMxYmUxZjBiMTE0YTFkYjEifX19"
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Pig\"}"},SkullOwner:{Id:"e1e1c2e4-1ed2-473d-bde2-3ec718535399",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0="}]}}} 1
                is Pig -> createCustomSkull(
                    "Cabeça de Porco",
                    UUID.fromString("e1e1c2e4-1ed2-473d-bde2-3ec718535399"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Zombie Pigman\"}"},SkullOwner:{Id:"c07e6ecd-1d19-4647-b3e6-bb04ff386bf0",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWUwMGI3MzMzMmQ1ZDc2Yzc0NzY2ODAyOTZiNGMzNmNiZmQ3YWM1MDI0NjMyYjcyYjVmMDQ1NjQ4YTRlMDVkYiJ9fX0="}]}}} 1
                is PigZombie -> createCustomSkull(
                    "Cabeça de Homem-Porco Zumbi",
                    UUID.fromString("c07e6ecd-1d19-4647-b3e6-bb04ff386bf0"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWUwMGI3MzMzMmQ1ZDc2Yzc0NzY2ODAyOTZiNGMzNmNiZmQ3YWM1MDI0NjMyYjcyYjVmMDQ1NjQ4YTRlMDVkYiJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"White Sheep\"}"},SkullOwner:{Id:"c1adea7f-79e7-4a04-964b-4bc6a1e87d20",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjkyZGYyMTZlY2QyNzYyNGFjNzcxYmFjZmJmZTAwNmUxZWQ4NGE3OWU5MjcwYmUwZjg4ZTljODc5MWQxZWNlNCJ9fX0="}]}}} 1
                is Sheep -> createCustomSkull(
                    "Cabeça de Ovelha",
                    UUID.fromString("c1adea7f-79e7-4a04-964b-4bc6a1e87d20"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjkyZGYyMTZlY2QyNzYyNGFjNzcxYmFjZmJmZTAwNmUxZWQ4NGE3OWU5MjcwYmUwZjg4ZTljODc5MWQxZWNlNCJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Slime\"}"},SkullOwner:{Id:"7f0b0873-df6a-4a19-9bcd-f6c90ef804c7",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="}]}}} 1
                is Slime -> createCustomSkull(
                    "Cabeça de Slime",
                    UUID.fromString("7f0b0873-df6a-4a19-9bcd-f6c90ef804c7"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Spider\"}"},SkullOwner:{Id:"8bdb71d0-4724-48b2-9344-e79480424798",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="}]}}} 1
                is Spider -> createCustomSkull(
                    "Cabeça de Aranha",
                    UUID.fromString("8bdb71d0-4724-48b2-9344-e79480424798"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Cave Spider\"}"},SkullOwner:{Id:"d9c412f1-5d3a-4bbe-9514-b943f881d60f",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA0ZDVmY2IyODlmZTY1YjY3ODY2ODJlMWM3MzZjM2Y3YjE2ZjM5ZDk0MGUzZDJmNDFjZjAwNDA3MDRjNjI4MiJ9fX0="}]}}} 1
                is CaveSpider -> createCustomSkull(
                    "Cabeça de Aranha de Caverna",
                    UUID.fromString("d9c412f1-5d3a-4bbe-9514-b943f881d60f"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA0ZDVmY2IyODlmZTY1YjY3ODY2ODJlMWM3MzZjM2Y3YjE2ZjM5ZDk0MGUzZDJmNDFjZjAwNDA3MDRjNjI4MiJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Wither\"}"},SkullOwner:{Id:"119c371b-ea16-47c9-ad7f-23b3d894520a",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RmNzRlMzIzZWQ0MTQzNjk2NWY1YzU3ZGRmMjgxNWQ1MzMyZmU5OTllNjhmYmI5ZDZjZjVjOGJkNDEzOWYifX19"}]}}} 1
                is Wither -> createCustomSkull(
                    "Cabeça de Wither",
                    UUID.fromString("119c371b-ea16-47c9-ad7f-23b3d894520a"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RmNzRlMzIzZWQ0MTQzNjk2NWY1YzU3ZGRmMjgxNWQ1MzMyZmU5OTllNjhmYmI5ZDZjZjVjOGJkNDEzOWYifX19"
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Squid\"}"},SkullOwner:{Id:"1d1bdf36-9472-41de-b691-ae316ed90bab",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDljMmM5Y2U2N2ViNTk3MWNjNTk1ODQ2M2U2YzlhYmFiOGU1OTlhZGMyOTVmNGQ0MjQ5OTM2YjAwOTU3NjlkZCJ9fX0="}]}}} 1
                is Squid -> createCustomSkull(
                    "Cabeça de Polvo",
                    UUID.fromString("1d1bdf36-9472-41de-b691-ae316ed90bab"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDljMmM5Y2U2N2ViNTk3MWNjNTk1ODQ2M2U2YzlhYmFiOGU1OTlhZGMyOTVmNGQ0MjQ5OTM2YjAwOTU3NjlkZCJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Villager\"}"},SkullOwner:{Id:"0a9e8efb-9191-4c81-80f5-e27ca5433156",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODIyZDhlNzUxYzhmMmZkNGM4OTQyYzQ0YmRiMmY1Y2E0ZDhhZThlNTc1ZWQzZWIzNGMxOGE4NmU5M2IifX19"}]}}} 1
                is Villager -> createCustomSkull(
                    "Cabeça de Villager",
                    UUID.fromString("0a9e8efb-9191-4c81-80f5-e27ca5433156"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODIyZDhlNzUxYzhmMmZkNGM4OTQyYzQ0YmRiMmY1Y2E0ZDhhZThlNTc1ZWQzZWIzNGMxOGE4NmU5M2IifX19"
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Guardian\"}"},SkullOwner:{Id:"628f15d0-e39c-4fd9-9c4e-8c41d4f54b29",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBiZjM0YTcxZTc3MTViNmJhNTJkNWRkMWJhZTVjYjg1Zjc3M2RjOWIwZDQ1N2I0YmZjNWY5ZGQzY2M3Yzk0In19fQ=="}]}}} 1
                is Guardian -> createCustomSkull(
                    "Cabeça de Guardião",
                    UUID.fromString("628f15d0-e39c-4fd9-9c4e-8c41d4f54b29"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBiZjM0YTcxZTc3MTViNmJhNTJkNWRkMWJhZTVjYjg1Zjc3M2RjOWIwZDQ1N2I0YmZjNWY5ZGQzY2M3Yzk0In19fQ=="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Vex\"}"},SkullOwner:{Id:"f83bcfc1-0213-4957-888e-d3e2fae71203",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3MzMwYzdkNWNkOGEwYTU1YWI5ZTk1MzIxNTM1YWM3YWUzMGZlODM3YzM3ZWE5ZTUzYmVhN2JhMmRlODZiIn19fQ=="}]}}} 1
                is Vex -> createCustomSkull(
                    "Cabeça de Vex",
                    UUID.fromString("f83bcfc1-0213-4957-888e-d3e2fae71203"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3MzMwYzdkNWNkOGEwYTU1YWI5ZTk1MzIxNTM1YWM3YWUzMGZlODM3YzM3ZWE5ZTUzYmVhN2JhMmRlODZiIn19fQ=="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Wolf\"}"},SkullOwner:{Id:"aaa01ef9-e51c-4ca4-91b5-a57988cf06b3",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjRkNzcyN2Y1MjM1NGQyNGE2NGJkNjYwMmEwY2U3MWE3YjQ4NGQwNTk2M2RhODNiNDcwMzYwZmFhOWNlYWI1ZiJ9fX0="}]}}} 1
                is Wolf -> createCustomSkull(
                    "Cabeça de Lobo",
                    UUID.fromString("aaa01ef9-e51c-4ca4-91b5-a57988cf06b3"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjRkNzcyN2Y1MjM1NGQyNGE2NGJkNjYwMmEwY2U3MWE3YjQ4NGQwNTk2M2RhODNiNDcwMzYwZmFhOWNlYWI1ZiJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Witch\"}"},SkullOwner:{Id:"7f92b3d6-5ee0-4ab6-afae-2206b9514a63",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMTNkMTg0NzRmYzk0ZWQ1NWFlYjcwNjk1NjZlNDY4N2Q3NzNkYWMxNmY0YzNmODcyMmZjOTViZjlmMmRmYSJ9fX0="}]}}} 1
                is Witch -> createCustomSkull(
                    "Cabeça de Bruxa",
                    UUID.fromString("7f92b3d6-5ee0-4ab6-afae-2206b9514a63"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMTNkMTg0NzRmYzk0ZWQ1NWFlYjcwNjk1NjZlNDY4N2Q3NzNkYWMxNmY0YzNmODcyMmZjOTViZjlmMmRmYSJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Stray\"}"},SkullOwner:{Id:"6a732098-1e05-4b30-9ae2-ef14fb58cd1b",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM1MDk3OTE2YmMwNTY1ZDMwNjAxYzBlZWJmZWIyODcyNzdhMzRlODY3YjRlYTQzYzYzODE5ZDUzZTg5ZWRlNyJ9fX0="}]}}} 1
                is Stray -> createCustomSkull(
                    "Cabeça de Stray",
                    UUID.fromString("6a732098-1e05-4b30-9ae2-ef14fb58cd1b"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM1MDk3OTE2YmMwNTY1ZDMwNjAxYzBlZWJmZWIyODcyNzdhMzRlODY3YjRlYTQzYzYzODE5ZDUzZTg5ZWRlNyJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Silverfish\"}"},SkullOwner:{Id:"9ad1b276-a15f-4b80-b07f-dec00328b2d0",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJlYzJjM2NiOTVhYjc3ZjdhNjBmYjRkMTYwYmNlZDRiODc5MzI5YjYyNjYzZDdhOTg2MDY0MmU1ODhhYjIxMCJ9fX0="}]}}} 1
                is Silverfish -> createCustomSkull(
                    "Cabeça de Silverfish",
                    UUID.fromString("9ad1b276-a15f-4b80-b07f-dec00328b2d0"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJlYzJjM2NiOTVhYjc3ZjdhNjBmYjRkMTYwYmNlZDRiODc5MzI5YjYyNjYzZDdhOTg2MDY0MmU1ODhhYjIxMCJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Shulker\"}"},SkullOwner:{Id:"95f94f3e-e61c-4e97-a11b-562f44dace9d",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM3YTI5NGY2YjdiNGJhNDM3ZTVjYjM1ZmIyMGY0Njc5MmU3YWMwYTQ5MGE2NjEzMmE1NTcxMjRlYzVmOTk3YSJ9fX0="}]}}} 1
                is Shulker -> createCustomSkull(
                    "Cabeça de Shulker",
                    UUID.fromString("95f94f3e-e61c-4e97-a11b-562f44dace9d"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM3YTI5NGY2YjdiNGJhNDM3ZTVjYjM1ZmIyMGY0Njc5MmU3YWMwYTQ5MGE2NjEzMmE1NTcxMjRlYzVmOTk3YSJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Rabbit\"}"},SkullOwner:{Id:"02703b0c-573f-4042-a91b-659a3981b508",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZlY2M2YjVlNmVhNWNlZDc0YzQ2ZTc2MjdiZTNmMDgyNjMyN2ZiYTI2Mzg2YzZjYzc4NjMzNzJlOWJjIn19fQ=="}]}}} 1
                is Rabbit -> createCustomSkull(
                    "Cabeça de Coelho",
                    UUID.fromString("02703b0c-573f-4042-a91b-659a3981b508"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZlY2M2YjVlNmVhNWNlZDc0YzQ2ZTc2MjdiZTNmMDgyNjMyN2ZiYTI2Mzg2YzZjYzc4NjMzNzJlOWJjIn19fQ=="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Parrot\"}"},SkullOwner:{Id:"e9754a09-69c6-4e01-8f5e-ad640f0359df",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQ0ODJhNjVkMDA0YmNlYjcxNDEzNDk2MzY5MTE2MzBkZmZiYTY4ZmYyMWI4ZDUxZDkxNzc4NTBmOTQ0YzZmZiJ9fX0="}]}}} 1
                is Parrot -> createCustomSkull(
                    "Cabeça de Papagaio",
                    UUID.fromString("e9754a09-69c6-4e01-8f5e-ad640f0359df"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQ0ODJhNjVkMDA0YmNlYjcxNDEzNDk2MzY5MTE2MzBkZmZiYTY4ZmYyMWI4ZDUxZDkxNzc4NTBmOTQ0YzZmZiJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Llama\"}"},SkullOwner:{Id:"a70aaecf-8fb9-4afe-8019-b1adc1cfcf2b",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc3NmE3OGY5NjI0NGUzZGE3MzJmYWZmZDkzYTMzOTgzNGRiMjdiNjk1NWJmN2E5YjI0YWU5ODEyNWI3ZWQifX19"}]}}} 1
                is Llama -> createCustomSkull(
                    "Cabeça de Lhama",
                    UUID.fromString("a70aaecf-8fb9-4afe-8019-b1adc1cfcf2b"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc3NmE3OGY5NjI0NGUzZGE3MzJmYWZmZDkzYTMzOTgzNGRiMjdiNjk1NWJmN2E5YjI0YWU5ODEyNWI3ZWQifX19"
                )

                // Their heads are the same!
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Evoker\"}"},SkullOwner:{Id:"fad62cda-1112-4e2b-bfaf-0b3252ec953c",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5ZjEzM2E4NWZlMDBkM2NmMjUyYTA0ZDZmMmViMjUyMWZlMjk5YzA4ZTBkOGI3ZWRiZjk2Mjc0MGEyMzkwOSJ9fX0="}]}}} 1
                is Evoker -> createCustomSkull(
                    "Cabeça de Evoker",
                    UUID.fromString("fad62cda-1112-4e2b-bfaf-0b3252ec953c"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5ZjEzM2E4NWZlMDBkM2NmMjUyYTA0ZDZmMmViMjUyMWZlMjk5YzA4ZTBkOGI3ZWRiZjk2Mjc0MGEyMzkwOSJ9fX0="
                )
                is Illager -> createCustomSkull(
                    "Cabeça de Illager",
                    UUID.fromString("f83bcfc1-0213-4957-888e-d3e2fae71203"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3MzMwYzdkNWNkOGEwYTU1YWI5ZTk1MzIxNTM1YWM3YWUzMGZlODM3YzM3ZWE5ZTUzYmVhN2JhMmRlODZiIn19fQ=="
                )
                is Pillager -> createCustomSkull(
                    "Cabeça de Pillager",
                    UUID.fromString("fad62cda-1112-4e2b-bfaf-0b3252ec953c"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5ZjEzM2E4NWZlMDBkM2NmMjUyYTA0ZDZmMmViMjUyMWZlMjk5YzA4ZTBkOGI3ZWRiZjk2Mjc0MGEyMzkwOSJ9fX0="
                )

                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Endermite\"}"},SkullOwner:{Id:"af1e1c5d-c3af-45c5-93eb-212c2a485338",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0="}]}}} 1
                is Endermite -> createCustomSkull(
                    "Cabeça de Endermite",
                    UUID.fromString("af1e1c5d-c3af-45c5-93eb-212c2a485338"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0="
                )
                is PolarBear -> createCustomSkull(
                    "Cabeça de Urso Polar",
                    UUID.fromString("32eb8a61-6e66-4dfd-874e-7481190014a9"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1ZDYwYTRkNzBlYzEzNmE2NTg1MDdjZTgyZTM0NDNjZGFhMzk1OGQ3ZmNhM2Q5Mzc2NTE3YzdkYjRlNjk1ZCJ9fX0="
                )
                // /give @p minecraft:player_head{display:{Name:"{\"text\":\"Ravager\"}"},SkullOwner:{Id:"c510588d-5bb3-499a-b05f-d0d26142bde7",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2QyMGJmNTJlYzM5MGEwNzk5Mjk5MTg0ZmM2NzhiZjg0Y2Y3MzJiYjFiZDc4ZmQxYzRiNDQxODU4ZjAyMzVhOCJ9fX0="}]}}} 1
                is Ravager -> createCustomSkull(
                    "Cabeça de Ravager",
                    UUID.fromString("c510588d-5bb3-499a-b05f-d0d26142bde7"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2QyMGJmNTJlYzM5MGEwNzk5Mjk5MTg0ZmM2NzhiZjg0Y2Y3MzJiYjFiZDc4ZmQxYzRiNDQxODU4ZjAyMzVhOCJ9fX0="
                )
                is Fox -> createCustomSkull(
                    "Cabeça de Raposa",
                    UUID.fromString("237a2651-7da8-457a-aaea-3714bcc196a2"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDg5NTRhNDJlNjllMDg4MWFlNmQyNGQ0MjgxNDU5YzE0NGEwZDVhOTY4YWVkMzVkNmQzZDczYTNjNjVkMjZhIn19fQ=="
                )

                is Bee -> createCustomSkull(
                    "Cabeça de Abelha",
                    UUID.fromString("658b47f7-fdfd-4a77-bb35-77c0f9ed2ba8"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3NDc4N2UzNjE1OWU0ZDc0ZGI1ZTI1YmFiYTk4N2I2NjVkY2M4OTBiZTlmMjYyYmIwY2JjZjVkMDFiODJiNiJ9fX0="
                )

                is Dolphin -> createCustomSkull(
                    "Cabeça de Golfinho",
                    UUID.fromString("8b7ccd6d-36de-47e0-8d5a-6f6799c6feb8"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ=="
                )

                is Turtle -> createCustomSkull(
                    "Cabeça de Tartaruga",
                    UUID.fromString("245f22b4-2c7c-4a9c-86fa-9ec64c54e4fa"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ=="
                )

                is Panda -> createCustomSkull(
                    "Cabeça de Panda",
                    UUID.fromString("bf7435c9-b7eb-49e9-8887-60697f8081b9"),
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNhMDk2ZWVhNTA2MzAxYmVhNmQ0YjE3ZWUxNjA1NjI1YTZmNTA4MmM3MWY3NGE2MzljYzk0MDQzOWY0NzE2NiJ9fX0="
                )

                is Skeleton -> {
                    ItemStack(Material.SKELETON_SKULL)
                }
                is Zombie -> {
                    ItemStack(Material.ZOMBIE_HEAD)
                }
                is Creeper -> {
                    ItemStack(Material.CREEPER_HEAD)
                }
                else -> null
            }

            if (skull != null)
                e.drops.add(skull)
        }
    }

    private fun createCustomSkull(itemName: String, uuid: UUID, value: String) = ItemStack(Material.PLAYER_HEAD)
        .meta<SkullMeta> {
            playerProfile = Bukkit.createProfile(uuid)
                .apply {
                    this.setProperty(
                        ProfileProperty(
                            "textures",
                            value
                        )
                    )
                }

            displayName(Component.text(itemName).decoration(TextDecoration.ITALIC, false))
        }
}