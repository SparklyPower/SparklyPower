package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.utils.ASNManager
import kotlin.jvm.optionals.getOrNull

class CheckAndKickCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        invocation.source().sendMessage("§aVerificando players online que deveriam estar banidos... (IP ban ou ASN ban)".fromLegacySectionToTextComponent())

        for (player in server.allPlayers) {
            val result = runBlocking { m.asnManager.isAsnBlacklisted(player.remoteAddress.hostString) }

            if (result.blocked) {
                invocation.source().sendMessage("§eExpulsando ${player.username} pois o ASN está bloqueado!".fromLegacySectionToTextComponent())
                player.disconnect("""§cSeu IP está bloqueado, desative VPNs ou proxies ativos para poder jogar!"""".trimMargin().fromLegacySectionToTextComponent())
            }
        }

        invocation.source().sendMessage("§aVerificação concluída!".fromLegacySectionToTextComponent())
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.checkandkick")
}