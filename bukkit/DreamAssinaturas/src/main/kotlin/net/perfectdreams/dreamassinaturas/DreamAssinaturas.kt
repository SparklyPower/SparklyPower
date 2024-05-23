package net.perfectdreams.dreamassinaturas

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamassinaturas.commands.AssinaturaCommand
import net.perfectdreams.dreamassinaturas.commands.AssinaturaDeleteCommand
import net.perfectdreams.dreamassinaturas.commands.AssinaturaTemplateCommand
import net.perfectdreams.dreamassinaturas.data.Assinatura
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class DreamAssinaturas : KotlinPlugin(), Listener {
	val storedTemplates = mutableMapOf<UUID, String>()
	// We use a map here because then the check can be O(1) instead of O(n) (iterating the list)
	var storedSignatures = mapOf<Assinatura.AssinaturaLocation, Assinatura>()

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
				for ((assinaturaLocation, assinaturaData) in storedSignatures.toList()) {
					val location = assinaturaLocation.toBukkitLocation()

					// Maybe someone added a signature in a deleted world!
					if (location.isWorldLoaded) {
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
									Assinaturas.deleteWhere {
										Assinaturas.id eq assinaturaData.id
									}
								}
								switchContext(SynchronizationContext.SYNC)
							}
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
			storedSignatures = Assinaturas.selectAll().associate {
				val assinaturaLocation = Assinatura.AssinaturaLocation(
					it[Assinaturas.worldName],
					it[Assinaturas.x].toInt(),
					it[Assinaturas.y].toInt(),
					it[Assinaturas.z].toInt()
				)

				assinaturaLocation to Assinatura(
					it[Assinaturas.id].value,
					it[Assinaturas.signedBy],
					Instant.ofEpochMilli(it[Assinaturas.signedAt]),
					assinaturaLocation
				)
			}
		}
	}
}