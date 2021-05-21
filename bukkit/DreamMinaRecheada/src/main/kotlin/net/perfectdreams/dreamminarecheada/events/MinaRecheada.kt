package net.perfectdreams.dreamminarecheada.events

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamminarecheada.DreamMinaRecheada
import net.perfectdreams.dreamminarecheada.utils.MinaRecheadaData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import java.time.Instant
import java.time.ZoneId

class MinaRecheada(val m: DreamMinaRecheada) : ServerEvent("Mina Recheada", "/mina") {
    var minaRecheadaData = MinaRecheadaData()
    private var location1: Location? = null
    private var location2: Location? = null
    var bossBar1: BossBar? = null
    var bossBar2: BossBar? = null
    val minaRecheadaWorld: World
        get() = Bukkit.getWorld("MinaRecheada")!!
    val timeZone = ZoneId.of("America/Sao_Paulo")
    override var delayBetween: Long
        get() {
            val lastMinaRecheadaInMillis = m.config.getLong("last-mina-recheada", 0L)
            val lastMinaRecheada = Instant.ofEpochMilli(lastMinaRecheadaInMillis)
                .atZone(timeZone)
            val now = Instant.now()
                .atZone(timeZone)

            val minaRecheadaHappenedToday = (lastMinaRecheada.dayOfMonth == now.dayOfMonth && lastMinaRecheada.year == now.year && lastMinaRecheada.monthValue == now.monthValue)
            return if (minaRecheadaHappenedToday)
                now.toOffsetDateTime()
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .toEpochSecond() * 1000
            else
                now.toEpochSecond() * 1000
        }
        set(value) {}

    init {
        this.requiredPlayers = 80
        this.discordAnnouncementRole = "538805118384996392"
    }

    override fun startNow(): Boolean {
        val lastMinaRecheadaInMillis = m.config.getLong("last-mina-recheada", 0L)
        val lastMinaRecheada = Instant.ofEpochMilli(lastMinaRecheadaInMillis)
            .atZone(timeZone)
        val now = Instant.now()
            .atZone(timeZone)

        // Mina Recheada can happen once every day
        val minaRecheadaHappenedToday = (lastMinaRecheada.dayOfMonth == now.dayOfMonth && lastMinaRecheada.year == now.year && lastMinaRecheada.monthValue == now.monthValue)

        return !minaRecheadaHappenedToday && Bukkit.getOnlinePlayers().size >= requiredPlayers
    }

    override fun getWarmUpAnnouncementMessage(idx: Int): String {
        return DreamMinaRecheada.PREFIX + "§ePreparem as suas §f逖 §9picaretas §f逖§e! Evento Mina Recheada irá iniciar em $idx segundos! §6/minarecheada"
    }

    override fun preStart() {
        running = true
        m.config.set("last-mina-recheada", System.currentTimeMillis())
        m.saveConfig()

        if (minaRecheadaData.pos1 == null)
            return
        if (minaRecheadaData.pos2 == null)
            return

        // Agora tudo é pedra!
        location1 = minaRecheadaData.pos1
        location2 = minaRecheadaData.pos2
        fillWith(Material.STONE, location1!!, location2!!)
        running = true

        val bossBar1 = Bukkit.createBossBar("§e§lMina §6§lR§e§le§6§lc§e§lh§6§le§e§la§6§ld§e§la§8", BarColor.PINK, BarStyle.SOLID)
        minaRecheadaWorld.players.forEach { bossBar1.addPlayer(it) }
        this.bossBar1 = bossBar1
        val bossBar2 = Bukkit.createBossBar("§aPreparem-se, para um dos melhores eventos do servidor! ʕ•ᴥ•ʔ", BarColor.BLUE, BarStyle.SOLID)
        minaRecheadaWorld.players.forEach { bossBar2.addPlayer(it) }
        this.bossBar2 = bossBar2

        countdown()
    }

    override fun start() {
        startMinaRecheada()
    }

    fun replaceInAllWith(from: Material, to: Material) {
        replaceWith(from, to, location1!!, location2!!)
    }

    fun replaceInAllWith(from: Material, to: Material, chance: Double) {
        replaceWith(from, to, chance, location1!!, location2!!)
    }

    fun startMinaRecheada() {
        replaceInAllWith(Material.STONE, Material.COAL_ORE, 7.5)
        replaceInAllWith(Material.STONE, Material.IRON_ORE, 5.0)
        replaceInAllWith(Material.STONE, Material.LAPIS_ORE, 5.0)
        replaceInAllWith(Material.STONE, Material.REDSTONE_ORE, 5.0)
        replaceInAllWith(Material.STONE, Material.GOLD_ORE, 3.5)
        replaceInAllWith(Material.STONE, Material.DIAMOND_ORE, 2.0)
        replaceInAllWith(Material.STONE, Material.EMERALD_ORE, 1.0)

        Bukkit.broadcastMessage(DreamMinaRecheada.PREFIX + "§ePreparem as suas §f逖 §9picaretas §f逖§e! Evento Mina Recheada começou! §6/minarecheada")

        eventLoop()
    }

    fun fixTheMina(from: Material, chance: Double, location1: Location, location2: Location) {
        val blocks = BlockUtils.getBlocksFromTwoLocations(location1, location2)

        for (block in blocks) {
            if (block.type == from && chance(chance)) {
                var material = Material.COAL_ORE

                if (from == Material.STONE) {
                    material = Material.COAL_ORE
                }
                if (from == Material.COAL_ORE) {
                    material = Material.IRON_ORE
                }
                if (from == Material.IRON_ORE) {
                    material = if (chance(50.0)) Material.REDSTONE_ORE else Material.LAPIS_ORE
                }
                if (from == Material.REDSTONE_ORE) {
                    material = if (chance(50.0)) Material.GOLD_ORE else Material.LAPIS_ORE
                }
                if (from == Material.LAPIS_ORE) {
                    material = if (chance(50.0)) Material.GOLD_ORE else Material.REDSTONE_ORE
                }
                if (from == Material.GOLD_ORE) {
                    material = Material.DIAMOND_ORE
                }
                if (from == Material.DIAMOND_ORE) {
                    material = Material.EMERALD_ORE
                }
                if (from == Material.EMERALD_ORE) {
                    material = Material.BEACON
                }
                block.type = material
            }
        }
    }

    fun fixAllMinas(from: Material, chance: Double) {
        fixTheMina(from, chance, location1!!, location2!!)
    }

    fun eventLoop() {
        val fourMinutes = (60 * 4) * 20

        minaRecheadaWorld.playSound(
            Location(minaRecheadaWorld, 0.5, 80.0, 0.5),
            "perfectdreams.sfx.special_stage",
            10000f,
            1f
        )

        scheduler().schedule(DreamMinaRecheada.INSTANCE) {
            // Event Loop da Mina Recheada
            var elapsedTicks = 0

            while (true) {
                val brokenBlocksRatio = getBrokenBlocksRatio()

                // Depois de quatro minutos
                if (elapsedTicks == (60 * 4) * 20 || brokenBlocksRatio == 1.0) {
                    // Acabou!
                    Bukkit.broadcastMessage(DreamMinaRecheada.PREFIX + "§eEvento Mina Recheada acabou! Obrigado a todos que participaram!")
                    lastTime = System.currentTimeMillis()
                    running = false
                    replaceInAllWith(Material.STONE, Material.AIR)
                    replaceInAllWith(Material.COAL_ORE, Material.AIR)
                    replaceInAllWith(Material.IRON_ORE, Material.AIR)
                    replaceInAllWith(Material.REDSTONE_ORE, Material.AIR)
                    replaceInAllWith(Material.LAPIS_ORE, Material.AIR)
                    replaceInAllWith(Material.GOLD_ORE, Material.AIR)
                    replaceInAllWith(Material.DIAMOND_ORE, Material.AIR)
                    replaceInAllWith(Material.EMERALD_ORE, Material.AIR)
                    replaceInAllWith(Material.BEACON, Material.AIR)
                    bossBar1?.removeAll()
                    bossBar1 = null
                    bossBar2?.removeAll()
                    bossBar2 = null
                    location1 = null
                    location2 = null
                    return@schedule
                }

                if (elapsedTicks == 2680) {
                    // tocar novamente a super musiquinha
                    minaRecheadaWorld.playSound(
                        Location(Bukkit.getWorld("MinaRecheada"), 0.5, 80.0, 0.5),
                        "perfectdreams.sfx.special_stage",
                        10000f,
                        1f
                    )
                }

                // Atualizar bossbar
                if (elapsedTicks % 100 == 0) {
                    bossBar1?.progress = 1.0 - (elapsedTicks.toDouble() / fourMinutes.toDouble())
                    bossBar1?.setTitle("§e§lMina §6§lR§e§le§6§lc§e§lh§6§le§e§la§6§ld§e§la§8")

                    val blockCount = getBlockCount()
                    val blockRatio = blockCount.second.toDouble() / blockCount.first.toDouble()

                    bossBar2?.progress = blockRatio

                    when {
                        blockRatio >= 0.75 -> bossBar2?.color = BarColor.WHITE
                        blockRatio >= 0.5 -> bossBar2?.color = BarColor.BLUE
                        blockRatio >= 0.25 -> bossBar2?.color = BarColor.PINK
                        else -> bossBar2?.color = BarColor.PURPLE
                    }
                    bossBar2?.setTitle("§e${String.format("%.2f", brokenBlocksRatio * 100)}% §ablocos quebrados")
                }

                // Depois de três minutos
                if (elapsedTicks == (60 * 3) * 20) {
                    minaRecheadaWorld.players.forEach { it.sendMessage(DreamMinaRecheada.PREFIX + "§aAlguns blocos começam a mudar... Faltam um minuto para acabar a Mina Recheada!") }

                    fixAllMinas(Material.STONE, 7.0)
                    fixAllMinas(Material.COAL_ORE, 5.0)
                    fixAllMinas(Material.IRON_ORE, 3.0)
                    fixAllMinas(Material.LAPIS_ORE, 2.5)
                    fixAllMinas(Material.REDSTONE_ORE, 2.5)
                    fixAllMinas(Material.GOLD_ORE, 2.5)
                    fixAllMinas(Material.DIAMOND_ORE, 2.5)
                    fixAllMinas(Material.EMERALD_ORE, 1.0)

                    bossBar1?.color = BarColor.RED
                }

                // Depois de dois minutos
                if (elapsedTicks == (60 * 2) * 20) {
                    minaRecheadaWorld.players.forEach { it.sendMessage(DreamMinaRecheada.PREFIX + "§aAlguns blocos começam a mudar... Faltam dois minutos para acabar a Mina Recheada!") }

                    fixAllMinas(Material.STONE, 7.0)
                    fixAllMinas(Material.COAL_ORE, 5.0)
                    fixAllMinas(Material.IRON_ORE, 3.0)
                    fixAllMinas(Material.LAPIS_ORE, 2.5)
                    fixAllMinas(Material.REDSTONE_ORE, 2.5)

                    bossBar1?.color = BarColor.YELLOW
                }

                // Depois de um minuto
                if (elapsedTicks == 60 * 20) {
                    minaRecheadaWorld.players.forEach { it.sendMessage(DreamMinaRecheada.PREFIX + "§aAlguns blocos começam a mudar... Faltam três minutos para acabar a Mina Recheada!") }

                    fixAllMinas(Material.STONE, 7.0)
                    fixAllMinas(Material.COAL_ORE, 5.0)
                    fixAllMinas(Material.IRON_ORE, 3.0)

                    bossBar1?.color = BarColor.GREEN
                }

                waitFor(20L)
                elapsedTicks += 20
            }
        }
    }

    fun getBlockCount(): Pair<Int, Int> {
        var total = 0
        var broken = 0

        val blocks = BlockUtils.getBlocksFromTwoLocations(location1!!, location2!!)

        for (b in blocks) {
            if (b.type == Material.QUARTZ_BLOCK || b.type == Material.QUARTZ_PILLAR || b.type == Material.CHISELED_QUARTZ_BLOCK || b.type == Material.LADDER)
                continue
            if (b.location.isWithinRegion("ladders_1") || b.location.isWithinRegion("ladders_2") || b.location.isWithinRegion("ladders_3") || b.location.isWithinRegion("ladders_4"))
                continue

            total++
            if (b.type == Material.AIR)
                broken++
        }
        return Pair(total, broken)
    }

    fun getBrokenBlocksRatio(): Double {
        var total = 0
        var broken = 0

        val blocks = BlockUtils.getBlocksFromTwoLocations(location1!!, location2!!)

        for (b in blocks) {
            if (b.type == Material.QUARTZ_BLOCK || b.type == Material.QUARTZ_PILLAR || b.type == Material.CHISELED_QUARTZ_BLOCK || b.type == Material.LADDER)
                continue
            if (b.location.isWithinRegion("ladders_1") || b.location.isWithinRegion("ladders_2") || b.location.isWithinRegion("ladders_3") || b.location.isWithinRegion("ladders_4"))
                continue

            total++
            if (b.type == Material.AIR)
                broken++
        }
        return broken.toDouble() / total.toDouble()
    }

    companion object {
        fun fillWith(fill: Material, location1: Location, location2: Location) {
            val blocks = BlockUtils.getBlocksFromTwoLocations(location1, location2)

            for (b in blocks) {
                if (b.type == Material.QUARTZ_BLOCK || b.type == Material.QUARTZ_PILLAR || b.type == Material.CHISELED_QUARTZ_BLOCK || b.type == Material.LADDER)
                    continue
                if (b.location.isWithinRegion("ladders_1") || b.location.isWithinRegion("ladders_2") || b.location.isWithinRegion("ladders_3") || b.location.isWithinRegion("ladders_4"))
                    continue

                b.type = fill
            }
        }

        fun replaceWith(from: Material, to: Material, location1: Location, location2: Location) {
            val blocks = BlockUtils.getBlocksFromTwoLocations(location1, location2)

            for (b in blocks) {
                if (b.type == from) {
                    b.type = to
                }
            }
        }

        fun replaceWith(from: Material, to: Material, chance: Double, location1: Location, location2: Location) {
            val blocks = BlockUtils.getBlocksFromTwoLocations(location1, location2)

            for (b in blocks) {
                if (b.type == from && chance(chance)) {
                    b.type = to
                }
            }
        }
    }
}
