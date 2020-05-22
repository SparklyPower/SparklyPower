package net.perfectdreams.dreamfight.utils

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamfight.DreamFight
import net.perfectdreams.dreamcore.utils.extensions.girl
import net.perfectdreams.dreamcore.utils.extensions.healAndFeed
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.removeAllPotionEffects
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class FightArena(var m: DreamFight) {
    lateinit var lobby: Location
    lateinit var pos1: Location
    lateinit var pos2: Location
    lateinit var exit: Location
    var winner: Location? = null
    var players = ArrayList<Player>()
    var inventories = HashMap<Player, Array<ItemStack>>()
    var started = false
    var preStart = false
    var p1: Player? = null
    var p2: Player? = null
    var p3: Player? = null
    var p4: Player? = null
    var time1: String? = null
    var time2: String? = null
    var modifiers = ArrayList<FightModifier>()
    var moveToCamarote = ArrayList<String>()
    var prize = 75
    var multiplier = 1
    var winnerPrize = 15000
    var isPvPStarted = false

    fun preStartFight() {
        // MettatonEX.reserveEvent()
        preStart = true
        started = true
        p1 = null
        p2 = null
        players.clear()
        // Não é necessário isto, mas não custa nada deixar aí
        DreamFight.Companion.lastFight = System.currentTimeMillis()
        m.eventoFight.running = true
        multiplier = 1
        prize = 75
        /*
		 * Adicionar Modifiers
		 */
        modifiers.clear()
        for (fa in FightModifier.values()) {
            if (chance(4.0)) {
                modifiers.add(fa)
            }
        }
        /*
		 * Remover conflitos de Modifiers
		 */
        // Ainda não é suportado
        modifiers.remove(FightModifier.TWO_TEAM)
        if (modifiers.contains(FightModifier.KNOCKBACK)) {
            modifiers.remove(FightModifier.KNOCK_STICK)
            modifiers.remove(FightModifier.ONLY_HAND)
            modifiers.remove(FightModifier.HAMBURGER_POWER)
        }
        if (modifiers.contains(FightModifier.DIAMOND_SWORD)) {
            modifiers.remove(FightModifier.KNOCK_STICK)
            modifiers.remove(FightModifier.HAMBURGER_POWER)
        }
        if (modifiers.contains(FightModifier.ONLY_HAND)) {
            modifiers.remove(FightModifier.KNOCK_STICK)
        }
        if (modifiers.contains(FightModifier.SUPER_SPEED)) {
            modifiers.remove(FightModifier.SLOWNESS)
        }
        if (modifiers.contains(FightModifier.FULL_DIAMOND)) {
            modifiers.remove(FightModifier.THORNS_STRATEGY)
        }
        countdown(60)
    }

    fun countdownPvP(count: Int, p1: Player, p2: Player) {
        object : BukkitRunnable() {
            override fun run() {
                if (p1 == null || !players.contains(p1) || !p1.isOnline) {
                    sendToFightArena(FancyAsriel.fancy("§eAlguém saiu antes do PvP... :("))
                    players.remove(p1)
                    preparePvP()
                    this.cancel()
                    return
                }
                if (p2 == null || !players.contains(p2) || !p2.isOnline) {
                    sendToFightArena(FancyAsriel.fancy("§eAlguém saiu antes do PvP... :("))
                    players.remove(p2)
                    preparePvP()
                    this.cancel()
                    return
                }
                if (count == 0) {
                    sendToFightArena(FancyAsriel.fancy("§e§lLutem§e!"))
                    startPvPBetween(p1, p2)
                    this.cancel()
                    return
                }
                sendToFightArena(FancyAsriel.fancy("§e§l$count§e..."))
                val modCount = count - 1
                countdownPvP(modCount, p1, p2)
            }
        }.runTaskLater(m, 20L)
    }

    fun countdownPvPMulti(count: Int, p1: Player, p2: Player, p3: Player, p4: Player) {
        object : BukkitRunnable() {
            override fun run() {
                if (p1 == null || !players.contains(p1) || !p1.isOnline) {
                    sendToFightArena(FancyAsriel.fancy("§eAlguém saiu antes do PvP... :("))
                    players.remove(p1)
                    preparePvP()
                    this.cancel()
                    return
                }
                if (p2 == null || !players.contains(p2) || !p2.isOnline) {
                    sendToFightArena(FancyAsriel.fancy("§eAlguém saiu antes do PvP... :("))
                    players.remove(p2)
                    preparePvP()
                    this.cancel()
                    return
                }
                if (p3 == null || !players.contains(p3) || !p3.isOnline) {
                    sendToFightArena(FancyAsriel.fancy("§eAlguém saiu antes do PvP... :("))
                    players.remove(p3)
                    preparePvP()
                    this.cancel()
                    return
                }
                if (p4 == null || !players.contains(p4) || !p4.isOnline) {
                    sendToFightArena(FancyAsriel.fancy("§eAlguém saiu antes do PvP... :("))
                    players.remove(p4)
                    preparePvP()
                    this.cancel()
                    return
                }
                if (count == 0) {
                    sendToFightArena(FancyAsriel.fancy("§e§lLutem§e!"))
                    startPvPBetween(p1, p2, p3, p4)
                    this.cancel()
                    return
                }
                sendToFightArena(FancyAsriel.fancy("§e§l$count§e..."))
                val modCount = count - 1
                countdownPvPMulti(modCount, p1, p2, p3, p4)
            }
        }.runTaskLater(m, 20L)
    }

    fun countdown(count: Int) {
        m.schedule {
            for (i in 60 downTo 1) {
                val announce = (i in 15..60 && i % 15 == 0) || (i in 0..14 && i % 5 == 0)

                if (announce) {
                    var text = ""
                    if (!modifiers.isEmpty()) {
                        val names = ArrayList<String?>()
                        for (fa in modifiers) {
                            names.add(fa.title)
                        }
                        text = "(" + names.joinToString(", ").toString() + ") "
                    }
                    Bukkit.broadcastMessage("${DreamFight.prefix}§eEvento Fight " + text + "irá iniciar em §l" + i + " segundos§e! §e/fight §e(Guarde os itens antes de entrar, vai se dá problema)")
                }

                waitFor(20)
            }

            startFight()
        }
        /* object : BukkitRunnable() {
            fun run() {
                if (count == 0) {
                    startFight()
                    this.cancel()
                    return
                }
                var modCount = count.toDouble()
                val asriel = modCount / 15
                if (asriel as Int.toDouble () == asriel){
                    var text = ""
                    if (!modifiers.isEmpty()) {
                        val names = ArrayList<String?>()
                        for (fa in modifiers) {
                            names.add(fa.title)
                        }
                        text = "(" + names.joinToString(", ").toString() + ") "
                    }
                    m.getServer()
                        .broadcastMessage(FancyAsriel.fancy(SparklyFight.Companion.prefix + "§eEvento Fight " + text + "irá iniciar em §l" + count + " segundos§e! §e/fight"))
                }
                modCount = modCount - 1
                countdown(modCount.toInt())
            }
        }.runTaskLater(m, 20L) */
    }

    fun startFight() {
        if (1 >= players.size) {
            m.server
                .broadcastMessage(
                    FancyAsriel.fancy(
                        DreamFight.prefix + "§cInfelizmente o Evento Fight acabou devido a §lfalta de players§c..."
                    )
                )
            for (p in players) {
                if (p.isValid) {
                    clearInventoryWithArmorOf(p)
                    p.removeAllPotionEffects()
                    p.healAndFeed()
                    restoreInventoryOf(p)
                    p.teleport(m.fight.exit)
                }
            }
            players.clear()
            started = false
            preStart = false
            m.eventoFight.running = false
            m.eventoFight.lastTime = System.currentTimeMillis()
            return
        }
        m.server.broadcastMessage(FancyAsriel.fancy(DreamFight.prefix + "§eEvento Fight §liniciou§e! Perdeu o evento? Então vá no camarote usando §e/fight camarote§e!"))
        started = true
        preStart = false
        DreamFight.Companion.lastFight = System.currentTimeMillis()
        preparePvP()
    }

    fun preparePvP() {
        if (hasFightEnded())
            return

        var p1 = players.random()
        var p2 = players.random()
        var p3 = players.random()
        var p4 = players.random()

        // Bukkit.broadcastMessage("1." + p1Str);
// Bukkit.broadcastMessage("2." + p2Str);
        while (!p1.isValid || p1 == p2) {
            if (hasFightEnded())
                return

            if (!p1.isValid)
                players.remove(p1)

            p1 = players.random()
            // Bukkit.broadcastMessage("1." + p1Str);
// Bukkit.broadcastMessage("2." + p2Str);
            if (modifiers.contains(FightModifier.TWO_TEAM)) {
                if (3 >= players.size) { // Bukkit.broadcastMessage("Desativando 2v2!");
                    /*
					 * Desative o TWO_TEAM modifier caso o número de players seja pequeno
					 */
                    modifiers.remove(FightModifier.TWO_TEAM)
                    /*
					 * Pegue os players novamente...
					 */
                    preparePvP()
                    return
                }
            }
        }
        if (hasFightEnded()) {
            return
        }
        while (!p2.isValid || p1 == p2) {
            if (hasFightEnded())
                return

            if (!p2.isValid)
                players.remove(p2)

            p2 = players.random()

            if (modifiers.contains(FightModifier.TWO_TEAM)) {
                if (3 >= players.size) { // Bukkit.broadcastMessage("Desativando 2v2!");
/*
					 * Desative o TWO_TEAM modifier caso o número de players seja pequeno
					 */
                    modifiers.remove(FightModifier.TWO_TEAM)
                    /*
					 * Pegue os players novamente...
					 */preparePvP()
                    return
                }
            }
        }
        /* if (modifiers.contains(FightModifier.TWO_TEAM)) {
            while (!p3.isValid || p3 == p2 || p3 == p1 || p3 == p4) {
                if (hasFightEnded())
                    return

                if (!p3 == null)
                    players.remove(p1Str)

                p3Str = players.random()
                p3 = Bukkit.getPlayerExact(p3Str)
                if (modifiers.contains(FightModifier.TWO_TEAM)) {
                    if (3 >= players.size) { // Bukkit.broadcastMessage("Desativando 2v2!");
/*
						 * Desative o TWO_TEAM modifier caso o número de players seja pequeno
						 */
                        modifiers.remove(FightModifier.TWO_TEAM)
                        /*
						 * Pegue os players novamente...
						 */preparePvP()
                        return
                    }
                }
            }
            while (p4 == null || p4Str == p2Str || p4Str == p1Str || p4Str == p3Str) {
                if (hasFightEnded()) {
                    return
                }
                if (p4 == null) {
                    players.remove(p1Str)
                }
                p4Str = players.random()
                p4 = Bukkit.getPlayerExact(p4Str)
                if (modifiers.contains(FightModifier.TWO_TEAM)) {
                    if (3 >= players.size) { // Bukkit.broadcastMessage("Desativando 2v2!");
/*
						 * Desative o TWO_TEAM modifier caso o número de players seja pequeno
						 */
                        modifiers.remove(FightModifier.TWO_TEAM)
                        /*
						 * Pegue os players novamente...
						 */preparePvP()
                        return
                    }
                }
            }
        } */
        if (hasFightEnded()) {
            return
        }
        this.p1 = p1
        this.p2 = p2
        this.p3 = p3
        this.p4 = p4
        if (!modifiers.contains(FightModifier.TWO_TEAM)) {
            sendToFightArena("§c§k§l*...* §e§l" + p1.getDisplayName().toString() + "§e §4§lVS §e§l" + p2.getDisplayName().toString() + " §c§k§L*...*")
            var text = ""
            if (!modifiers.isEmpty()) {
                val names = ArrayList<String?>()
                for (fa in modifiers) {
                    names.add(fa.title)
                }
                text = "(" + names.joinToString(", ").toString() + ") "
            }
            for (p in lobby.world.players) {
                p.sendTitle(
                    "§a" + p1.getDisplayName().toString() + " §4§lVS §a" + p2.getDisplayName(),
                    text,
                    5,
                    50,
                    5
                )
            }
            countdownPvP(3, p1, p2)
        } else {
            var time1: String = RandomTeamName.rtn.random()
            val time2: String = RandomTeamName.rtn.random()
            while (time1 == time2) {
                time1 = RandomTeamName.rtn.random()
            }
            p1
            p2
            p3!!
            p4!!
            sendToFightArena("§c§k§l*...* §b§lTime " + time1 + ": §e§l" + p1.displayName + "§e e §e§l" + p2.displayName + " §c§k§L*...*")
            sendToFightArena("§c§k§l*...* §4§lTime " + time2 + ": §e§l" + p3.displayName + "§e e §e§l" + p4.displayName + " §c§k§L*...*")
            this.time1 = time1
            this.time2 = time2
            countdownPvPMulti(3, p1, p2, p3, p4)
        }
    }

    fun startPvPBetween(p1: Player, p2: Player) {
        initializeFightFor(p1)
        initializeFightFor(p2)
        p1.teleport(pos1)
        p2.teleport(pos2)
        isPvPStarted = true
    }

    fun startPvPBetween(p1: Player, p2: Player, p3: Player, p4: Player) {
        initializeFightFor(p1)
        initializeFightFor(p2)
        initializeFightFor(p3)
        initializeFightFor(p4)
        p1.teleport(pos1)
        p2.teleport(pos1)
        p3.teleport(pos2)
        p4.teleport(pos2)
    }

    fun initializeFightFor(p: Player) {
        var helmet = ItemStack(Material.AIR)
        var chestplate= ItemStack(Material.AIR)
        var leggings = ItemStack(Material.AIR)
        var boots = ItemStack(Material.AIR)
        var sword = ItemStack(Material.AIR)
        val golden = ItemStack(Material.GOLDEN_APPLE)
        if (!modifiers.contains(FightModifier.ONLY_HAND) && !modifiers.contains(
                FightModifier.KNOCK_STICK
            ) && !modifiers.contains(
                FightModifier.HAMBURGER_POWER
            )
        ) {
            helmet = ItemStack(Material.IRON_HELMET)
            chestplate = ItemStack(Material.IRON_CHESTPLATE)
            leggings = ItemStack(Material.IRON_LEGGINGS)
            boots = ItemStack(Material.IRON_BOOTS)

            if (modifiers.contains(FightModifier.FULL_DIAMOND)) {
                helmet = ItemStack(Material.DIAMOND_HELMET)
                chestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
                leggings = ItemStack(Material.DIAMOND_LEGGINGS)
                boots = ItemStack(Material.DIAMOND_BOOTS)
            }

            if (modifiers.contains(FightModifier.THORNS_STRATEGY)) {
                helmet.addUnsafeEnchantment(Enchantment.THORNS, 4)
                chestplate.addUnsafeEnchantment(Enchantment.THORNS, 4)
                leggings.addUnsafeEnchantment(Enchantment.THORNS, 4)
                boots.addUnsafeEnchantment(Enchantment.THORNS, 4)
            }

            sword = ItemStack(Material.IRON_SWORD)

            if (modifiers.contains(FightModifier.KNOCKBACK)) {
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2)
            }
        }
        if (modifiers.contains(FightModifier.DIAMOND_SWORD)) {
            sword = ItemStack(Material.DIAMOND_SWORD)
        }
        if (modifiers.contains(FightModifier.KNOCK_STICK)) {
            sword = ItemStack(Material.STICK)
                .rename("§4§lSuper Palito")
                .apply {
                    addUnsafeEnchantment(Enchantment.KNOCKBACK, 4)
                }
        }
        if (modifiers.contains(FightModifier.HAMBURGER_POWER)) { // sword = new ItemStackBuilder(Material.BREAD).buildMeta().withDisplayName("§6✪_§f䰛_§a§lSuper_Hamburger_da_Casa_do_João_§f䰛_§6✪").withLore("§7Ele voltou.").withEnchant(Enchantment.DAMAGE_ALL, 5, true).ItemStack().build();
            sword = ItemStack(Material.BREAD)
                .rename("§6✪ §a§lSuper Hamburger da Casa do João §6✪")
                .lore("§7Ele voltou.")
                .apply {
                    this.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5)
                }
        }
        p.removeAllPotionEffects()
        clearInventoryWithArmorOf(p)
        p.healAndFeed()

        if (modifiers.contains(FightModifier.GOLDEN_APPLES)) {
            p.inventory.addItem(golden)
        }

        if (modifiers.contains(FightModifier.BOW)) {
            p.inventory.addItem(
                ItemStack(Material.BOW)
                    .apply {
                        this.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
                        this.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1)
                    }
            )

            p.inventory.addItem(
                ItemStack(Material.ARROW)
            )
        }
        if (modifiers.contains(FightModifier.DAMAGE_POTION)) {
            p.inventory.addItem(
                ItemStack(Material.SPLASH_POTION).meta<PotionMeta> {
                    this.addCustomEffect(PotionEffect(PotionEffectType.HARM, 20 * 5, 0), true)
                }
            )
        }
        if (modifiers.contains(FightModifier.HEAL_POTION)) {
            p.inventory.addItem(
                ItemStack(Material.POTION).meta<PotionMeta> {
                    this.addCustomEffect(PotionEffect(PotionEffectType.HEAL, 20 * 15, 0), true)
                }
            )
        }
        p.inventory.helmet = helmet
        p.inventory.chestplate = chestplate
        p.inventory.leggings = leggings
        p.inventory.boots = boots
        p.inventory.addItem(sword)
        p.inventory.setItemInOffHand(ItemStack(Material.SHIELD))

        if (modifiers.contains(FightModifier.SUPER_SPEED))
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 1000000, 2))

        if (modifiers.contains(FightModifier.HIGH_JUMP))
            p.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 1000000, 2))

        if (modifiers.contains(FightModifier.SLOWNESS))
            p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 1000000, 2))

        if (modifiers.contains(FightModifier.ONE_HIT_KILL)) {
            p.health = 2.0
            p.foodLevel = 10
        }
    }

    fun hasFightEnded(): Boolean {
        if (players.size == 1) {
            val winner: Player? = players.firstOrNull()
            started = false
            preStart = false
            players.clear()
            p1 = null
            p2 = null
            p3 = null
            p4 = null

            if (winner != null) {
                clearInventoryWithArmorOf(winner)
                restoreInventoryOf(winner)
                winner.healAndFeed()
                winner.teleport(exit)
                m.server.broadcastMessage(
                    FancyAsriel.fancy(
                        DreamFight.prefix + "§eNós encontramos o nosso §4§lLutador" + (if (winner.girl) "a" else "") + "§e! §l" + winner.displayName + "§e §lvenceu o Evento Fight§e! §a+§l" + winnerPrize + "$§a e §cum pesadelo§a!"
                    )
                )
                winner.balance += winnerPrize
                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                    Cash.giveCash(winner, 1)
                }
                // VaultUtils.econ.depositPlayer(winner, winnerPrize);
                DreamFight.lastWinner = winner.name
                m.eventoFight.lastTime = System.currentTimeMillis()
                m.eventoFight.running = false
                DreamFight.lastFight = System.currentTimeMillis()
                // MettatonEX.stopMyEvent()
            }
            return true
        }
        return false
    }

    /**
     * Servidor está sendo desligado!
     */
    fun shutdownFight() {
        for (p in players) {
            clearInventoryWithArmorOf(p)
            restoreInventoryOf(p)
            p.removeAllPotionEffects()
            p.healAndFeed()
            p.teleport(exit)
        }
    }

    fun setWinner(p: Player?, wr: WinReason) {
        isPvPStarted = false
        val winner: Player? = p
        var loser: Player? = null
        loser = if (p1!!.equals(p)) {
            p2
        } else {
            p1
        }

        winner!!
        loser!!

        val finalPrize = prize * multiplier
        if (wr == WinReason.DEATH) {
            sendToFightArena("§e§l" + winner.displayName.toString() + "§e ganhou o PvP! §a+§l" + finalPrize.toString() + "$§a!")
            winner.balance += finalPrize
            // VaultUtils.econ.depositPlayer(winner, finalPrize);
        }
        if (wr == WinReason.DISCONNECT) {
            sendToFightArena("§e§l" + loser.displayName.toString() + "§e arregou o PvP! §a+§l" + finalPrize.toString() + "$§a!")
            winner.balance += finalPrize
            // VaultUtils.econ.depositPlayer(winner, finalPrize);
        }
        multiplier = multiplier + 1
        clearInventoryWithArmorOf(loser)
        clearInventoryWithArmorOf(winner)
        loser.removeAllPotionEffects()
        winner.removeAllPotionEffects()
        winner.healAndFeed()
        players.remove(loser)
        restoreInventoryOf(loser)
        InstantFirework.spawn(
            winner.location,
            FireworkEffect.builder()
                .with(FireworkEffect.Type.BURST)
                .withColor(Color.RED)
                .withFade(Color.BLACK)
                .build()
        )
        // Fireworks.playFirework(winner.getLocation(), Fireworks.randomFireworkEffect(), m);
        winner.teleport(lobby)
        loser.teleport(exit)
        if (hasFightEnded()) {
            return
        }
        preparePvP()
    }

    fun sendToFightArena(str: String) {
        for (p in lobby.world.players) {
            p.sendMessage(FancyAsriel.fancy(DreamFight.prefix + str))
        }
    }

    fun addToFight(p: Player) {
        if (!players.contains(p)) {
            players.add(p)
            p.teleport(lobby)
            storeInventoryOf(p)
            clearInventoryWithArmorOf(p)
            p.removeAllPotionEffects()
            p.healAndFeed()

            p.sendMessage(FancyAsriel.fancy(DreamFight.prefix + "§aVocê entrou no §lEvento Fight§a! Divirta-se!"))
            p.sendMessage(FancyAsriel.fancy(DreamFight.prefix + "§7Ainda o Evento Fight §lnão iniciou§7, mas ele irá iniciar em breve..."))
            p.sendMessage(FancyAsriel.fancy(DreamFight.prefix + "§7Após iniciar, espere até você ser chamado para ir no PvP! §lBoa Sorte§7!"))
        } else {
            p.sendMessage(FancyAsriel.fancy(DreamFight.prefix + "§cVocê §ljá está§c no Fight!"))
        }
    }

    fun clearInventoryWithArmorOf(player: Player?) {
        if (player != null) {
            player.openInventory.close()
            player.inventory.clear()
        }
    }

    fun storeInventoryOf(player: Player?) {
        if (player != null) {
            inventories[player] = player.inventory.contents.clone()
        }
    }

    fun restoreInventoryOf(player: Player?) {
        if (player != null) {
            val inventory = inventories[player] ?: return
            player.inventory.contents = inventory
        }
    }

    /**
     * Somente utilizado quando o modifier TWO_TEAM está ativo.
     */
    fun shouldEndPvP(died: Player): Boolean {
        players.remove(died)
        // Bukkit.broadcastMessage("1: " + p1.getName() + " Vivo? " + (players.contains(p1.getName()) ? "SIM" : "NÃO"));
// Bukkit.broadcastMessage("2: " + p2.getName() + " Vivo? " + (players.contains(p2.getName()) ? "SIM" : "NÃO"));
// Bukkit.broadcastMessage("3: " + p3.getName() + " Vivo? " + (players.contains(p3.getName()) ? "SIM" : "NÃO"));
// Bukkit.broadcastMessage("4: " + p4.getName() + " Vivo? " + (players.contains(p4.getName()) ? "SIM" : "NÃO"));
        clearInventoryWithArmorOf(died)
        died.removeAllPotionEffects()
        died.healAndFeed()
        restoreInventoryOf(died)
        died.teleport(exit)
        if (died === p1) {
            if (p2 != null && !players.contains(p2!!)) {
                return true
            }
        }
        if (died === p2) {
            if (p1 != null && !players.contains(p1!!)) {
                return true
            }
        }
        if (died === p3) {
            if (p4 != null && !players.contains(p4!!)) {
                return true
            }
        }
        if (died === p4) {
            if (p3 != null && !players.contains(p3!!)) {
                return true
            }
        }
        return false
    }

    /**
     * Somente utilizado quando o modifier TWO_TEAM está ativo.
     */
    fun setWinner() {
        var winner1: Player? = null
        var winner2: Player? = null
        val lost: ArrayList<Player?> = ArrayList<Player?>()
        if (p1 != null && players.contains(p1!!)) {
            winner1 = p1
        } else {
            lost.add(p1)
        }
        if (p2 != null && players.contains(p2!!)) {
            winner2 = p2
        } else {
            lost.add(p2)
        }
        if (p3 != null && players.contains(p3!!)) {
            winner1 = p3
        } else {
            lost.add(p3)
        }
        if (p4 != null && players.contains(p4!!)) {
            winner2 = p4
        } else {
            lost.add(p4)
        }
        val finalPrize = prize * multiplier
        var sent = false
        if (winner1 != null && winner2 != null && !sent) {
            sendToFightArena("§e§l" + winner1.displayName.toString() + "§e e §l" + winner2.displayName.toString() + " §eganharam o PvP! §a+§l" + finalPrize.toString() + "$§a!")
            sent = true
            // VaultUtils.econ.depositPlayer(winner1, finalPrize);
// VaultUtils.econ.depositPlayer(winner2, finalPrize);
        }
        if (winner1 != null && !sent) {
            sendToFightArena("§e§l" + winner1.displayName.toString() + "§e ganhou o PvP! §a+§l" + finalPrize.toString() + "$§a!")
            sent = true
            // VaultUtils.econ.depositPlayer(winner1, finalPrize);
        }
        if (winner2 != null && !sent) {
            sendToFightArena("§e§l" + winner2.displayName.toString() + "§e ganhou o PvP! §a+§l" + finalPrize.toString() + "$§a!")
            sent = true
            // VaultUtils.econ.depositPlayer(winner2, finalPrize);
        }
        multiplier = multiplier + 1
        if (winner1 != null) {
            clearInventoryWithArmorOf(winner1)
            winner1.removeAllPotionEffects()
            winner1.healAndFeed()
            // Fireworks.playFirework(winner1.getLocation(), Fireworks.randomFireworkEffect(), m);
            winner1.teleport(lobby)
        }
        if (winner2 != null) {
            clearInventoryWithArmorOf(winner2)
            winner2.removeAllPotionEffects()
            winner2.healAndFeed()
            // Fireworks.playFirework(winner2.getLocation(), Fireworks.randomFireworkEffect(), m);
            winner2.teleport(lobby)
        }
        for (loser in lost) {
            if (loser != null) {
                clearInventoryWithArmorOf(loser)
                loser.removeAllPotionEffects()
                loser.healAndFeed()
                players.remove(loser)
                restoreInventoryOf(loser)
                loser.teleport(exit)
            }
        }
        if (hasFightEnded()) {
            return
        }
        preparePvP()
    }

}