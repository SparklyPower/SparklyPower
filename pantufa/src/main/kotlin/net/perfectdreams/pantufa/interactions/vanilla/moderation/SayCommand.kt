package net.perfectdreams.pantufa.interactions.vanilla.moderation

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.pantufa.utils.extensions.await
import net.perfectdreams.pantufa.api.commands.styled

class SayCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("say", "Envia mensagens pela Pantufa") {
        subcommand("send", "Envia uma mensagem pela Pantufa") {
            executor = SaySendCommandExecutor()
        }

        subcommand("edit", "Edita uma mensagem enviada pela Pantufa") {
            executor = SayEditCommandExecutor()
        }
    }

    inner class SaySendCommandExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val channel = channel("channel", "Canal onde será enviado a mensagem")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val mainLandGuild = context.pantufa.mainLandGuild
            val sparklyPower = context.pantufa.config.sparklyPower

            val staffRole = mainLandGuild!!.getRoleById(sparklyPower.guild.staffRoleId)!!

            if (!context.member.roles.contains(staffRole)) {
                context.reply(true) {
                    styled(
                        "Ei! Você não tem permissão para usar este comando.",
                        "<:pantufa_bonk:1028160322990776331>"
                    )
                }
                return
            }

            val channel = mainLandGuild.getGuildChannelById(args[options.channel].id) ?: return

            context.reply(true) {
                styled(
                    "Clique no botão para escrever uma mensagem.",
                    "<:pantufa_analise:853048446813470762>"
                )

                actionRow(
                    context.pantufa.interactivityManager
                        .buttonForUser(context.user, ButtonStyle.PRIMARY, "Escrever Mensagem") { button ->
                            val message = modalString(
                                "Mensagem",
                                TextInputStyle.PARAGRAPH,
                                value = "A pantufa é muito fofa :3"
                            )

                            button.sendModal(
                                "Mensagem",
                                listOf(ActionRow.of(message.toJDA())),
                            ) { it, args ->
                                (channel as TextChannel).sendMessage(args[message]).queue()

                                it.reply(true) {
                                    styled(
                                        "Mensagem enviada com sucesso!",
                                        "<:pantufa_coffee:853048446981111828>"
                                    )
                                }
                            }
                        }
                )
            }
        }
    }

    inner class SayEditCommandExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val messageReference = string("message_link", "Link da mensagem que você deseja editar!")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val mainLandGuild = context.pantufa.mainLandGuild
            val sparklyPower = context.pantufa.config.sparklyPower

            val staffRole = mainLandGuild!!.getRoleById(sparklyPower.guild.staffRoleId)!!

            if (!context.member.roles.contains(staffRole)) {
                context.reply(true) {
                    styled(
                        "Ei! Você não tem permissão para usar este comando.",
                        "<:pantufa_bonk:1028160322990776331>"
                    )
                }
                return
            }

            val extractedMessage = args[options.messageReference].extractMessage()
            val channel = mainLandGuild.getTextChannelById(extractedMessage.channelId) ?: return
            val message = channel.retrieveMessageById(extractedMessage.messageId).await()

            if (message == null) {
                context.reply(true) {
                    styled(
                        "Mensagem não encontrada!",
                        "<:pantufa_bonk:1028160322990776331>"
                    )
                }
                return
            }

            if (message.author.id != context.pantufa.jda.selfUser.id) {
                context.reply(true) {
                    styled(
                        "Você não pode editar mensagens que não foram enviadas pela Pantufa!",
                        "<:pantufa_bonk:1028160322990776331>"
                    )
                }
                return
            }

            context.reply(true) {
                styled(
                    "Clique no botão para editar a mensagem.",
                    "<:pantufa_analise:853048446813470762>"
                )

                actionRow(
                    context.pantufa.interactivityManager
                        .buttonForUser(context.user, ButtonStyle.PRIMARY, "Editar Mensagem") { button ->
                            val messageInput = modalString(
                                "Mensagem",
                                TextInputStyle.PARAGRAPH,
                                value = message.contentRaw
                            )

                            button.sendModal(
                                "Mensagem",
                                listOf(ActionRow.of(messageInput.toJDA())),
                            ) { it, args ->
                                message.editMessage(args[messageInput]).queue()

                                it.reply(true) {
                                    styled(
                                        "Mensagem editada com sucesso!",
                                        "<:pantufa_coffee:853048446981111828>"
                                    )
                                }
                            }
                        }
                )
            }
        }

        private fun String.extractMessage(): ExtractedMessage {
            val parts = this.split("/")

            val guildId = parts[4]
            val channelId = parts[5]
            val messageId = parts[6]

            return ExtractedMessage(guildId, channelId, messageId)
        }
    }

    data class ExtractedMessage(
        val guildId: String,
        val channelId: String,
        val messageId: String
    )
}