package net.perfectdreams.dreamreparar.listeners

import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamreparar.DreamReparar
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*

class SignListener(val m: DreamReparar) : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onRightClick(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return

        if (e.rightClick && clickedBlock.type.name.contains("SIGN")) {
            val sign = clickedBlock.state as Sign

            val l = sign.location
            val p = e.player

            if (sign.getLine(0) == "§1[Reparar]") {
                e.isCancelled = true
                if (p.itemInHand.type == Material.AIR) {
                    p.sendMessage("§8[§d§lReparar§8] §cVoc\u00ea §4n\u00e3o pode reparar§c a sua m\u00e3o, bobinho :3")
                    p.world.spawnParticle(Particle.VILLAGER_ANGRY, l, 100, 1.5, 1.0, 1.0)
                    return
                }

                val itemReparar = p.inventory.itemInMainHand

                if (itemReparar.type != Material.AIR) {
                    val meta = itemReparar.itemMeta

                    if (meta is Damageable) {
                        val durability = meta.damage

                        if (!m.canRepair(itemReparar)) {
                            p.sendMessage("§8[§d§lReparar§8] §cVocê §4não pode§c reparar isto!")
                            p.world.spawnParticle(Particle.VILLAGER_ANGRY, l, 100, 1.5, 1.0, 1.0)
                            return
                        }

                        if (durability == 0) {
                            p.sendMessage("§8[§d§lReparar§8] §cVoc\u00ea §4n\u00e3o precisa§c reparar isto!")
                            p.world.spawnParticle(Particle.VILLAGER_ANGRY, l, 100, 1.5, 1.0, 1.0)
                            return
                        }

                        val preco = m.calculatePrice(itemReparar, p)
                        if (preco <= p.balance) {
                            p.balance -= preco.toDouble()

                            meta.damage = 0
                            itemReparar.itemMeta = meta

                            p.sendMessage("§8[§d§lReparar§8] §aItem Reparado!")
                            val lookingAt = e.player.getTargetBlock(null as HashSet<Material>?, 5)
                            if (lookingAt.type.name.contains("SIGN") && lookingAt is Sign) {
                                val lines = (lookingAt as Sign).lines
                                lines[1] = "Obrigado por"
                                lines[2] = "reparar!"
                                lines[3] = ""
                                p.sendSignChange(sign.location, lines)
                                p.world.spawnParticle(Particle.VILLAGER_HAPPY, l, 100, 1.5, 1.0, 1.0)
                            }
                            return
                        }

                        p.sendMessage("§8[§d§lReparar§8] §cVoc\u00ea n\u00e3o tem §4dinheiro suficiente§c para fazer isto!")
                        p.world.spawnParticle(Particle.VILLAGER_ANGRY, l, 100, 1.5, 1.0, 1.0)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onSignChange(e: SignChangeEvent) {
        if (e.getLine(0) == "[Reparar]") {
            if (e.player.hasPermission("dreamreparar.placa")) {
                e.setLine(0, "§1[Reparar]")
                e.player.sendMessage("§aPlaca de Repara\u00e7\u00e3o criada com sucesso!")
            } else {
                e.player.sendMessage("§cVocê precisa ser VIP++ para poder criar a sua própria placa de reparação!")
                e.block.type = Material.AIR
                e.block.world.dropItemNaturally(e.block.location, ItemStack(Material.OAK_SIGN, 1))
            }
        }
    }
}