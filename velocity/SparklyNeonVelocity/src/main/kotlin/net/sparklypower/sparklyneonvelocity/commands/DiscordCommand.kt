package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.Ban
import net.sparklypower.sparklyneonvelocity.dao.DiscordAccount
import net.sparklypower.sparklyneonvelocity.dao.Warn
import net.sparklypower.sparklyneonvelocity.tables.Bans
import net.sparklypower.sparklyneonvelocity.tables.DiscordAccounts
import net.sparklypower.sparklyneonvelocity.tables.Warns
import net.sparklypower.sparklyneonvelocity.utils.prettyBoolean
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.util.*

class DiscordCommand(private val m: SparklyNeonVelocity) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val player = invocation.source() as Player
        val arg0 = invocation.arguments().getOrNull(0)
        if (arg0 == "registrar" || arg0 == "register") {
            val account = m.pudding.transactionBlocking {
                DiscordAccount.find { DiscordAccounts.minecraftId eq player.uniqueId }
                    .firstOrNull()
            }

            if (account == null) {
                invocation.source().sendMessage("§cVocê não tem nenhum registro pendente! Use \"-registrar ${player.username}\" no nosso servidor no Discord para registrar a sua conta!".fromLegacySectionToTextComponent())
                return
            }

            m.pudding.transactionBlocking {
                account.isConnected = true

                DiscordAccounts.deleteWhere {
                    DiscordAccounts.minecraftId eq player.uniqueId and (DiscordAccounts.id neq account.id)
                }
            }

            player.sendMessage("§aConta do Discord foi registrada com sucesso, yay!".fromLegacySectionToTextComponent())

            m.discordAccountAssociationsWebhook.send("Conta **`${player.username}`** (`${player.uniqueId}`) foi associada a conta `${account.discordId}` (<@${account.discordId}>)")
            return
        }

        invocation.source().sendMessage("§dNosso Discord! https://discord.gg/JYN6g2s".fromLegacySectionToTextComponent())
    }
}