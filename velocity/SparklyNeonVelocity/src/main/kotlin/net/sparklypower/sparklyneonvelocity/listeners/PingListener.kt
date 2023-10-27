package net.sparklypower.sparklyneonvelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.sparklypower.common.utils.TextUtils
import net.sparklypower.common.utils.toLegacySection
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.*

class PingListener(private val m: SparklyNeonVelocity, private val server: ProxyServer) {
    companion object {
        private val COLOR_LOGO_RED = TextColor.color(237, 46, 22)
        private val COLOR_LOGO_AQUA = TextColor.color(1, 235, 247)
    }

    @Subscribe
    fun onPing(event: ProxyPingEvent) {
        m.logger.info { "Pinged by ${event.connection.remoteAddress.hostString}, adding to the pinged by addresses list..." }
        m.pingedByAddresses.add(event.connection.remoteAddress.hostString)

        val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))
        val currentDayOfTheWeek = now.dayOfWeek
        val year = now.year

        // val version = ProtocolSupportAPI.getProtocolVersion(e.connection.address)
        val online = server.playerCount
        val max = year

        val builder = event.ping.asBuilder()
            .onlinePlayers(online)
            .maximumPlayers(max)

        val top: String
        val bottom: String

        if (m.isMaintenance) {
            builder.favicon(m.favicons["pantufa_zz"])

            top = TextUtils.getCenteredMessage("§cSparklyPower está em manutenção!", 128)

            bottom = TextUtils.getCenteredMessage(
                "§cVolte mais tarde!",
                128
            )
        } else {
            builder.favicon(m.favicons["pantufa_point_allouette"])

            top = TextUtils.getCenteredMessage("§a\u266b §6(\uff89\u25d5\u30ee\u25d5)\uff89 §e* :\uff65\uff9f\u2727 ${COLOR_LOGO_RED.toLegacySection()}§lSparkly${COLOR_LOGO_AQUA.toLegacySection()}§lPower §e\u2727\uff9f\uff65: *§6\u30fd(\u25d5\u30ee\u25d5\u30fd) §a\u266b", 128)


            bottom = if (currentDayOfTheWeek == DayOfWeek.FRIDAY) {
                // The colored part is "este servidor é incrível!
                TextUtils.getCenteredMessage(
                    "§5§l»§d§l» §x§d§5§d§6§1§0HOJE É SEXTA CAMBADA! VAMOS ANIMAR!!! §d§l«§5§l«",
                    128
                )
            } else {
                // The colored part is "este servidor é incrível!
                TextUtils.getCenteredMessage(
                    "§5§l»§d§l» §fModéstia à parte, §x§f§f§8§0§8§0e§x§f§f§9§f§8§0s§x§f§f§b§f§8§0t§x§f§f§d§f§8§0e§x§f§f§f§f§8§0 §x§d§f§f§f§8§0s§x§b§f§f§f§8§0e§x§9§f§f§f§8§0r§x§8§0§f§f§8§0v§x§8§0§f§f§9§fi§x§8§0§f§f§b§fd§x§8§0§f§f§d§fo§x§8§0§f§f§f§fr§x§8§0§d§f§f§f §x§8§0§b§f§f§fé§x§8§0§9§f§f§f §x§8§0§8§0§f§fi§x§9§f§8§0§f§fn§x§b§f§8§0§f§fc§x§d§f§8§0§f§fr§x§f§f§8§0§f§fí§x§f§f§8§0§d§fv§x§f§f§8§0§b§fe§x§f§f§8§0§9§fl§f! §d§l«§5§l«",
                    128
                )
            }
        }

        // val bottom = TextUtils.getCenteredMessage("§5§l»§d§l» §fQuer coisas da §c§l1.16§f? Então entre! §c^-^ §d§l«§5§l«", 128)
        builder.description(LegacyComponentSerializer.legacySection().deserialize("$top\n$bottom"))

        builder.samplePlayers(
            createPlayerListMessage("§b✦§3§m                    §8[§4§lSparkly§b§lPower§8]§3§m                    §b✦"),
            createPlayerListMessage("§6✧ §fModéstia a parte, este servidor é §a§lincrível§f! §6✧"),
            createPlayerListMessage(""),
            createPlayerListMessage("§b✦ §6$online§e Players Online! §b✦"),
            createPlayerListMessage("§b✦ §eUm §6Survival§e que você §6jamais§e viu antes! §b✦"),
            createPlayerListMessage("§b✦ §eServidor da §6Loritta Morenitta§e! §b✦"),
            createPlayerListMessage("§b✦ §eItens §6personalizados§e! §b✦"),
            createPlayerListMessage("§b✦ §eSem §6Lag§e! (as vezes né) §b✦"),
            createPlayerListMessage("§b✦ §eDesde §62014§e divertindo nossos jogadores! §b✦"),
            createPlayerListMessage("§b✦ §ee §6§lmuito§e mais! §b✦"),
            createPlayerListMessage(""),
            createPlayerListMessage("§5✸ §eentre agora... §5✸"),
            createPlayerListMessage("§d✸ §eporque só falta você! §c✌ §d✸"),
        )

        event.ping = builder.build()
    }

    private fun createPlayerListMessage(text: String) = ServerPing.SamplePlayer(
        TextUtils.getCenteredMessage(
            text,
            128
        ),
        UUID.randomUUID()
    )
}