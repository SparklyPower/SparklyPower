package net.perfectdreams.dreamassinaturas

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.commands.AssinaturaCommand
import net.perfectdreams.dreamassinaturas.commands.AssinaturaDeleteCommand
import net.perfectdreams.dreamassinaturas.commands.AssinaturaTemplateCommand
import net.perfectdreams.dreamassinaturas.dao.Assinatura
import net.perfectdreams.dreamassinaturas.listeners.SignListener
import net.perfectdreams.dreamassinaturas.tables.AssinaturaTemplates
import net.perfectdreams.dreamassinaturas.tables.Assinaturas
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Particle
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamAssinaturas : KotlinPlugin(), Listener {
	val storedTemplates = mutableMapOf<UUID, String>()
	var storedSignatures = listOf<Assinatura>()

	override fun softEnable() {
		super.softEnable()

		registerCommand(AssinaturaCommand)
		registerCommand(AssinaturaTemplateCommand)
		registerCommand(AssinaturaDeleteCommand)

		registerEvents(SignListener(this))

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Assinaturas,
				AssinaturaTemplates
			)
		}

		loadTemplates()
		loadSignatures()

		scheduler().schedule(this) {
			while (true) {
				// Nós iremos clonar a lista já que pode ter outra thread modificando a lista
				for (signature in storedSignatures.toList()) {
					val location = signature.getLocation()

					val isChunkLoaded = location.isChunkLoaded
					if (isChunkLoaded) {
						// Não é mais uma placa! Iremos remover...
						if (location.block.type.name.contains("SIGN")) {
							location.world.spawnParticle(
								Particle.VILLAGER_HAPPY,
								location.add(0.5, 0.5, 0.5),
								1,
								0.25,
								0.25,
								0.25
							)
						} else {
							switchContext(SynchronizationContext.ASYNC)
							transaction(Databases.databaseNetwork) {
								signature.delete()
							}
							switchContext(SynchronizationContext.SYNC)
						}
					}
				}
				waitFor(20)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun loadTemplates() {
		transaction(Databases.databaseNetwork) {
			AssinaturaTemplates.selectAll().forEach {
				storedTemplates[it[AssinaturaTemplates.id].value] = it[AssinaturaTemplates.template]
			}
		}
	}

	fun loadSignatures() {
		transaction(Databases.databaseNetwork) {
			storedSignatures = Assinatura.all().sortedBy { it.signedAt }.toList()
		}
	}
}