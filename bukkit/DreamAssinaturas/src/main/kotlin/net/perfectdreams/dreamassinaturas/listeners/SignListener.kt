package net.perfectdreams.dreamassinaturas.listeners

import com.Acrobot.ChestShop.Signs.ChestShopSign
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.DreamAssinaturas
import net.perfectdreams.dreamassinaturas.data.Assinatura
import net.perfectdreams.dreamassinaturas.tables.Assinaturas
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.colorize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcore.utils.stripColors
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
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

            val split = template.split("\n")

            val replacedLines = split.map {
                it.replace("{0}", lines[1])
                    .replace("{1}", lines[2])
                    .replace("{2}", lines[3])
                    .colorize()
            }

            // Don't allow users to create signatures that contain "SparklyShop" in the sign template
            if (replacedLines.any { it.stripColors().contains("SparklyShop", true) })
                return

            if (replacedLines.firstOrNull() == "§1[Reparar]")
                return

            val isChestShopSign = ChestShopSign.isValid(replacedLines.toTypedArray())
            if (isChestShopSign)
                return

            for ((index, str) in replacedLines.withIndex()) {
                e.setLine(
                    index,
                    str
                )
            }

            m.schedule(SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    Assinaturas.insert {
                        it[Assinaturas.signedBy] = e.player.uniqueId
                        it[Assinaturas.signedAt] = System.currentTimeMillis()
                        it[Assinaturas.worldName] = e.block.location.world.name
                        it[Assinaturas.x] = e.block.location.x
                        it[Assinaturas.y] = e.block.location.y
                        it[Assinaturas.z] = e.block.location.z
                    }
                }
                m.loadSignatures()
                switchContext(SynchronizationContext.SYNC)
                e.player.sendMessage("§aPlaca de assinatura criada com sucesso!")
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onRightClick(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        if (e.clickedBlock?.type?.name?.contains("SIGN") == false)
            return

        val clickedBlockLocation = e.clickedBlock?.location ?: return

        val signature = m.storedSignatures[Assinatura.AssinaturaLocation(
            clickedBlockLocation.world.name,
            clickedBlockLocation.x.toInt(),
            clickedBlockLocation.y.toInt(),
            clickedBlockLocation.z.toInt()
        )] ?: return

        // Wow, é uma assinatura! :3
        val indexOf = m.storedSignatures.values
            .asSequence()
            .filter { it.signedBy == signature.signedBy }
            .sortedBy(Assinatura::id)
            .indexOf(signature)

        // We will cancel the event to avoid propagating to ChestShop & stuff
        e.isCancelled = true

        m.schedule(SynchronizationContext.ASYNC) {
            val username = transaction(Databases.databaseNetwork) {
                val user = User.findById(signature.signedBy)
                user?.username
            }

            val instant = signature.signedAt.atZone(ZoneId.of("America/Sao_Paulo"))
            switchContext(SynchronizationContext.SYNC)

            e.player.sendMessage("§8[ §bAssinatura §8]".centralizeHeader())
            e.player.sendMessage("§bAssinado por: §a$username")
            e.player.sendMessage("§bNúmero da Assinatura: §a#${indexOf + 1}")
            e.player.sendMessage("§bData da Assinatura: §3$instant")
            e.player.sendMessage(DreamUtils.HEADER_LINE)
        }
    }
}