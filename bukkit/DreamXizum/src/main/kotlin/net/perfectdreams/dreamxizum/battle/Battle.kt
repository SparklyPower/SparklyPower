package net.perfectdreams.dreamxizum.battle

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.elo.Rating
import net.perfectdreams.dreamxizum.battle.npcs.Corpse
import net.perfectdreams.dreamxizum.battle.npcs.Referee
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.dao.Duelist
import net.perfectdreams.dreamxizum.extensions.*
import net.perfectdreams.dreamxizum.lobby.Lobby
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.SkullMeta

class Battle(val type: BattleType, val limit: Int, val author: Player? = null) {
    companion object {
        val arenas = XizumConfig.models.locations.arenas
        private val times = Title.Times.times(0.seconds, 1.seconds, 0.seconds)
        private val colors = listOf(0xFF69E3, 0xFF85CA, 0xFF9DAD, 0xF9BC92, 0xEEE07B).map { TextColor { it } }
        private val title = Component.text("")

        private val head = setOf(Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET)
        private val body = setOf(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE)
        private val legs = setOf(Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS)
        private val feet = setOf(Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS)
    }

    val corpses = mutableSetOf<DreamNPC>()
    val players = Array<BattleUser?>(limit) { null }
    var stage = BattleStage.CREATING_BATTLE
    var options = BattleOptions()
    val isFull get() = players.none { it == null }
    val canStart get() = isFull && players.none { it?.status == BattleUserStatus.PENDING }
    val alivePlayers get() = players.filter { it?.status == BattleUserStatus.ALIVE }.filterNotNull()
    private val range = limit / 2

    val team1 get() = players.toList().subList(0, range).filterNotNull()
    val team2 get() = players.toList().subList(range, limit).filterNotNull()

    operator fun contains(player: Player) = indexOf(player) > -1
    operator fun get(index: Int) = players.getOrNull(index)
    fun indexOf(player: Player) = players.indexOfFirst { it?.player == player }
    fun invite(player: Player) = with (players) { set(indexOfFirst { it == null }, BattleUser(player, BattleUserStatus.PENDING)) }
    fun remove(player: Player) { players[indexOf(player)] = null }

    fun canHit(player1: Player, player2: Player): Boolean {
        val index1 = indexOf(player1)
        val index2 = indexOf(player2)

        val min = minOf(index1, index2)
        val max = maxOf(index1, index2)

        return range in (min + 1) .. max
    }

    fun addToBattle(player: Player) {
        var index = 0

        players.let { array ->
            array.firstOrNull { it?.player == player } ?: invite(player)
            index = indexOf(player)
            array[index]!!.status = BattleUserStatus.ALIVE
        }

        player.prepareToBattle()
        player.teleport(if (type == BattleType.NORMAL) arenas.getPosition(range, index + 1)
            else arenas.getRankedPosition(index + 1))
        player.blockCommandsExcept(Lobby.allowedCommands, "Você não pode usar esse comando durante um xizum.")
        player.setPlayerTime(16_000, false)

        player.battle = this
        player.legacyPvp = options.legacyPvp
        player.allowMcMMO = options.allowMcMMO

        with (player.inventory) {
            if (options.itemsType == BattleItems.PLAYER_ITEMS) {
                player.inventory.contents = playerInventories[player]
                return@with
            }

            options.items.forEach {
                if (it.type == Material.SHIELD) setItemInOffHand(it)
                addItem(it)
            }

            with (player.inventory) {
                helmet = options.armor.firstOrNull { it.type in head }
                chestplate = options.armor.firstOrNull { it.type in body }
                leggings = options.armor.firstOrNull { it.type in legs }
                boots = options.armor.firstOrNull { it.type in feet }
            }

            val bow = options.items.firstOrNull { it.type == Material.BOW }
            val crossbow = options.items.firstOrNull{ it.type == Material.CROSSBOW }

            var arrows = 0
            var bowHasInfinity = false

            bow?.let {
                bowHasInfinity = it.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0
                arrows += if (bowHasInfinity) 1 else 64
            }

            crossbow?.let { arrows += if (bowHasInfinity) 63 else 64 }

            if (arrows > 0) for (i in 0 .. arrows / 65) player.inventory.addItem(Material.ARROW.toItemStack(arrows))

            var goldenApples = options.items.size * 2
            if (options.armor.isNotEmpty()) goldenApples += 3

            if (goldenApples > 0) player.inventory.addItem(Material.GOLDEN_APPLE.toItemStack(goldenApples))
        }

        DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) {
            Cash.takeCash(player, options.cash)
            switchContext(SynchronizationContext.SYNC)
            player.withdraw(options.sonecas)
        }

        players.forEach {
            if (it?.status != BattleUserStatus.ALIVE) return@forEach
            it.player.spawnParticle(Particle.CLOUD, player.location, 100, -.25, 1.0, -.25)
            // TODO: fix it so you don't need to use DreamCore.INSTANCE to show a partially hidden player
            it.player.showPlayer(DreamCore.INSTANCE, player)
            player.showPlayer(DreamCore.INSTANCE, it.player)
        }

        author?.let { attemptToStart() } ?: if (canStart) start()

        author?.let {
            if (player == author) {
                stage = BattleStage.WAITING_PLAYERS
                BattleHolograms.createAuthorHologram(this)
            } else {
                BattleHolograms.updateHolograms(this)
                BattleHolograms.createHologram(this, player)
            }
        }
    }

    fun attemptToStart() {
        if (canStart)
            DreamXizum.INSTANCE.schedule {
                broadcastMessage("A partida terá início em breve. Você tem ${highlight("5 segundos")} para desistir.")
                waitFor(100L)
                if (canStart) start()
            }
    }

    fun start() {
        BattleHolograms.deleteHolograms(this)
        stage = BattleStage.COUNTDOWN

        val referee = Referee(this).apply { spawn() }

        DreamXizum.INSTANCE.schedule {
            broadcastSubtitle(colors[0]) { "Preparad${it.artigo}?" }
            waitFor(20L)

            for (index in 3 downTo 0) {
                if (stage == BattleStage.FINISHED) return@schedule
                waitFor(20L)
                broadcastSubtitle(colors[4 - index]) {
                    referee.punch()
                    if (index > 0) "$index..." else "Vai!"
                }
            }

            referee.destroy()
            stage = BattleStage.FIGHTING
            alivePlayers.forEach { it.player.unfreeze() }

            DreamXizum.INSTANCE.schedule {
                with (options) {
                    while (timeLimit > 0 && stage < BattleStage.FINISHED) {
                        if (stage == BattleStage.FINISHED) return@with
                        timeLimit--

                        if (timeLimit == 0) finishBattle()
                        else broadcastMessage("${highlight(timeLimit.pluralize("minuto" to "minutes"))} restante${if (timeLimit > 1) "s" else ""}.")

                        waitFor(20L * 60)
                    }
                }
            }
        }
    }

    fun removeFromBattle(player: Player, reason: BattleDeathReason, location: Location = player.location) {
        val isAlive = this[indexOf(player)]?.status == BattleUserStatus.ALIVE
        val firstTeam = team1
        val secondTeam = team2

        when (stage) {
            BattleStage.WAITING_PLAYERS -> {
                if (player == author) return Matchmaker.cancelBattle(this, false)

                if (isAlive) {
                    player.teleportToServerSpawn()
                    player.freeFromBattle()

                    alivePlayers.forEach { player.hidePlayer(DreamCore.INSTANCE, it.player) }
                }

                remove(player)
                BattleHolograms.updateHolograms(this)
            }

            in BattleStage.COUNTDOWN .. BattleStage.FIGHTING -> {
                this[indexOf(player)]!!.status = BattleUserStatus.DECEASED
                val randomKiller = (if (firstTeam.any { it.player == player }) secondTeam.random() else firstTeam.random()).player

                val killer = player.killer ?: run {
                    val entity = player.lastDamageCause?.entity
                    if (entity is Player && entity in this) entity else randomKiller
                }

                corpses.forEach { it.removeViewer(player) }

                Corpse.createDeadBody(player, location).apply {
                    alivePlayers.forEach { addViewer(it.player) }
                    corpses.add(this)
                }

                broadcastMessage(when (reason) {
                    BattleDeathReason.DISCONNECTED -> "${highlight(player.name)} se apavorou e desconectou do jogo."
                    BattleDeathReason.TELEPORTED -> "${highlight(player.name)} se teletransportou e foi desclassificad${player.artigo} do xizum."
                    else -> DeathMessage.getRandomMessage(player, killer)
                })

                with (options) {
                    var message = "Você recebeu "

                    if (sonecas > 0) {
                        killer.deposit(sonecas)
                        message += "${highlight(sonecas.toLong().formatted)} sonecas"
                    }
                    if (cash > 0) {
                        DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) { Cash.giveCash(killer, cash) }
                        if (sonecas > 0) message += " e "
                        message += "${highlight(cash.formatted)} pesadelos"
                    }

                    if (sonecas + cash > 0) killer.sendMessage("${DreamXizum.PREFIX} $message por matar ${highlight(player.name)}.")
                }

                playerInventories[killer]?.let {
                    val inventory = Bukkit.createInventory(null, InventoryType.PLAYER)
                    val head = Material.PLAYER_HEAD.toItemStack().meta<SkullMeta> {
                        playerProfile = player.playerProfile
                    }

                    if (inventory.canHoldItem(head)) it.set(it.indexOfFirst { it.type == Material.AIR }, head)
                    else killer.sendMessage("${DreamXizum.PREFIX} Seu inventário estava cheio, então você não pôde pegar a cabeça de ${highlight(player.name)}.")
                }

                player.freeFromBattle()
                player.battle = null
                if (reason != BattleDeathReason.TELEPORTED) player.teleportToServerSpawn()

                if (firstTeam.none { it.status == BattleUserStatus.ALIVE }) finishBattle(team2 to team1)
                if (secondTeam.none { it.status == BattleUserStatus.ALIVE }) finishBattle(team1 to team2)
            }

            else -> {
                this[indexOf(player)]?.status = BattleUserStatus.LEFT
                sendPlayerToLobby(player)
            }
        }
    }

    fun finishBattle(winners: Pair<List<BattleUser>, List<BattleUser>>? = null) {
        stage = BattleStage.FINISHED
        winners?.let {
            alivePlayers.forEach {
                repeat (20) { _ ->
                    it.player.playSound(it.player.location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 10F, 1F)
                }
            }
            if (range == 1) {
                if (type == BattleType.RANKED) {
                    DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) {
                        val players = listOf(it.first[0].player, it.second[0].player)
                        val winnerId = players[0].uniqueId
                        val winner = Duelist.fetch(winnerId)
                        val loser = Duelist.fetch(players[1].uniqueId)
                        val ratings = Rating.calculatePoints(winner.points, loser.points, 1)

                        players.forEach {
                            if (it.uniqueId == winnerId) {
                                winner.addVictory()
                                winner.addPoints(ratings.first)
                                it.sendMessage("${DreamXizum.PREFIX} Parabéns por derrotar ${highlight(it.name)}, você recebeu ${highlight("${ratings.first} pontos")} por isso.")
                            } else {
                                var points = ratings.second
                                if (loser.points - points <= 0) points = loser.points
                                loser.addDefeat()
                                loser.addPoints(points)
                                it.sendMessage("${DreamXizum.PREFIX} Como você perdeu para ${highlight(it.name)}, você perdeu ${highlight("$points pontos")}.")
                            }
                        }
                    }
                } else {
                    broadcastMessage("Você venceu o xizum.")
                    sendEveryoneAliveToSpawn()

                }
            } else {
                broadcastMessage("Parabéns por vencerem o xizum contra o time inimigo.")
                sendEveryoneAliveToSpawn()
            }
        } ?: run {
            broadcastMessage("Houve um empate e ninguém venceu.")
            sendEveryoneAliveToSpawn()
        }
    }

    fun sendPlayerToLobby(player: Player) {
        corpses.forEach { it.removeViewer(player) }

        player.freeFromBattle()
        player.battle = null
        player.teleportToServerSpawn()

        player.deposit(options.sonecas)
        DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) { Cash.giveCash(player, options.cash) }
    }

    fun sendEveryoneAliveToSpawn() = alivePlayers.forEach { sendPlayerToLobby(it.player) }

    fun broadcastMessage(message: String) = players.forEach {
        if (it?.status == BattleUserStatus.ALIVE) it.player.sendMessage("${DreamXizum.PREFIX} $message")
    }

    fun broadcastSubtitle(color: TextColor, message: (Player) -> String) =
        players.forEach {
            if (it?.status == BattleUserStatus.ALIVE) it.player.showTitle(Title.title(title,
                Component.text(message.invoke(it.player)).style(Style.style(color)), times))
        }
}

class BattleUser(val player: Player, var status: BattleUserStatus)