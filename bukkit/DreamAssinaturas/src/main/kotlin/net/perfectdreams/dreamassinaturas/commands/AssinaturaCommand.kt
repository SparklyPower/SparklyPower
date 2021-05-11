package net.perfectdreams.dreamassinaturas.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.DreamAssinaturas
import net.perfectdreams.dreamassinaturas.tables.AssinaturaTemplates
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.Command
import net.perfectdreams.dreamcore.utils.commands.CommandContext
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.block.Sign
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object AssinaturaCommand : DSLCommandBase<DreamAssinaturas> {
    override fun command(plugin: DreamAssinaturas) = create(
        listOf("assinatura")
    ) {
        permission = "dreamassinaturas.setup"

        executes {
            player.sendMessage("§e/assinatura template")
            player.sendMessage("§e/assinatura delete")
        }
    }
}