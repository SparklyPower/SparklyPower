package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.dao.Ban
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.dao.Profile
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.*
import net.perfectdreams.pantufa.api.minecraft.AccountResult
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.minecraft.MinecraftAccountInfo
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

abstract class UnleashedContext(
    val pantufa: PantufaBot,
    val discordGuildLocale: DiscordLocale?,
    val discordUserLocale: DiscordLocale,
    val jda: JDA,
    val mentions: UnleashedMentions,
    val user: User,
    val memberOrNull: Member?,
    val guildOrNull: Guild?,
    val channelOrNull: MessageChannel?,
) {
    val guildId
        get() = guildOrNull?.idLong

    val guild: Guild
        get() = guildOrNull ?: error("This interaction was not sent in a guild!")

    val member: Member
        get() = memberOrNull ?: error("This interaction was not sent in a guild!")

    val channel: MessageChannel
        get() = channelOrNull ?: error("This interaction was not sent in a message channel!")

    var wasInitiallyDeferredEphemerally: Boolean? = null

    abstract suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook

    suspend fun reply(ephemeral: Boolean, content: String) = reply(ephemeral) {
        this.content = content
    }

    fun retrieveLorittaProfile(userId: Long): Profile {
        return transaction(Databases.loritta) {
            Profile.findById(userId)
        } ?: throw RuntimeException()
    }

    fun getUserBanned(uuid: UUID) = transaction(Databases.sparklyPower) {
        Ban.find {
            Bans.player eq uuid and  (
                    Bans.temporary eq false or (
                            Bans.temporary eq true and (
                                    Bans.expiresAt.isNotNull() and(
                                            Bans.expiresAt greaterEq System.currentTimeMillis()
                                            )
                                    )
                            )
                    )
        }.firstOrNull()
    }

    fun getPlayerSonecasBalance(uuid: UUID) = transaction(Databases.sparklyPower) {
        val playerSonecasData = PlayerSonecas.selectAll().where {
            PlayerSonecas.id eq uuid
        }.firstOrNull()

        return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
    }

    suspend fun retrieveAccountStatus(username: String): AccountResult = pantufa.transactionOnSparklyPowerDatabase {
        val dbUser = net.perfectdreams.pantufa.dao.User.find { Users.username eq username }.firstOrNull()
            ?: return@transactionOnSparklyPowerDatabase AccountResult.UNKNOWN_PLAYER

        val connectedAccounts = DiscordAccount.find {
            DiscordAccounts.minecraftId eq dbUser.id.value and (DiscordAccounts.isConnected eq true)
        }.count()

        if (connectedAccounts != 0L)
            return@transactionOnSparklyPowerDatabase AccountResult.ALREADY_REGISTERED

        DiscordAccount.new {
            this.minecraftId = dbUser.id.value
            this.discordId = user.idLong
            this.isConnected = false
        }

        return@transactionOnSparklyPowerDatabase AccountResult.OK
    }

    suspend fun retrieveConnectedDiscordAccount() =
        pantufa.retrieveDiscordAccountFromUser(user.idLong)

    suspend fun retrieveConnectedMinecraftAccount(): MinecraftAccountInfo? {
        val discordAccount = retrieveConnectedDiscordAccount() ?: return null

        if (!discordAccount.isConnected)
            return null

        val dbUser = transaction(Databases.sparklyPower) {
            net.perfectdreams.pantufa.dao.User.find { Users.id eq discordAccount.minecraftId }.firstOrNull()
        }

        if (dbUser == null) {
            reply(false) {
                styled(
                    "${user.asMention} Parece que você tem uma conta associada, mas não existe o seu username salvo no banco de dados! Bug?",
                    Constants.ERROR
                )
            }
            return null
        }

        return MinecraftAccountInfo(
            discordAccount.minecraftId,
            dbUser.username
        )
    }

    suspend fun retrieveConnectedMinecraftAccountOrFail(): MinecraftAccountInfo {
        return retrieveConnectedMinecraftAccount() ?: run {
            reply(false) {
                styled(
                    "Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!",
                    Constants.ERROR
                )
            }

            throw SilentCommandException()
        }
    }

    suspend fun reply(ephemeral: Boolean, data: MessageCreateData): InteractionMessage {
        return reply(ephemeral) {
            content = data.content
            embeds += data.embeds
            components += data.components
        }
    }

    abstract suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit = {}): InteractionMessage
}