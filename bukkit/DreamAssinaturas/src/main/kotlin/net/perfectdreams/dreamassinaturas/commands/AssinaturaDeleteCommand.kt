package net.perfectdreams.dreamassinaturas.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.DreamAssinaturas
import net.perfectdreams.dreamassinaturas.data.Assinatura
import net.perfectdreams.dreamassinaturas.tables.AssinaturaTemplates
import net.perfectdreams.dreamassinaturas.tables.Assinaturas
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.Command
import net.perfectdreams.dreamcore.utils.commands.CommandContext
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.block.Sign
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object AssinaturaDeleteCommand : DSLCommandBase<DreamAssinaturas> {
    override fun command(plugin: DreamAssinaturas) = create(
        listOf("assinatura delete")
    ) {
        permission = "dreamassinaturas.staff"

        executes {
            val targetBlock = player.getTargetBlock(null, 10)

            if (!targetBlock.type.name.contains("SIGN")) {
                player.sendMessage("§cVocê precisa estar olhando para uma placa!")
                return@executes
            }

            val signature = plugin.storedSignatures[Assinatura.AssinaturaLocation(
                targetBlock.world.name,
                targetBlock.x,
                targetBlock.y,
                targetBlock.z
            )]

            if (signature == null) {
                player.sendMessage("§cIsto não é uma placa de assinatura!")
                return@executes
            }

            scheduler().schedule(plugin, SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    Assinaturas.deleteWhere {
                        Assinaturas.id eq signature.id
                    }
                }
                plugin.loadSignatures()

                switchContext(SynchronizationContext.SYNC)

                player.sendMessage("§aAssinatura deletada com sucesso!")
            }
        }
    }
}