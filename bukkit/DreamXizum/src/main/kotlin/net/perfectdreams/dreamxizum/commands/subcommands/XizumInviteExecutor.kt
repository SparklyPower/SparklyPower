package net.perfectdreams.dreamxizum.commands.subcommands

import com.gmail.nossr50.api.PartyAPI
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.canPay
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.Mode
import net.perfectdreams.dreamcore.utils.extensions.hasEnchantments
import net.perfectdreams.dreamcore.utils.extensions.humanize
import net.perfectdreams.dreamcore.utils.extensions.toTextComponent
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.BattleHolograms
import net.perfectdreams.dreamxizum.battle.BattleItems
import net.perfectdreams.dreamxizum.extensions.requireAuthoringBattle
import net.perfectdreams.dreamxizum.extensions.requireAvailablePlayer
import net.perfectdreams.dreamxizum.extensions.requireDifferentPlayers
import net.perfectdreams.dreamxizum.extensions.requireUnvanishedPlayer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

@Suppress("DEPRECATION")
class XizumInviteExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(XizumInviteExecutor::class) {
        object Options : CommandOptions() {
            val invitee = player("player").register()
        }

        override val options = Options

        private val COLOR = ChatColor.of(Color(0x9CEA64))
        private val HIGHLIGHT = ChatColor.of(Color(0x22DD49))

        private val WEAPON_COLOR = ChatColor.of(Color(0x88E2F1))
        private val WEAPON_HIGHLIGHT = ChatColor.of(Color(0x22AFC6))

        private val ARMOR_COLOR = ChatColor.of(Color(0xF582BC))
        private val ARMOR_HIGHLIGHT = ChatColor.of(Color(0xF14C9F))

        private val EXTRA_COLOR = ChatColor.of(Color(0xF1A00E))
        private val EXTRA_HIGHLIGHT = ChatColor.of(Color(0xF2C25F))

        private val INFO_COLOR = ChatColor.of(Color(0x9ABD42))
        private val INFO_HIGHLIGHT = ChatColor.of(Color(0x8DAD3C))

        private val MONEY_COLOR = ChatColor.of(Color(0x65DC23))
        private val PARTY_COLOR = ChatColor.of(Color(0x9AEEDE))

        private val formatter = NumberFormat.getNumberInstance(Locale.GERMAN) as DecimalFormat

        private fun translateMaterial(material: Material) = with (material.toString()) {
            when {
                contains("IRON") -> "ferro"
                contains("DIAMOND") -> "diamante"
                contains("NETHERITE") -> "netherite"
                else -> "???"
            }
        }

        private fun commaOrAnd(item: ItemStack, list: List<ItemStack>) = with (list) {
            when {
                item == last() -> " e "
                indexOf(item) == 1 -> " acompanhado de "
                else -> ", "
            }
        }
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        plugin.schedule {
            val player = context.requirePlayer()
            val battle = context.requireAuthoringBattle(player)
            val invitee = args.getAndValidate(options.invitee)
            context.requireDifferentPlayers(player, invitee)
            context.requireUnvanishedPlayer(invitee, "${player.name} tentou te convidar para um xizum.")
            context.requireAvailablePlayer(invitee)

            if (battle.isFull) context.fail("§cNão tem espaço para convidar jogadores.")

            switchContext(SynchronizationContext.ASYNC)

            val inviteeCash = Cash.getCash(invitee)

            switchContext(SynchronizationContext.SYNC)

            val sonecas = battle.options.sonecas
            val cash = battle.options.cash

            if (!invitee.canPay(sonecas)) return@schedule player.sendMessage("§c${invitee.name} não tem sonecas suficientes para cobrir a aposta.")
            if (inviteeCash < cash) return@schedule player.sendMessage("§c${invitee.name} não tem pesadelos suficientes para cobrir a aposta.")

            battle.invite(invitee)

            switchContext(SynchronizationContext.ASYNC)

            val message = ComponentBuilder("[").color(ChatColor.DARK_GRAY)
                .append("X1").color(ChatColor.of(Color(0x2E90D1))).bold(true)
                .append("] ").color(ChatColor.DARK_GRAY).bold(false)
                .append(player.name).color(HIGHLIGHT)
                .append(" te convidou para uma partida ").color(COLOR)
                .append("${battle.limit/2}v${battle.limit/2}").color(HIGHLIGHT)
                .append(".").color(COLOR)

            if (battle.options.itemsType == BattleItems.PLAYER_ITEMS)
                message.append("\n➼ Atenção: você deve levar seus próprios itens (você não os perderá caso morra).").color(ChatColor.of(Color(0xF9FB7E)))

            val items = battle.options.items
            val armor = battle.options.armor

            val weapon = items.firstOrNull { it.type.toString().contains("SWORD") || it.type.toString().contains("AXE") }
            val bow = items.firstOrNull { it.type == Material.BOW }
            val crossbow = items.firstOrNull { it.type == Material.CROSSBOW }
            val trident = items.firstOrNull { it.type == Material.TRIDENT }
            val shield = items.firstOrNull { it.type == Material.SHIELD }

            var numberOfDefensiveItems = (shield?.let { 1 } ?: 0) + if (armor.isNotEmpty()) 1 else 0
            var weapons = listOfNotNull(weapon, bow, crossbow, trident)
            var mainWeapon = weapons.firstOrNull()

            weapon?.let {
                var indefiniteArticle: String
                var definiteArticle: String
                var item: String

                if (weapon.type.toString().contains("SWORD")) {
                    indefiniteArticle = "uma"
                    definiteArticle = "a"
                    item = "espada de ${translateMaterial(it.type)}"
                } else {
                    indefiniteArticle = "um"
                    definiteArticle = "o"
                    item = "machado de ${translateMaterial(it.type)}"
                }

                message.append("\n➼ Você empunhará $indefiniteArticle ").color(WEAPON_COLOR)
                    .append(item + if (it.hasEnchantments()) " encantad${definiteArticle}" else "")
                    .color(WEAPON_HIGHLIGHT).event(HoverEvent(Action.SHOW_ITEM, it.toTextComponent()))
            }

            bow?.let {
                message.append(if (mainWeapon == it) "\n➼ Afie sua mira, pois " else commaOrAnd(it, weapons))
                    .reset().color(WEAPON_COLOR).append("um ").append("arco" + if (it.hasEnchantments()) " encantado" else "")
                    .color(WEAPON_HIGHLIGHT).event(HoverEvent(Action.SHOW_ITEM, it.toTextComponent()))
            }

            crossbow?.let {
                message.append(if (mainWeapon == it) "\n➼ Tem mira boa? Sua arma principal será " else commaOrAnd(it, weapons))
                    .reset().color(WEAPON_COLOR).color(WEAPON_COLOR).append("uma ").append("besta" + if (it.hasEnchantments()) " encantada" else "")
                    .color(WEAPON_HIGHLIGHT).event(HoverEvent(Action.SHOW_ITEM, it.toTextComponent()))
            }

            trident?.let {
                message.append(if (mainWeapon == it) "\n➼ Espete seus inimigos com " else commaOrAnd(it, weapons))
                    .reset().color(WEAPON_COLOR).append("um ").append("tridente" + if (it.hasEnchantments()) " encantado" else "")
                    .color(WEAPON_HIGHLIGHT).event(HoverEvent(Action.SHOW_ITEM, it.toTextComponent()))
            }

            if (weapons.isNotEmpty()) message.append(".").reset().color(WEAPON_COLOR)

            if (numberOfDefensiveItems > 0) {
                message.append("\n➼ " + if (weapons.isNotEmpty()) "Para sua proteção, você contará com " else "Sem poder ofensivo à sua disposição, apenas ")
                    .reset().color(ARMOR_COLOR)
                if (armor.isNotEmpty()) {
                    val chestplate = armor.first { it.type in setOf(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE) }
                    message.append("uma armadura de ${translateMaterial(chestplate.type)}" + if (chestplate.hasEnchantments()) " encantada" else "")
                        .reset().color(ARMOR_HIGHLIGHT).event(HoverEvent(Action.SHOW_ITEM, chestplate.toTextComponent()))
                }
                shield?.let {
                    message.append((if (numberOfDefensiveItems > 1) " e " else " ") + "um escudo" + if (it.hasEnchantments()) " encantado" else "")
                        .reset().color(ARMOR_HIGHLIGHT).event(HoverEvent(Action.SHOW_ITEM, it.toTextComponent()))
                }
                message.append(".").reset().color(ARMOR_COLOR)
            }

            if (weapons.isEmpty() && numberOfDefensiveItems == 0 && battle.options.itemsType != BattleItems.PLAYER_ITEMS)
                message.append("\n➼ Sem itens ofensivos ou defensivos. Praticamente um sumô.").reset().color(ChatColor.of(Color(0xE883FC)))

            message.append("\n➼ Nota: mcMMO está ").color(EXTRA_COLOR)
                .append(battle.options.allowMcMMO.humanize(Mode.ACTIVATED)).underlined(true).color(EXTRA_HIGHLIGHT)
                .append(" e ao morrer você ").underlined(false).color(EXTRA_COLOR)
                .append(battle.options.dropHeads.humanize(Mode.NO_OR_NOTHING, true) + "dropará").underlined(true).color(EXTRA_HIGHLIGHT)
                .append(" sua cabeça.").underlined(false).color(EXTRA_COLOR)

            message.append("\n➼ A duração da partida é de ").color(INFO_COLOR)
                .append("${battle.options.timeLimit} minutos").underlined(true).color(INFO_HIGHLIGHT)
                .append(" e a versão do pvp é ").underlined(false).color(INFO_COLOR)
                .append(if (battle.options.legacyPvp) "1.8" else "1.16+").underlined(true).color(INFO_HIGHLIGHT)
                .append(".").underlined(false).color(INFO_COLOR)

            if (sonecas + cash > 0) {
                message.append("\n➼ O valor da aposta é de ").color(MONEY_COLOR)
                if (sonecas > 0) message.append(formatter.format(sonecas)).underlined(true).append(" sonecas").underlined(false)
                if (cash > 0) {
                    if (sonecas > 0) message.append(" e")
                    message.append(formatter.format(cash)).underlined(true).append(" pesadelos").underlined(false)
                }
                message.append(".")
            }

            if (PartyAPI.inParty(invitee))
                message.append("\n➼ Você está em uma party do mcMMO. Caso aceite, você será removido dela automaticamente.").color(PARTY_COLOR)

            message.append("\n· ✧ ").color(ChatColor.YELLOW)
                .append("[Aceitar]").color(ChatColor.of(Color(0x2FD024))).bold(true)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xizum aceitar ${player.name}"))
                .append(" · ").reset().color(ChatColor.YELLOW)
                .append("[Recusar]").color(ChatColor.of(Color(0xDA252F))).bold(true)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xizum recusar ${player.name}"))
                .append(" ✧ ·").reset().color(ChatColor.YELLOW)

            switchContext(SynchronizationContext.SYNC)

            BattleHolograms.updateHolograms(battle)
            invitee.sendMessage(*message.create())
            player.sendMessage("${DreamXizum.PREFIX} ${highlight(invitee.name)} foi convidado com sucesso.")
        }
    }
}