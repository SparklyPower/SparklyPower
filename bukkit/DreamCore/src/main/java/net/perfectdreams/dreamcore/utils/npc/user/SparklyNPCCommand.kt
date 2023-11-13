package net.perfectdreams.dreamcore.utils.npc.user

import kotlinx.datetime.Clock
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread

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
            executor = PlayerSkinNPCExecutor()
        }

        subcommand(listOf("mojangskin")) {
            executor = PlayerSkinNPCExecutor()
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
            val npcId = integer("id")
            val npcName = greedyString("name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val npcId = args[options.npcId]
            val npcName = args[options.npcName]

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return
            }

            sparklyNPC.setPlayerName(npcName)

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC $npcId teve seu nome alterado!")
            }
        }
    }

    inner class DeleteNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = integer("id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val npcId = args[options.npcId]

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return
            }

            m.sparklyUserNPCManager.createdNPCs.remove(npcId)
            sparklyNPC.sparklyNPC.remove()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC $npcId foi deletado... tchauzinho para ele...")
            }
        }
    }

    inner class MoveHereNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = integer("id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val npcId = args[options.npcId]

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return
            }

            sparklyNPC.teleport(player.location)

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("NPC $npcId foi teletransportado até você!")
            }
        }
    }

    inner class LookCloseNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = integer("id")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val npcId = args[options.npcId]

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return
            }

            sparklyNPC.lookClose = !sparklyNPC.lookClose

            if (sparklyNPC.lookClose) {
                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Agora NPC $npcId irá olhar para players que estão perto!")
                }
            } else {
                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Agora NPC $npcId não irá olhar para players que estão perto!")
                }
            }
        }
    }

    inner class SparklySkinNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = integer("id")
            val autoRefreshSkin = boolean("auto_refresh_skin")
            val npcPlayerSkinName = optionalGreedyString("player_skin_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val npcId = args[options.npcId]

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return
            }

            val playerSkinName = args[options.npcPlayerSkinName]
            if (playerSkinName == null) {
                sparklyNPC.setTextures(null)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Skin do NPC $npcId foi removida!")
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
                    content("NPC $npcId agora tem a skin do player ${playerSkinName}!")
                }
            }
        }
    }

    inner class PlayerSkinNPCExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val npcId = integer("id")
            val autoRefreshSkin = boolean("auto_refresh_skin")
            val npcPlayerSkinName = optionalGreedyString("player_skin_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val npcId = args[options.npcId]

            val sparklyNPC = m.sparklyUserNPCManager.createdNPCs[npcId]
            if (sparklyNPC == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("NPC desconhecido!")
                }
                return
            }

            val playerSkinName = args[options.npcPlayerSkinName]
            if (playerSkinName == null) {
                sparklyNPC.setTextures(null)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Skin do NPC $npcId foi removida!")
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
                    content("NPC $npcId agora tem a skin do player ${playerSkinName}!")
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
}