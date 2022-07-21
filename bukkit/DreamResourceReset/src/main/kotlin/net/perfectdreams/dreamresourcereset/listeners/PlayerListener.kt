package net.perfectdreams.dreamresourcereset.listeners

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.adventure.lore
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamcore.utils.extensions.getSafeDestination
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.fromBase64Item
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreamcorreios.DreamCorreios
import net.perfectdreams.dreammapwatermarker.DreamMapWatermarker
import net.perfectdreams.dreamresourcereset.DreamResourceReset
import net.perfectdreams.dreamresourcereset.tables.DeathChestMaps
import net.perfectdreams.dreamresourcereset.tables.DeathChestsInformation
import net.perfectdreams.dreamresourcereset.utils.DeathChestMapCharacter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*


class PlayerListener(val m: DreamResourceReset) : Listener {
    @EventHandler
    fun onMapInit(e: MapInitializeEvent) {
        m.initializeDeathChestMap(e.map)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCreeperExplode(e: EntityExplodeEvent) {
        if (e.entity.type == EntityType.CREEPER) {
            e.blockList().removeIf {
                // Remove any chest that has the IS_DEATH_CHEST metadata
                it.type == Material.CHEST && (it.state as? Chest)?.persistentDataContainer?.get(DreamResourceReset.IS_DEATH_CHEST, PersistentDataType.BYTE) == 1.toByte()
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDeath(e: PlayerDeathEvent) {
        val attributes = m.worldAttributesMap[e.player.world.name] ?: return
        val canYouLoseItems = attributes.canYouLoseItems()

        // Get where the chest should be placed
        val whereThePlayerDied = e.player.location
        val whereTheBlockShouldBePlaced = whereThePlayerDied.getSafeDestination()
            .block

        if (!canYouLoseItems || !e.player.canBreakAt(whereTheBlockShouldBePlaced.location, Material.CHEST)) {
            e.keepInventory = true
            e.keepLevel = true

            // The documentation says that you need to clear the drops and clear the dropped XP if you set to keep inventory/level
            e.drops.clear()
            e.droppedExp = 0
        } else {
            e.keepInventory = false
            e.keepLevel = false

            val lastDamageCause = e.player.lastDamageCause?.cause
            if (lastDamageCause == EntityDamageEvent.DamageCause.LAVA) {
                e.player.sendMessage("§cAíííí! Você morreu na lava, então o seu túmulo derreteu!")
                return
            }

            whereTheBlockShouldBePlaced.type = Material.CHEST
            val state = whereTheBlockShouldBePlaced.state as Chest
            state.persistentDataContainer.set(
                DreamResourceReset.IS_DEATH_CHEST,
                PersistentDataType.BYTE,
                1
            )

            val droppedExp = e.droppedExp
            val items = e.drops.joinToString(";") { it.toBase64() }

            e.drops.clear()
            e.droppedExp = 0

            // Create map of where they died
            val mapView = Bukkit.createMap(whereThePlayerDied.world)

            mapView.centerX = whereTheBlockShouldBePlaced.x
            mapView.centerZ = whereTheBlockShouldBePlaced.z
            mapView.isTrackingPosition = true
            mapView.isUnlimitedTracking = true

            val map = ItemStack(Material.FILLED_MAP)
                .meta<MapMeta> {
                    displayNameWithoutDecorations("Mapa do seu Túmulo") {
                        color(NamedTextColor.YELLOW)
                        decorate(TextDecoration.BOLD)
                    }

                    lore {
                        textWithoutDecorations("Siga o mapa para encontrar a") {
                            color(NamedTextColor.GREEN)
                            decorate(TextDecoration.BOLD)
                        }
                        textWithoutDecorations("localização do seu túmulo!") {
                            color(NamedTextColor.GREEN)
                            decorate(TextDecoration.BOLD)
                        }
                        emptyLine()
                        textWithoutDecorations {
                            color(NamedTextColor.GRAY)

                            append("Mas cuidado...")
                        }
                        textWithoutDecorations {
                            color(NamedTextColor.GRAY)
                            append("Outras pessoas podem roubar o seu túmulo") {
                                color(NamedTextColor.RED)
                                decorate(TextDecoration.BOLD)
                            }

                            append("!")
                        }
                        emptyLine()
                        textWithoutDecorations {
                            append("X: ") {
                                color(NamedTextColor.GOLD)
                            }
                            append(whereTheBlockShouldBePlaced.x.toString())
                        }
                        textWithoutDecorations {
                            append("Y: ") {
                                color(NamedTextColor.GOLD)
                            }
                            append(whereTheBlockShouldBePlaced.y.toString())
                        }
                        textWithoutDecorations {
                            append("Z: ") {
                                color(NamedTextColor.GOLD)
                            }
                            append(whereTheBlockShouldBePlaced.z.toString())
                        }
                    }

                    this.mapView = mapView
                }
                .also { DreamMapWatermarker.watermarkMap(it, null) }

            e.itemsToKeep.add(map)

            m.launchAsyncThread {
                val chestId = transaction(Databases.databaseNetwork) {
                    val insertedInformation = DeathChestsInformation.insert {
                        it[DeathChestsInformation.player] = e.player.uniqueId
                        it[DeathChestsInformation.createdAt] = Instant.now()
                        it[DeathChestsInformation.worldName] = whereThePlayerDied.world.name
                        it[DeathChestsInformation.items] = items
                        it[DeathChestsInformation.xp] = droppedExp
                        it[DeathChestsInformation.x] = whereTheBlockShouldBePlaced.x
                        it[DeathChestsInformation.y] = whereTheBlockShouldBePlaced.y
                        it[DeathChestsInformation.z] = whereTheBlockShouldBePlaced.z
                        it[DeathChestsInformation.found] = false
                        it[DeathChestsInformation.resetVersion] = m.config.getInt(
                            "resourceWorldChange",
                            0
                        )
                    }

                    DeathChestMaps.insert {
                        it[DeathChestMaps.id] = mapView.id
                        it[DeathChestMaps.chest] = insertedInformation[DeathChestsInformation.id]
                        it[DeathChestMaps.character] = DeathChestMapCharacter.values().random()
                    }

                    // Get chest ID because we will insert it on the database
                    insertedInformation[DeathChestsInformation.id].value
                }

                onMainThread {
                    state.persistentDataContainer.set(
                        DreamResourceReset.DEATH_CHEST_ID,
                        PersistentDataType.LONG,
                        chestId
                    )
                    state.update()

                    m.initializeDeathChestMap(mapView)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDeathChestInteract(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        if (clickedBlock.type != Material.CHEST)
            return

        val state = clickedBlock.state as Chest
        if (state.persistentDataContainer.get(DreamResourceReset.IS_DEATH_CHEST, PersistentDataType.BYTE) != 1.toByte())
            return

        val chestId = state.persistentDataContainer.get(DreamResourceReset.DEATH_CHEST_ID, PersistentDataType.LONG)

        if (chestId != null) {
            e.isCancelled = true
            processFoundChest(e.player, clickedBlock, false)
        }
    }

    private fun processFoundChest(
        player: Player,
        clickedBlock: Block,
        bypassOtherPlayerCheck: Boolean
    ) {
        m.launchAsyncThread {
            val result = transaction(Databases.databaseNetwork) {
                val info = getDeathChestInfo(clickedBlock.world, clickedBlock.x, clickedBlock.y, clickedBlock.z) ?: return@transaction null

                if (info[DeathChestsInformation.player].value != player.uniqueId && !bypassOtherPlayerCheck)
                    return@transaction DifferentUserFound(
                        Users.select { Users.id eq info[DeathChestsInformation.player] }
                            .firstOrNull()?.getOrNull(Users.username)
                    )

                DeathChestsInformation.update({ DeathChestsInformation.id eq info[DeathChestsInformation.id] }) {
                    it[DeathChestsInformation.found] = true
                    it[DeathChestsInformation.foundBy] = player.uniqueId
                    it[DeathChestsInformation.foundAt] = Instant.now()
                    it[DeathChestsInformation.gaveBackToUser] = false
                }

                val mapId = transaction(Databases.databaseNetwork) {
                    DeathChestMaps.select { DeathChestMaps.chest eq info[DeathChestsInformation.id] }
                        .limit(1)
                        .firstOrNull()?.getOrNull(DeathChestMaps.id)?.value
                }

                return@transaction Success(
                    mapId,
                    info[DeathChestsInformation.player].value,
                    Users.select { Users.id eq info[DeathChestsInformation.player] }
                        .firstOrNull()?.getOrNull(Users.username),
                    info[DeathChestsInformation.items],
                    info[DeathChestsInformation.xp]
                )
            } ?: return@launchAsyncThread

            when (result) {
                is DifferentUserFound -> {
                    val (playerName) = result

                    onMainThread {
                        val menu = createMenu(9, "Túmulo de $playerName") {
                            slot(3) {
                                item = ItemStack(Material.PAPER)
                                    .meta<ItemMeta> {
                                        displayNameWithoutDecorations("Devolver para o jogador que morreu") {
                                            color(NamedTextColor.YELLOW)
                                            decorate(TextDecoration.BOLD)
                                        }
                                        setCustomModelData(49)
                                    }

                                onClick {
                                    it.closeInventory()

                                    giveItemsToPlayer(player, clickedBlock)
                                }
                            }

                            slot(5) {
                                item = ItemStack(Material.PAPER)
                                    .meta<ItemMeta> {
                                        displayNameWithoutDecorations("Pegar os itens para você") {
                                            color(NamedTextColor.RED)
                                            decorate(TextDecoration.BOLD)
                                        }
                                        setCustomModelData(50)
                                    }

                                onClick {
                                    it.closeInventory()

                                    processFoundChest(player, clickedBlock, true)
                                }
                            }

                            slot(8) {
                                item = ItemStack(Material.BARRIER)
                                    .meta<ItemMeta> {
                                        displayNameWithoutDecorations("Eita! Em caso de investigação policial...") {
                                            color(NamedTextColor.GRAY)
                                            decorate(TextDecoration.BOLD)
                                        }
                                    }

                                onClick {
                                    it.closeInventory()
                                }
                            }
                        }

                        menu.sendTo(player)
                    }
                }

                is Success -> {
                    val (mapId, playerId, playerName, itemsAsBase64, deathDroppedXp) = result

                    onMainThread {
                        // Trigger a map renderer update if possible
                        if (mapId != null)
                            Bukkit.getMap(mapId)?.let { m.initializeDeathChestMap(it) }

                        clickedBlock.type = Material.AIR

                        player.giveExp(deathDroppedXp)
                        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.7f)

                        val items = itemsAsBase64.split(";").filter { it.isNotEmpty() }.map { it.fromBase64Item() }
                        val whereItemsShouldBeDropped = clickedBlock.location.add(0.0, 0.5, 0.0)

                        for (item in items) {
                            clickedBlock.world.dropItemNaturally(
                                whereItemsShouldBeDropped,
                                item
                            )
                        }

                        if (playerId == player.uniqueId) {
                            player.sendMessage("§aParabéns! Você conseguiu encontrar o seu túmulo!")
                        } else if (playerName != null) {
                            player.sendMessage("§aVocê encontrou o túmulo de §b${playerName}§a!")
                        }
                    }
                }
            }
        }
    }

    private fun giveItemsToPlayer(
        player: Player,
        clickedBlock: Block
    ) {
        m.launchAsyncThread {
            val (mapId, playerId, playerName, itemsAsBase64, xp) = transaction(Databases.databaseNetwork) {
                val info = getDeathChestInfo(clickedBlock.world, clickedBlock.x, clickedBlock.y, clickedBlock.z) ?: return@transaction null

                DeathChestsInformation.update({ DeathChestsInformation.id eq info[DeathChestsInformation.id] }) {
                    it[DeathChestsInformation.found] = true
                    it[DeathChestsInformation.foundBy] = player.uniqueId
                    it[DeathChestsInformation.foundAt] = Instant.now()
                    it[DeathChestsInformation.gaveBackToUser] = true
                }

                val mapId = transaction(Databases.databaseNetwork) {
                    DeathChestMaps.select { DeathChestMaps.chest eq info[DeathChestsInformation.id] }
                        .limit(1)
                        .firstOrNull()?.getOrNull(DeathChestMaps.id)?.value
                }

                return@transaction Success(
                    mapId,
                    info[DeathChestsInformation.player].value,
                    Users.select { Users.id eq info[DeathChestsInformation.player] }
                        .firstOrNull()?.getOrNull(Users.username),
                    info[DeathChestsInformation.items],
                    info[DeathChestsInformation.xp]
                )
            } ?: return@launchAsyncThread

            onMainThread {
                // Trigger a map renderer update if possible
                if (mapId != null)
                    Bukkit.getMap(mapId)?.let { m.initializeDeathChestMap(it) }

                clickedBlock.type = Material.AIR

                val items = itemsAsBase64.split(";").filter { it.isNotEmpty() }.map { it.fromBase64Item() }

                DreamCorreios.getInstance().addItem(playerId, *items.toTypedArray())

                val playerDeathChestOwner = Bukkit.getPlayer(playerId)
                player.sendMessage("§aVocê devolveu os itens do túmulo para §b${playerName}§a!")
                playerDeathChestOwner?.sendMessage("§b${player.name}§a encontrou seu túmulo e decidiu devolver os itens para você!")
            }
        }
    }

    private fun getDeathChestInfo(world: World, x: Int, y: Int, z: Int) = DeathChestsInformation.select {
        DeathChestsInformation.worldName eq world.name and
                (DeathChestsInformation.x eq x) and
                (DeathChestsInformation.y eq y) and
                (DeathChestsInformation.z eq z) and
                (DeathChestsInformation.found eq false) and
                (DeathChestsInformation.resetVersion eq m.config.getInt(
                    "resourceWorldChange",
                    0
                ))
    }.firstOrNull()

    sealed class Result
    data class DifferentUserFound(val playerName: String?) : Result()
    data class Success(
        val mapId: Int?,
        val playerId: UUID,
        val playerName: String?,
        val itemsAsBase64: String,
        val xp: Int
    ) : Result()
}