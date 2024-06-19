package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.pantufa.api.minecraft.MinecraftUserDisplayUtils
import net.perfectdreams.pantufa.api.commands.styled
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

class MinecraftCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("sparklyplayer", "Veja a conta associada ao SparklyPower de um usuário", CommandCategory.MINECRAFT) {
        enableLegacyMessageSupport = true

        subcommand("playername", "Veja a conta associada ao SparklyPower pelo nome de um player no SparklyPower") {
            enableLegacyMessageSupport = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("mcplayer")
            }

            executor = MinecraftCheckPlayerCommandExecutor()
        }

        subcommand("discorduser", "Veja a conta associada ao SparklyPower pela conta no Discord") {
            enableLegacyMessageSupport = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("mcuser")
            }

            executor = MinecraftCheckUserCommandExecutor()
        }
    }

    inner class MinecraftCheckPlayerCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val playerName = string("player_name", "Nome do Player") {
                autocomplete {
                    val focusedOptionValue = it.event.focusedOption.value

                    transaction(Databases.sparklyPower) {
                        Users.select(Users.username).where {
                            Users.username.like(focusedOptionValue.replace("%", "") + "%")
                        }.limit(25)
                    }.associate { it[Users.username] to it[Users.username] }
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val playerName = args[options.playerName]

            context.deferChannelMessage(false)

            val minecraftUser = context.pantufa.getMinecraftUserFromUsername(playerName) ?: run {
                context.reply(false) {
                    styled(
                        "O jogador `$playerName` não tem uma conta do Discord associada!"
                    )
                }
                return
            }

            val userInfo = context.pantufa.getDiscordAccountFromUniqueId(minecraftUser.id.value)

            MinecraftUserDisplayUtils.replyWithAccountInformation(
                context.pantufa,
                context,
                userInfo,
                minecraftUser
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val playerName = args.getOrNull(0)

            if (playerName == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.playerName to playerName
            )
        }
    }

    inner class MinecraftCheckUserCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", "Conta do Usuário no Discord")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val discordUser = args[options.user]

            context.deferChannelMessage(false)

            val minecraftUser = context.pantufa.getDiscordAccountFromId(discordUser.user.idLong) ?: run {
                context.reply(false) {
                    styled(
                        "O usuário <@${discordUser.user.id}> não tem uma conta associada!"
                    )
                }
                return
            }

            val userInfo = context.pantufa.getMinecraftUserFromUniqueId(minecraftUser.minecraftId)

            MinecraftUserDisplayUtils.replyWithAccountInformation(
                context.pantufa,
                context,
                minecraftUser,
                userInfo
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val user = context.mentions.users.firstOrNull()

            if (user == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.user to UserAndMember(
                    user,
                    context.guild.getMemberById(user.idLong)
                )
            )
        }
    }
}