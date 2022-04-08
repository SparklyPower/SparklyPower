package net.perfectdreams.dreamxizum.gui.pages.kits

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.DreamNPC
import net.perfectdreams.dreamcore.utils.TextUtils.convertToNumeroNomeAdjetivo
import net.perfectdreams.dreamcore.utils.WrapperHologram
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.stripColors
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.dao.Kit
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.FinalPage
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class PlayerKitsPage(private val toSaveKit: Boolean, kits: MutableList<Kit>, val player: Player) : AbstractPage() {
    companion object : Listener {
        val namingKit = mutablePlayerMapOf<(String) -> Unit>()

        @EventHandler
        fun onMessage(event: AsyncPlayerChatEvent) =
            with (event) {
                if (player !in namingKit) return@with
                isCancelled = true
                message.let {
                    if (it.length > 12) return player.sendMessage("§cO nome do kit não pode ter mais que 12 letras.")
                    with (namingKit.remove(player)!!) { invoke(it) }
                }
            }

        init { DreamXizum.INSTANCE.registerEvents(this) }
    }

    private val takenModels = with (XizumConfig.models.takenSkins) { listOf(kit1, kit2, kit3, kit4) }
    private val colors = listOf("§x§a§a§f§f§0§0§l", "§x§4§2§f§f§b§a§l", "§x§c§5§9§f§f§9§l", "§x§f§b§a§b§5§1§l")
    private val lines = if (toSaveKit) mutableListOf("§x§f§f§6§6§0§0§lClique em um slot para salvar", "§x§f§f§6§6§0§0§lseu kit atual")
        else mutableListOf("§x§1§e§c§b§e§1§lClique em um slot para carregar", "§x§1§e§c§b§e§1§lo kit selecionado")

    init {
        hologram = WrapperHologram(XizumConfig.models.locations.pageHologram.toBukkitLocation(), lines)
        with (npcs) { listOf(kit1, kit2, kit3, kit4) }.forEachIndexed { index, model ->
            lateinit var dreamNPC: DreamNPC
            var currentKit = kits.firstOrNull { it.slot == index }
            val availableColor = "§x§d§f§e§d§f§d§l"
            val color = currentKit?.let { colors[index] } ?: availableColor
            val name = currentKit?.let { "\"${it.name ?: "sem nome"}\"" } ?: "Disponível"
            var lastClick = 0L

            namedButton(model, "$color${(index + 1).convertToNumeroNomeAdjetivo()} kit\n" + color + name) {
                val options = it.battle!!.options

                if (toSaveKit) {
                    kits.firstOrNull { kit -> kit.hash == options.hashCode()}?.let { kit ->
                        val message = if (index == kit.slot) "§cEsse kit já está salvo nesse slot."
                            else "§cSeu ${(kit.slot + 1).convertToNumeroNomeAdjetivo()!!.lowercase()} kit é idêntico a esse."
                        return@namedButton it.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND, message)
                    }

                    currentKit?.let { _ ->
                        System.currentTimeMillis().apply {
                            if (this - lastClick > 6500) {
                                lastClick = this
                                return@namedButton it.playSoundAndSendMessage(Sound.ENTITY_ITEM_PICKUP,
                                    "§cClique novamente para confirmar a sobreposição do kit atual.")
                            }
                        }
                    }

                    lateinit var newKit: Kit

                    plugin.schedule(SynchronizationContext.ASYNC) {
                        newKit = Kit.createKit(it.uniqueId, options, index)
                        currentKit?.deleteKit()

                        switchContext(SynchronizationContext.SYNC)

                        with (takenModels[index]) { dreamNPC.updateSkin(texture, signature) }
                        dreamNPC.lines = mutableListOf(colors[index] + dreamNPC.lines[0].stripColors(), "${colors[index]}\"\"")
                        dreamNPC.equipment.apply {
                            clear()
                            addAll(options.items + options.armor)
                            dreamNPC.buildEquipmentPacket()
                        }

                        it.spawnParticle(Particle.VILLAGER_HAPPY, dreamNPC.location, 30, -.45, 1.0, .75)

                        kits.add(newKit)
                        currentKit = null

                        namingKit[it] = { name ->
                            plugin.schedule(SynchronizationContext.ASYNC) {
                                newKit.rename(name)
                                switchContext(SynchronizationContext.SYNC)
                                it.playSound(it.location, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 10F, 1F)
                                dreamNPC.updateLine(1, "${colors[index]}\"$name\"")
                                hologram!!.lines = lines
                            }
                        }

                        hologram!!.lines = mutableListOf("§e§lDigite no chat o nome", "§e§ldesse kit")
                    }
                }

                else {
                    if (it.isSneaking) {
                        System.currentTimeMillis().apply {
                            currentKit?.let { _ ->
                                if (this - lastClick > 6500) {
                                    lastClick = this
                                    return@namedButton it.playSoundAndSendMessage(Sound.ENTITY_ITEM_PICKUP,
                                        "§cClique novamente para confirmar a exclusão do kit atual.")
                                }
                            }
                        }

                        currentKit?.let { kit ->
                            currentKit = null
                            plugin.schedule(SynchronizationContext.ASYNC) {
                                kit.deleteKit()
                                switchContext(SynchronizationContext.SYNC)
                                with (dreamNPC) {
                                    clearItems()
                                    lines = mutableListOf(availableColor + dreamNPC.lines[0].stripColors(), "${availableColor}Disponível")
                                    with (model.skin) { updateSkin(texture, signature) }
                                }
                                it.spawnParticle(Particle.VILLAGER_HAPPY, dreamNPC.location, 30, -.45, 1.0, .75)
                                it.playSoundAndSendMessage(Sound.UI_LOOM_TAKE_RESULT, "${DreamXizum.PREFIX} Você excluiu o kit com sucesso.")
                            }
                        } ?: return@namedButton it.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND, "§cNão tem como excluir um kit inexistente.")
                    }

                    currentKit?.let {
                        kit -> options.loadKit(kit)
                        Paginator.fetch(it).addAndShowPage(FinalPage(it))
                    } ?: it.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND, "§cVocê não salvou nenhum kit nesse slot.")
                }
            }.also { button ->
                button.sound = null
                with (button.npc) {
                    dreamNPC = button.npc
                    currentKit?.let {
                        equipment = it.buildArmor()
                        it.buildItems().firstOrNull()?.let { item -> changeItem(item) }
                        with (takenModels[index]) { updateSkin(texture, signature) }
                    }
                }
            }
        }
    }

    override fun onBack() {
        namingKit.remove(player)
        if (!toSaveKit) player.battle!!.options.clearItems()
    }
}