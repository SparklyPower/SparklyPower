package net.perfectdreams.dreamassinaturas.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.DreamAssinaturas
import net.perfectdreams.dreamassinaturas.tables.AssinaturaTemplates
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.block.Sign
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object AssinaturaTemplateCommand : DSLCommandBase<DreamAssinaturas> {
    override fun command(plugin: DreamAssinaturas) = create(
        listOf("assinatura template")
    ) {
        permission = "dreamassinaturas.setup"

        executes {
            val targetBlock = player.getTargetBlock(null, 10)

            if (targetBlock?.type?.name?.contains("SIGN") == false) {
                player.sendMessage("§cVocê precisa estar olhando para uma placa!")
                return@executes
            }

            val sign = targetBlock.state as Sign
            val joinedLines = sign.lines.joinToString("\n")

            scheduler().schedule(plugin, SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    AssinaturaTemplates.deleteWhere {
                        AssinaturaTemplates.id eq player.uniqueId
                    }

                    AssinaturaTemplates.insert {
                        it[_id] = player.uniqueId
                        it[template] = joinedLines
                    }
                }

                plugin.loadTemplates()

                switchContext(SynchronizationContext.SYNC)

                player.sendMessage("§aTemplate criado com sucesso! Para automaticamente preencher uma placa, escreva §e*assinatura*§a na primeira linha da placa!")
            }
        }
    }
}