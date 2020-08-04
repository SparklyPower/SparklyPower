package net.perfectdreams.dreamassinaturas.listeners

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.DreamAssinaturas
import net.perfectdreams.dreamassinaturas.dao.Assinatura
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.colorize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class SignListener(val m: DreamAssinaturas) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSignChange(e: SignChangeEvent) {
        if (!e.player.hasPermission("dreamassinaturas.setup"))
            return

        if (e.lines[0] != "*assinatura*")
            return

        // Don't allow users to create signatures that contain "SparklyShop" in the sign signature
        if (e.lines.any { it.contains("SparklyShop", true) })
            return

        // Parece ser uma assinatura...
        val template = m.storedTemplates[e.player.uniqueId]

        if (template != null) {
            // Assinatura, wow!
            val lines = e.lines.copyOf()
            // Don't allow users to create signatures that contain "SparklyShop" in the sign template
            if (lines.any { it.contains("SparklyShop", true) })
                return

            val split = template.split("\n")

            for ((index, str) in split.withIndex())
                e.setLine(
                    index,
                    str.replace("{0}", lines[1])
                        .replace("{1}", lines[2])
                        .replace("{2}", lines[3])
                        .colorize()
                )

            m.schedule(SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    Assinatura.new {
                        this.signedBy = e.player.uniqueId
                        this.signedAt = System.currentTimeMillis()
                        this.setLocation(e.block.location)
                    }
                }
                m.loadSignatures()
                switchContext(SynchronizationContext.SYNC)
                e.player.sendMessage("§aPlaca de assinatura criada com sucesso!")
            }
        }
    }

    @EventHandler
    fun onRightClick(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        if (e.clickedBlock?.type?.name?.contains("SIGN") == false)
            return

        val signature = m.storedSignatures.firstOrNull {
            it.getLocation() == e.clickedBlock?.location
        } ?: return

        // Wow, é uma assinatura! :3
        val indexOf = m.storedSignatures.filter { it.signedBy == signature.signedBy }
            .indexOf(signature)

        m.schedule(SynchronizationContext.ASYNC) {
            val username = transaction(Databases.databaseNetwork) {
                val user = User.findById(signature.signedBy)
                user?.username
            }

            val instant = Instant.ofEpochMilli(signature.signedAt)
                .atZone(ZoneId.of("America/Sao_Paulo"))
            switchContext(SynchronizationContext.SYNC)

            e.player.sendMessage("§8[ §bAssinatura §8]".centralizeHeader())
            e.player.sendMessage("§bAssinado por: §a$username")
            e.player.sendMessage("§bNúmero da Assinatura: §a#${indexOf + 1}")
            e.player.sendMessage("§bData da Assinatura: §3$instant")
            e.player.sendMessage(DreamUtils.HEADER_LINE)
        }
    }
}