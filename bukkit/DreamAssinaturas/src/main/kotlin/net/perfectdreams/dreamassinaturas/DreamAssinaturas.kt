package net.perfectdreams.dreamassinaturas

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamassinaturas.commands.AssinaturaCommand
import net.perfectdreams.dreamassinaturas.commands.AssinaturaCommand.Companion.prefix
import net.perfectdreams.dreamassinaturas.data.Assinatura
import net.perfectdreams.dreamassinaturas.listeners.SignListener
import net.perfectdreams.dreamassinaturas.tables.AssinaturaTemplates
import net.perfectdreams.dreamassinaturas.tables.Assinaturas
import net.perfectdreams.dreamassinaturas.utils.buildAndSendMessage
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class DreamAssinaturas : KotlinPlugin(), Listener {
	var signaturesToBeMoved = mutableMapOf<UUID, JsonObject>()
	val storedTemplates = mutableMapOf<UUID, String>()
	// We use a map here because then the check can be O(1) instead of O(n) (iterating the list)
	var storedSignatures = mapOf<Assinatura.AssinaturaLocation, Assinatura>()

	override fun softEnable() {
		super.softEnable()

		registerCommand(AssinaturaCommand(this))

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
									Particle.HAPPY_VILLAGER,
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

	fun transferSignature(player: Player, targetBlock: Block, event: PlayerInteractEvent? = null) {
		val currentClaim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)
		val playerData = GriefPrevention.instance.dataStore.getPlayerData(player.uniqueId)

		if (currentClaim != null) {
			if (!currentClaim.hasExplicitPermission(player.uniqueId, ClaimPermission.Build) && !playerData.ignoreClaims) {
				if (event == null) {
					player.buildAndSendMessage {
						color(NamedTextColor.RED)
						append(prefix())
						appendSpace()
						append("Você não tem permissão para mover placas nesse terreno!")
					}
				}
				return
			}
		}

		// we need to check if the targetBlock is a sign
		if (!targetBlock.type.name.contains("SIGN")) {
			player.buildAndSendMessage {
				color(NamedTextColor.RED)
				append(prefix())
				appendSpace()
				append("Você precisa estar olhando para uma placa!")
			}
			return
		}

		// now we need to check if the sign is a signature
		// if it is, we cannot overwrite an existent signature
		if (storedSignatures.containsKey(Assinatura.AssinaturaLocation(
				targetBlock.world.name,
				targetBlock.x,
				targetBlock.y,
				targetBlock.z
			))) {
			player.buildAndSendMessage {
				color(NamedTextColor.RED)
				append(AssinaturaCommand.prefix())
				appendSpace()
				append("A placa que você está tentando aplicar a assinatura já é uma assinatura!")
			}
			return
		}

		if (signaturesToBeMoved.containsKey(player.uniqueId)) {
			if (event != null) {
				event.isCancelled = true
			}

			// If the player uuid is in the map, it means that the user wants to move the signature set to him
			// let's get the signature id that is set to the player uuid
			val signature = signaturesToBeMoved[player.uniqueId] ?: run {
				// if the signature id is null, it means that the player is trying to move a signature that doesn't exist
				// so, we can remove him from the map and return
				signaturesToBeMoved.remove(player.uniqueId)
				return
			}

			val signatureToMove = storedSignatures.values.firstOrNull { it.id == signature["id"]!!.jsonPrimitive.long } ?: run {
				// if the signature doesn't exist, we can remove the player from the map and return
				signaturesToBeMoved.remove(player.uniqueId)
				return
			}

			// we need to update the block location of the signature in the database
			scheduler().schedule(this, SynchronizationContext.ASYNC) {
				// get the previous signature sign from the database
				val previousSignatureSign = transaction(Databases.databaseNetwork) {
					Assinaturas.select {
						Assinaturas.id eq signatureToMove.id
					}.first()
				}

				transaction(Databases.databaseNetwork) {
					Assinaturas.update({
						Assinaturas.id eq signatureToMove.id
					}) {
						it[worldName] = targetBlock.world.name
						it[x] = targetBlock.x.toDouble()
						it[y] = targetBlock.y.toDouble()
						it[z] = targetBlock.z.toDouble()
					}
				}

				loadSignatures()

				switchContext(SynchronizationContext.SYNC)

				// then we need to break the old signature sign just to keep things tidy
				// obs: the things related to the server itself cannot run in an asynchronous context
				val block = server.getWorld(previousSignatureSign[Assinaturas.worldName])?.getBlockAt(
					previousSignatureSign[Assinaturas.x].toInt(),
					previousSignatureSign[Assinaturas.y].toInt(),
					previousSignatureSign[Assinaturas.z].toInt()
				)

				if (block?.state is Sign) {
					// break naturally to drop the sign item
					block.breakNaturally()
				}

				// ok, all set, now we need to transfer the content of the signature to the new sign
				val sign = targetBlock.state as Sign

				val content = signature["content"]!!.jsonArray.map { it.jsonPrimitive.content }

				for ((index, str) in content.withIndex()) {
					sign.setLine(
						index,
						str
					)
				}

				sign.update()

				player.buildAndSendMessage {
					color(NamedTextColor.GREEN)
					append(AssinaturaCommand.prefix())
					appendSpace()
					append("Assinatura movida com sucesso!")
				}

				// now we can safely remove the player of the map
				signaturesToBeMoved.remove(player.uniqueId)
			}
		} else {
			if (event == null) {
				player.buildAndSendMessage {
					color(NamedTextColor.RED)
					append(prefix())
					appendSpace()
					append("Você não selecionou nenhuma assinatura para mover!")
				}
			}
		}
	}
}