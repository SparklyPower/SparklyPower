package net.sparklypower.sparklyneonvelocity.commands

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.salomonbrys.kotson.jsonObject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.common.utils.toLegacySection
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import net.sparklypower.sparklyneonvelocity.utils.DreamNetwork
import net.sparklypower.sparklyneonvelocity.utils.StaffColors
import net.sparklypower.sparklyneonvelocity.utils.emotes
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import kotlin.jvm.optionals.getOrNull

class AdminChatCommand(val m: SparklyNeonVelocity, val server: ProxyServer) : SimpleCommand {
    companion object {
        val adminChatColor = NamedTextColor.AQUA
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source() as Player
        val arguments = invocation.arguments()
        val arg0 = arguments.getOrNull(0)
        val args = arguments.joinToString(" ")

        if (args.isEmpty()) {
            sender.sendMessage("§cVocê não pode enviar uma mensagem vazia.".fromLegacySectionToTextComponent())
        } else if (arg0 == "lock") {
            val isLocked = m.lockedAdminChat.contains(sender.uniqueId)
            if (isLocked) {
                m.lockedAdminChat.remove(sender.uniqueId)
            } else {
                m.lockedAdminChat.add(sender.uniqueId)
            }

            sender.sendMessage("§x§8§3§9§E§F§7Seu chat foi ${if (isLocked) "des" else ""}travado com sucesso.".fromLegacySectionToTextComponent())
        } else {
            m.broadcastAdminChatMessage(sender, args)
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.adminchat")
}