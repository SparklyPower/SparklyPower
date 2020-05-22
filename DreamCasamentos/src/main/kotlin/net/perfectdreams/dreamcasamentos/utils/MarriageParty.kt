package net.perfectdreams.dreamcasamentos.utils

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcasamentos.DreamCasamentos
import net.perfectdreams.dreamcasamentos.dao.Marriage
import net.perfectdreams.dreamcore.utils.InstantFirework
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.apache.commons.io.FileUtils.waitFor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

object MarriageParty {
    lateinit var INSTANCE: DreamCasamentos

    fun getCasalByPlayer(player: Player): Marriage? {
        return INSTANCE.getMarriageFor(player)
    }

    fun startMarriageParty(casal: Marriage, player1: Player, player2: Player) {
        val loc1Wrapper = INSTANCE.config.loc1!!
        val loc2Wrapper = INSTANCE.config.loc2!!

        val loc1 = Location(Bukkit.getWorld(loc1Wrapper.world), loc1Wrapper.x, loc1Wrapper.y, loc1Wrapper.z, loc1Wrapper.yaw, loc1Wrapper.pitch)
        val loc2 = Location(Bukkit.getWorld(loc2Wrapper.world), loc2Wrapper.x, loc2Wrapper.y, loc2Wrapper.z, loc2Wrapper.yaw, loc2Wrapper.pitch)

        player1.teleport(loc1)
        player2.teleport(loc2)

        giveMarriageArmor(player1)
        giveMarriageArmor(player2)

        playFireworkEffects(player1)
        playFireworkEffects(player2)
    }

    private fun playFireworkEffects(player: Player) {
        val scheduler = Bukkit.getScheduler()

        val fireworkEffect = FireworkEffect.builder()
                .flicker(true)
                .with(FireworkEffect.Type.STAR)
                .withColor(Color.WHITE)
                .withFade(Color.GRAY)
                .build()

        scheduler.schedule(INSTANCE) {
            for (i in 0 until 5) {
                if (!player.isOnline) {
                    return@schedule
                }
                InstantFirework.spawn(player.location, fireworkEffect)
                waitFor(60)
            }
        }
    }

    private fun giveMarriageArmor(player: Player) {
        if (player.girl) {
            val helmet = ItemStack(Material.LEATHER_HELMET)
            (helmet.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(255, 255, 255))
                helmet.itemMeta = this
            }
            replaceArmorAndStore(player, helmet, EquipmentSlot.HEAD)

            val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            (chestplate.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(255, 255, 255))
                chestplate.itemMeta = this
            }
            replaceArmorAndStore(player, chestplate, EquipmentSlot.CHEST)

            val leggings = ItemStack(Material.LEATHER_LEGGINGS)
            (leggings.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(255, 255, 255))
                leggings.itemMeta = this
            }
            replaceArmorAndStore(player, leggings, EquipmentSlot.LEGS)

            val boots = ItemStack(Material.LEATHER_BOOTS)
            (boots.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(255, 255, 255))
                boots.itemMeta = this
            }
            replaceArmorAndStore(player, boots, EquipmentSlot.FEET)
        } else {
            val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            (chestplate.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(0, 0, 0))
                chestplate.itemMeta = this
            }
            replaceArmorAndStore(player, chestplate, EquipmentSlot.CHEST)

            val leggings = ItemStack(Material.LEATHER_LEGGINGS)
            (leggings.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(0, 0, 0))
                leggings.itemMeta = this
            }
            replaceArmorAndStore(player, leggings, EquipmentSlot.LEGS)

            val boots = ItemStack(Material.LEATHER_BOOTS)
            (boots.itemMeta as LeatherArmorMeta).apply {
                this.setColor(Color.fromRGB(0, 0, 0))
                boots.itemMeta = this
            }
            replaceArmorAndStore(player, boots, EquipmentSlot.FEET)
        }
    }

    private fun replaceArmorAndStore(player: Player, item: ItemStack, slot: EquipmentSlot) {
        when (slot) {
            EquipmentSlot.HEAD -> {
                if (player.inventory.canHoldItem(item)) {
                    if (player.inventory.helmet != null)
                        player.inventory.addItem(player.inventory.helmet!!)
                    player.inventory.helmet = item
                }
            }
            EquipmentSlot.CHEST -> {
                if (player.inventory.canHoldItem(item)) {
                    if (player.inventory.chestplate != null)
                        player.inventory.addItem(player.inventory.chestplate!!)
                    player.inventory.chestplate = item
                }
            }
            EquipmentSlot.LEGS -> {
                if (player.inventory.canHoldItem(item)) {
                    if (player.inventory.leggings != null)
                        player.inventory.addItem(player.inventory.leggings!!)
                    player.inventory.leggings = item
                }
            }
            EquipmentSlot.FEET -> {
                if (player.inventory.canHoldItem(item)) {
                    if (player.inventory.boots != null)
                        player.inventory.addItem(player.inventory.boots!!)
                    player.inventory.boots = item
                }
            }
        }

    }
}