package net.perfectdreams.pantufa.interactions.vanilla.utils

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.utils.extensions.await
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class NotificarCommand : SlashCommandDeclarationWrapper {
    companion object {
        val eventRoles = mapOf(
            "quiz" to 555184377504464898L,
            "dropparty" to 539979402143072267L,
            "minarecheada" to 538805118384996392L,
            "labirinto" to 539471142772146176L,
            "corrida" to 477979984275701760L,
            "fight" to 798697267312853002L,
            "especial" to 798697126573506571L,
            "mcmmo2x" to 798696876052185119L,
            "enquetes" to 1157110842815418408L
        )
    }

    override fun command() = slashCommand("notify", "TODOFIXTHISDATA", CommandCategory.UTILS) {
        enableLegacyMessageSupport = true

        subcommand("player", "Te notifica quando um jogador entrar no servidor!") {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("notify player")
                add("notificar player")
            }
            executor = NotificarPlayerCommandExecutor()
        }

        subcommand("event", "Te notifica quando um evento começar!") {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("notify event")
                add("notificar evento")
            }
            executor = NotificarEventoCommandExecutor()
        }
    }

    inner class NotificarPlayerCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val player = string("player", "O nome do jogador que você deseja ser notificado")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val playerName = args[options.player]
            val minecraftUser = context.pantufa.getMinecraftUserFromUsername(playerName) ?: run {
                context.reply(true) {
                    styled(
                        "O usuário `${playerName.replace("`", "")}` parece não jogar no SparklyPower, tem certeza que colocou o nick correto?"
                    )
                }
                return
            }

            val selfAccount = context.pantufa.getDiscordAccountFromUser(context.user) ?: run {
                context.reply(true) {
                    styled(
                        "Você precisa registrar a sua conta antes de poder receber notificações!"
                    )
                }
                return
            }

            val existingTracker = transaction(Databases.sparklyPower) {
                NotifyPlayersOnline.selectAll()
                    .where { NotifyPlayersOnline.player eq selfAccount.minecraftId and (NotifyPlayersOnline.tracked eq minecraftUser.id.value) }
                    .count() != 0L
            }

            if (existingTracker) {
                transaction(Databases.sparklyPower) {
                    NotifyPlayersOnline.deleteWhere { player eq selfAccount.minecraftId and (tracked eq minecraftUser.id.value) }
                }

                context.reply(false) {
                    styled(
                        "Você agora vai parar de receber notificações quando `${playerName.replace("`", "")}` entra no SparklyPower!"
                    )
                }
            } else {
                transaction(Databases.sparklyPower) {
                    NotifyPlayersOnline.insert {
                        it[player] = selfAccount.minecraftId
                        it[tracked] = minecraftUser.id.value
                    }
                }

                context.reply(false) {
                    styled(
                        "Você agora vai receber notificações quando `${playerName.replace("`", "")}` entrar no SparklyPower!"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val username = args.getOrNull(0)

            if (username == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.player to username
            )
        }
    }
    inner class NotificarEventoCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val event = string("event", "O nome do evento que você deseja ser notificado") {
                choice("Quiz", "555184377504464898")
                choice("Drop Party", "539979402143072267")
                choice("Mina Recheada", "538805118384996392")
                choice("Labirinto", "539471142772146176")
                choice("Corrida", "477979984275701760")
                choice("Fight", "798697267312853002")
                choice("Especial", "798697126573506571")
                choice("McMMO 2x", "798696876052185119")
                choice("Enquetes", "1157110842815418408")
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val member = context.memberOrNull ?: return
            val guild = context.guild
            val sparklyPower = context.pantufa.config.sparklyPower

            if (guild.idLong != sparklyPower.guild.idLong) {
                context.reply(true) {
                    styled(
                        "Você só pode usar o comando no servidor do SparklyPower! https://discord.gg/sparklypower"
                    )
                }
                return
            }

            val selectedEvent = args[options.event].toLongOrNull()

            if (selectedEvent == null) {
                context.reply(true) {
                    styled(
                        "Evento inválido! Eventos suportados: ${eventRoles.keys.joinToString(", ") { "`$it`" }}"
                    )
                }
                return
            }

            val role = guild.getRoleById(selectedEvent) ?: run {
                context.reply(true) {
                    styled(
                        "Parece que o cargo desse evento não existe mais... Avise para a equipe!"
                    )
                }
                return
            }

            if (member.roles.contains(role)) {
                guild.removeRoleFromMember(member, role).await()

                context.reply(false) {
                    styled(
                        "Cargo removido com sucesso! Cansou do evento para remover a notificação dele?"
                    )
                }

                return
            } else {
                guild.addRoleToMember(member, role).await()

                context.reply(false) {
                    styled(
                        "Cargo adicionado com sucesso! Quero ver você sempre no evento, hein?"
                    )
                }

                return
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val event = args.getOrNull(0)

            if (event == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.event to eventRoles[event].toString()
            )
        }
    }
}