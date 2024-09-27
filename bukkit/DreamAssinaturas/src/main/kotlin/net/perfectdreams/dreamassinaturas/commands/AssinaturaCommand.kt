package net.perfectdreams.dreamassinaturas.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.serialization.json.*
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamassinaturas.DreamAssinaturas
import net.perfectdreams.dreamassinaturas.data.Assinatura
import net.perfectdreams.dreamassinaturas.tables.AssinaturaTemplates
import net.perfectdreams.dreamassinaturas.tables.Assinaturas
import net.perfectdreams.dreamassinaturas.utils.buildAndSendMessage
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.block.Sign
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class AssinaturaCommand(val m: DreamAssinaturas) : SparklyCommandDeclarationWrapper {
    companion object {
        fun prefix() = textComponent {
            append("[") {
                color(NamedTextColor.DARK_GRAY)
            }

            append("Assinaturas") {
                color(NamedTextColor.AQUA)
                decorate(TextDecoration.BOLD)
            }

            append("]") {
                color(NamedTextColor.DARK_GRAY)
            }
        }
    }

    override fun declaration() = sparklyCommand(listOf("assinatura")) {
        executor = AssinaturaCommandExecutor()

        subcommand(listOf("template")) {
            permission = "dreamassinaturas.setup"
            executor = AssinaturaTemplateCommandExecutor()
        }

        subcommand(listOf("delete")) {
            permission = "dreamassinaturas.staff"
            executor = AssinaturaDeleteCommandExecutor()
        }

        subcommand(listOf("mover")) {
            executor = AssinaturaMoverCommandExecutor()
        }
    }

    inner class AssinaturaCommandExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            context.sendMessage {
                color(NamedTextColor.YELLOW)
                append(prefix())
                appendSpace()

                append("Comandos disponíveis:")
                append("\n")
                appendCommand("/assinatura template")
                append(" - Cria um template de assinatura baseado na placa que você está olhando no momento;\n")
                appendCommand("/assinatura delete")
                append(" - Remove a assinatura caso esteja olhando para ela;\n")
                appendCommand("/assinatura mover")
                append(" - Move uma assinatura de uma placa para outra.")
            }
        }
    }

    inner class AssinaturaTemplateCommandExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val targetBlock = player.getTargetBlock(null, 10)

            if (!targetBlock.type.name.contains("SIGN")) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(prefix())
                    appendSpace()
                    append("Você precisa estar olhando para uma placa!")
                }
                return
            }

            val sign = targetBlock.state as Sign
            val joinedLines = sign.lines.joinToString("\n")

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    AssinaturaTemplates.deleteWhere {
                        id eq player.uniqueId
                    }

                    AssinaturaTemplates.insert {
                        it[_id] = player.uniqueId
                        it[template] = joinedLines
                    }
                }

                m.loadTemplates()

                switchContext(SynchronizationContext.SYNC)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    append(prefix())
                    appendSpace()
                    append("Template criado com sucesso! Para automaticamente preencher uma placa, escreva §b*assinatura*")
                    append(" na primeira linha da placa!")
                }
            }
        }
    }

    inner class AssinaturaDeleteCommandExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val targetBlock = player.getTargetBlock(null, 10)

            if (!targetBlock.type.name.contains("SIGN")) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(prefix())
                    appendSpace()
                    append("Você precisa estar olhando para uma placa!")
                }
                return
            }

            val signature = m.storedSignatures[Assinatura.AssinaturaLocation(
                targetBlock.world.name,
                targetBlock.x,
                targetBlock.y,
                targetBlock.z
            )]

            if (signature == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(prefix())
                    appendSpace()
                    append("Isto não é uma placa de assinatura!")
                }
                return
            }

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    Assinaturas.deleteWhere {
                        Assinaturas.id eq signature.id
                    }
                }

                m.loadSignatures()

                switchContext(SynchronizationContext.SYNC)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    append(prefix())
                    appendSpace()
                    append("Assinatura deletada com sucesso!")
                }
            }
        }
    }

    inner class AssinaturaMoverCommandExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val action = word("action") { _, builder ->
                listOf("selecionar", "aplicar", "cancelar").forEach {
                    builder.suggest(it)
                }
            }
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val action = args[options.action]

            when (action) {
                // check if the user is trying to cancel the signature
                "cancelar" -> {
                    if (m.signaturesToBeMoved.containsKey(player.uniqueId)) {
                        m.signaturesToBeMoved.remove(player.uniqueId)

                        context.sendMessage {
                            color(NamedTextColor.GREEN)
                            append(prefix())
                            appendSpace()
                            append("Você cancelou a movimentação da assinatura!")
                        }

                        return
                    }
                }

                "selecionar" -> {
                    val currentClaim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)
                    val playerData = GriefPrevention.instance.dataStore.getPlayerData(player.uniqueId)

                    println(playerData.ignoreClaims)

                    if (currentClaim != null) {
                        if (!currentClaim.hasExplicitPermission(player.uniqueId, ClaimPermission.Build) && !playerData.ignoreClaims) {
                            player.buildAndSendMessage {
                                color(NamedTextColor.RED)
                                append(prefix())
                                appendSpace()
                                append("Você não tem permissão para mover placas nesse terreno!")
                            }
                            return
                        }
                    }

                    val targetBlock = player.getTargetBlock(null, 10)

                    if (!targetBlock.type.name.contains("SIGN")) {
                        context.sendMessage {
                            color(NamedTextColor.RED)
                            append(prefix())
                            appendSpace()
                            append("Você precisa estar olhando para uma placa!")
                        }
                        return
                    }

                    val signature = m.storedSignatures[Assinatura.AssinaturaLocation(
                        targetBlock.world.name,
                        targetBlock.x,
                        targetBlock.y,
                        targetBlock.z
                    )]

                    // check if the sign is a signature sign
                    if (signature == null) {
                        context.sendMessage {
                            color(NamedTextColor.RED)
                            append(prefix())
                            appendSpace()
                            append("Isto não é uma placa de assinatura!")
                        }
                        return
                    }

                    // build the signature data with all needed information
                    val signatureData = buildJsonObject {
                        put("id", "${signature.id}")
                        put("content", buildJsonArray {
                            (targetBlock.state as Sign).lines.forEach {
                                add(it)
                            }
                        })
                    }

                    // stored it into the map with the user's uuid
                    m.signaturesToBeMoved[player.uniqueId] = signatureData

                    context.sendMessage {
                        color(NamedTextColor.GREEN)
                        append(prefix())
                        appendSpace()

                        append("Agora você precisa clicar em outra placa ou usar ")
                        appendCommand("/assinatura mover aplicar")
                        append(" para mover a assinatura! Caso se não quiser mais mover a assinatura, use ")
                        appendCommand("/assinatura mover cancelar")
                        append("!")
                    }
                }

                "aplicar" -> {
                    val targetBlock = player.getTargetBlock(null, 5)

                    m.transferSignature(player, targetBlock)
                }
            }
        }
    }
}