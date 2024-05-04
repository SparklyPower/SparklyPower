package net.sparklypower.sparklyneonvelocity

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.sparklypower.common.utils.DateUtils
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.tables.Users
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.jvm.optionals.getOrNull

class PunishmentManager(private val m: SparklyNeonVelocity, val server: ProxyServer) {
    companion object {
        // 30 dias
        const val WARN_EXPIRATION = 2_592_000_000L

        // 14 dias
        const val DEFAULT_IPBAN_EXPIRATION = 1_209_600_000L
    }

    fun getUniqueId(playerName: String): UUID {
        // Because we support premium users, we will check it using our user cache
        // TODO: Should we really use runBlocking here?
        return runBlocking {
            m.pudding.transaction {
                User.find { Users.username eq playerName }
                    .firstOrNull()
                    ?.id
                    ?.value
            }
        } ?: UUID.nameUUIDFromBytes("OfflinePlayer:$playerName".toByteArray(Charsets.UTF_8)) // If not, we will fall back to the OfflinePlayer
    }

    suspend fun getUserNameByUniqueId(uniqueId: UUID): String? {
        return m.pudding.transaction {
            User.findById(uniqueId)?.username
        }
    }

    /**
     * Sends the punishment to Discord
     */
    fun sendPunishmentToDiscord(silent: Boolean, punishedDisplayName: String, punishedUniqueId: UUID?, title: String, punisherDisplayName: String, effectiveReason: String? = null, server: String?, time: Long? = null) {
        if (silent)
            return

        val embed = WebhookEmbedBuilder()
            .setColor(title.hashCode() and 0x00FFFFFF)
            .setTitle(WebhookEmbed.EmbedTitle("$punishedDisplayName | $title", null))
            .setDescription("Fazer o que né, não soube ler as regras! <:sad_cat:419474182758334465>")
            .addField(WebhookEmbed.EmbedField(true, "Quem puniu", punisherDisplayName))
            .addField(WebhookEmbed.EmbedField(true, "Motivo", effectiveReason ?: "Não sei, não quero saber, e tenho raiva de quem sabe (Sem motivo definido)"))
            .addField(WebhookEmbed.EmbedField(true, "Servidor", server ?: "Desconhecido"))

        if (time != null)
            embed.addField(WebhookEmbed.EmbedField(true, "Duração", DateUtils.formatDateDiff(time)))

        if (punishedUniqueId != null)
            embed.setFooter(WebhookEmbed.EmbedFooter("UUID do usuário: $punishedUniqueId", "https://sparklypower.net/api/v1/render/avatar?name=$punishedDisplayName&scale=16"))

        val avatarDisplay = if (punisherDisplayName == "Pantufa")
            "Pantufinha"
        else punisherDisplayName

        embed.setThumbnailUrl("https://sparklypower.net/api/v1/render/avatar?name=$avatarDisplay&scale=16")

        m.punishmentWebhook.send(embed.build())
    }

    fun getPunisherName(sender: CommandSource): String {
        return when (sender) {
            is Player -> sender.username
            else -> "Pantufa"
        }
    }

    suspend fun getPunisherName(uniqueId: UUID?): String? {
        if (uniqueId == null)
            return "Pantufa"

        return getUserNameByUniqueId(uniqueId)
    }

    fun getPunishedInfoByString(playerName: String): Punished? {
        var punishedUniqueId: UUID? = null
        var punishedDisplayName: String? = null

        // O nosso querido "player name" pode ser várias coisas...
        // Talvez seja um player online!

        var player = server.getPlayer(playerName).getOrNull()

        if (player != null) {
            punishedDisplayName = player.username
            punishedUniqueId = player.uniqueId
        } else {
            // Talvez seja o UUID de um player online!
            punishedUniqueId = try {
                UUID.fromString(playerName)
            } catch (e: IllegalArgumentException) {
                null
            }
            if (punishedUniqueId != null) {
                val playerByUuid = server.getPlayer(punishedUniqueId).getOrNull()
                if (playerByUuid != null) {
                    player = playerByUuid
                    punishedUniqueId = playerByUuid.uniqueId
                    punishedDisplayName = playerByUuid.username
                } else {
                    // ...tá, mas talvez seja o UUID de um player offline!
                    // Caso o UUID seja != null, quer dizer que ele É UM UUID VÁLIDO!!
                    punishedDisplayName = punishedUniqueId.toString()
                }
            } else {
                // Se não, vamos processar como se fosse um player mesmo
                punishedDisplayName = playerName
                punishedUniqueId = getUniqueId(playerName)
            }
        }

        if (punishedDisplayName == null && punishedUniqueId == null)
            return null

        return Punished(punishedDisplayName, punishedUniqueId, player)
    }

    fun hideIp(ip: String): String {
        val split = ip.split(".")
        if (split.size == 4)
            return "${split[0]}.${split[1]}.XXX.XXX"
        return "XXX.XXX.XXX.XXX"
    }

    data class Punished(
        val displayName: String?,
        val uniqueId: UUID?,
        val player: Player?
    )
}