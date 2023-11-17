package net.perfectdreams.dreamcore.utils.npc.user

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SparklyNPCCommand(val m: DreamCore) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("sparklynpc")) {
        permission = "sparklynpc.manage"

        subcommand(listOf("create")) {
            executor = CreateNPCExecutor()
        }

        subcommand(listOf("rename")) {
            executor = RenameNPCExecutor()
        }

        subcommand(listOf("delete")) {
            executor = DeleteNPCExecutor()
        }

        subcommand(listOf("movehere", "tphere")) {
            executor = MoveHereNPCExecutor()
        }

        subcommand(listOf("lookclose")) {
            executor = LookCloseNPCExecutor()
        }

        subcommand(listOf("sparklyskin")) {
            executor = SparklySkinNPCExecutor()
        }

        subcommand(listOf("mojangskin")) {
            executor = MojangSkinNPCExecutor()
        }

        subcommand(listOf("mineskin")) {
            executor = MineSkinNPCExecutor()
        }


        subcommand(listOf("who", "select")) {
            executor = WhoIAmLookingAtNPCExecutor()
        }
    }

    inner class CreateNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcName = greedyString("name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val npcName = args[options.npcName]

            // Get first available ID
            var id = 0
            while (m.sparklyUserNPCManager.createdNPCs.keys.contains(id)) {
                id++
            }
            m.sparklyUserNPCManager.spawn(
                UserCreatedNPCData(
                    id,
                    npcName,
                    UserCreatedNPCData.LocationReference.fromBukkit(player.location),
                    false,
                    null
                )
            )

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC \"${npcName}\" (ID: $id) foi criado com sucesso!")
            }
        }
    }

    inner class RenameNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
            val npcName = greedyString("name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val npcName = args[options.npcName]

            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            sparklyNPC.setPlayerName(npcName)

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC ${sparklyNPC.data.id} teve seu nome alterado!")
            }
        }
    }

    inner class DeleteNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val npcId = args[options.npcId]

            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            m.sparklyUserNPCManager.createdNPCs.remove(sparklyNPC.data.id)
            sparklyNPC.sparklyNPC.remove()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC $npcId foi deletado... tchauzinho para ele...")
            }
        }
    }

    inner class MoveHereNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            sparklyNPC.teleport(player.location)

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC ${sparklyNPC.data.id} foi teletransportado até você!")
            }
        }
    }

    inner class LookCloseNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            sparklyNPC.lookClose = !sparklyNPC.lookClose

            if (sparklyNPC.lookClose) {
                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Agora NPC ${sparklyNPC.data.id} irá olhar para players que estão perto!")
                }
            } else {
                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Agora NPC ${sparklyNPC.data.id} não irá olhar para players que estão perto!")
                }
            }
        }
    }

    inner class SparklySkinNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
            val autoRefreshSkin = boolean("auto_refresh_skin")
            val npcPlayerSkinName = greedyString("player_skin_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            val playerSkinName = args[options.npcPlayerSkinName]
            if (playerSkinName == null) {
                sparklyNPC.setTextures(null)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Skin do NPC ${sparklyNPC.data.id} foi removida!")
                }
                return
            }

            m.launchAsyncThread {
                val user = DreamUtils.retrieveUserInfo(playerSkinName)
                if (user == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Player desconhecido!")
                    }
                    return@launchAsyncThread
                }

                val profileProperty = m.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(user.id.value)
                if (profileProperty == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Não consegui pegar a skin do player ${playerSkinName}...")
                    }
                    return@launchAsyncThread
                }

                onMainThread {
                    sparklyNPC.setTextures(
                        UserCreatedNPCData.CustomSkin(
                            SkinTexture(profileProperty.value, profileProperty.signature!!),
                            UserCreatedNPCData.CustomSkin.SkinTextureSource.SparklyTextureSource(user.id.value.toString()),
                            args[options.autoRefreshSkin],
                            Clock.System.now()
                        )
                    )
                }

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("NPC ${sparklyNPC.data.id} agora tem a skin do player ${playerSkinName}!")
                }
            }
        }
    }

    inner class MojangSkinNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
            val autoRefreshSkin = boolean("auto_refresh_skin")
            val npcPlayerSkinName = greedyString("player_skin_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            val playerSkinName = args[options.npcPlayerSkinName]
            if (playerSkinName == null) {
                sparklyNPC.setTextures(null)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Skin do NPC ${sparklyNPC.data.id} foi removida!")
                }
                return
            }

            m.launchAsyncThread {
                val accountResponse = m.skinUtils.retrieveMojangAccountInfo(playerSkinName)
                if (accountResponse == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Não consegui pegar a skin do player ${playerSkinName}...")
                    }
                    return@launchAsyncThread
                }

                onMainThread {
                    sparklyNPC.setTextures(
                        UserCreatedNPCData.CustomSkin(
                            SkinTexture(accountResponse.textures.raw.value, accountResponse.textures.raw.signature),
                            UserCreatedNPCData.CustomSkin.SkinTextureSource.MojangTextureSource(accountResponse.uuid),
                            args[options.autoRefreshSkin],
                            Clock.System.now()
                        )
                    )
                }

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("NPC ${sparklyNPC.data.id} agora tem a skin do player ${playerSkinName}!")
                }
            }
        }
    }

    inner class MineSkinNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = quotableString("id")
            val npcMineSkinSkinUrl = greedyString("mineskin_skin_url")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val sparklyNPC = getNPCFromId(context, context.sender, args[options.npcId]) ?: return

            val mineskinPlayerSkinUrl = args[options.npcMineSkinSkinUrl]
            if (mineskinPlayerSkinUrl == null) {
                sparklyNPC.setTextures(null)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Skin do NPC ${sparklyNPC.data.id} foi removida!")
                }
                return
            }

            m.launchAsyncThread {
                val response = DreamUtils.http.get("https://api.mineskin.org/get/uuid/${mineskinPlayerSkinUrl.substringAfterLast("/")}")

                if (response.status != HttpStatusCode.OK) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Não consegui pegar a skin ${mineskinPlayerSkinUrl}...")
                    }
                    return@launchAsyncThread
                }

                val responseData = JsonIgnoreUnknownKeys.decodeFromString<MineSkinPlayerSkinResponse>(response.bodyAsText())

                onMainThread {
                    sparklyNPC.setTextures(
                        UserCreatedNPCData.CustomSkin(
                            SkinTexture(responseData.data.texture.value, responseData.data.texture.signature),
                            UserCreatedNPCData.CustomSkin.SkinTextureSource.MineSkinTextureSource,
                            false,
                            Clock.System.now()
                        )
                    )
                }

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("NPC ${sparklyNPC.data.id} agora tem a skin ${mineskinPlayerSkinUrl}!")
                }
            }
        }
    }

    inner class WhoIAmLookingAtNPCExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val targetEntity = player.getTargetEntity(15)
            if (targetEntity == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você não está olhando para uma entidade!")
                }
                return
            }
            val userNPC = m.sparklyUserNPCManager.createdNPCs.values.firstOrNull { it.sparklyNPC.uniqueId == targetEntity.uniqueId }
            if (userNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você não está olhando para um NPC!")
                }
                return
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Você está olhando para o NPC \"${userNPC.data.name}\", ID ${userNPC.data.id}")
            }
        }
    }

    @Serializable
    data class MineSkinPlayerSkinResponse(
        val data: MineSkinPlayerSkinData
    ) {
        @Serializable
        data class MineSkinPlayerSkinData(
            val uuid: String,
            val texture: MineSkinPlayerSkinTexture
        )

        @Serializable
        data class MineSkinPlayerSkinTexture(
            val value: String,
            val signature: String
        )
    }

    fun getNPCFromId(context: CommandContext, player: CommandSender, id: String): UserCreatedNPC? {
        if (id == "look") {
            if (player !is Player) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você só pode usar \"look\" se você for um player!")
                }
                return null
            }
            val targetEntity = player.getTargetEntity(15)

            if (targetEntity == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você não está olhando para uma entidade!")
                }
                return null
            }
            val userNPC = m.sparklyUserNPCManager.createdNPCs.values.firstOrNull { it.sparklyNPC.uniqueId == targetEntity.uniqueId }
            if (userNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você não está olhando para um NPC!")
                }
                return null
            }

            return userNPC
        } else {
            val npcId = id.toIntOrNull()
            if (npcId == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você não passou um ID válido!")
                }
                return null
            }

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return null
            }

            return sparklyNPC
        }
    }
}