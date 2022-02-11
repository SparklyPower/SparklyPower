package net.perfectdreams.dreamclubes.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.subcommands.*
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeHome
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class ClubesCommand(val m: DreamClubes) : SparklyCommand(arrayOf("clube", "clubes", "clan", "clans")) {
    @Subcommand
    fun root(player: Player, args: Array<String>) {
        val arg0 = args.getOrNull(0)

        async {
            val clube = ClubeAPI.getPlayerClube(player)

            var selfMember: ClubeMember? = null

            if (clube != null) {
                selfMember = clube.retrieveMember(player) ?: return@async

                if (arg0 == "admin") {
                    AdminSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "dono") {
                    DonoSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "tag") {
                    TagSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "name" || arg0 == "nome") {
                    NameSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "lista" || arg0 == "list" || arg0 == "players") {
                    PlayersSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "coords") {
                    CoordsSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "convidar") {
                    ConvidarSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "playertag" || arg0 == "prefixo") {
                    PlayerTagSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "vitals") {
                    VitalsSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "kick" || arg0 == "expulsar") {
                    KickSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "sair" || arg0 == "quit") {
                    SairSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                } else if (arg0 == "deletar" || arg0 == "delete") {
                    DeletarSubCommand(m).execute(
                        player,
                        clube,
                        selfMember,
                        args.toMutableList().apply { this.removeAt(0) }.toTypedArray()
                    )
                    return@async
                }
            }

            if (arg0 == "aceitar") {
                AceitarSubCommand(m).execute(player, args.toMutableList().apply { this.removeAt(0) }.toTypedArray())
                return@async
            }

            player.sendMessage("§8[ §bClube §8]".centralizeHeader())
            var isOwner = false
            var isAdmin = false
            var hasClan = false
            if (clube != null) {
                hasClan = true
                if (selfMember?.canExecute(ClubePermissionLevel.ADMIN) == true) {
                    isAdmin = true
                }
                if (selfMember?.canExecute(ClubePermissionLevel.OWNER) == true) {
                    isOwner = true
                }
            }
            if (!hasClan) {
                player.sendMessage("/clube criar - Cria uma clube! §aCusto: 150000 sonecas")
            }
            player.sendMessage("/clube lista - Mostra os 10 clube mais poderosos")
            player.sendMessage("/clube leaderboard - Mostra os 10 players com maior KDR")
            player.sendMessage("/clube kdr - Mostra o seu KDR")
            player.sendMessage("/clube resetkdr - Reseta o seu KDR")
            if (hasClan && !isOwner) {
                player.sendMessage("/clube sair - Sai do seu clube atual")
            }
            if (hasClan && isOwner) {
                player.sendMessage("/clube deletar - Exclui o seu clube")
                player.sendMessage("/clube admin - Deixa um player como admin")
                player.sendMessage("/clube dono - Transfere a posse do clube para outro player")
            }
            if (hasClan) {
                player.sendMessage("/clube coords - Mostra as coordenadas dos seus amigos")
                player.sendMessage("/clube vitals - Mostra o status de seus amigos")
                player.sendMessage("/clube membros - Mostra os membros do clube")
            }
            if (hasClan && isAdmin) {
                player.sendMessage("/clube tag - Altera a tag do clube")
                player.sendMessage("/clube nome - Altera o nome do clube")
                player.sendMessage("/clube kick - Remove algu\u00e9m do clube")
                player.sendMessage("/clube prefixo - Coloca um prefixo no player")
                player.sendMessage("/clube setcasa - Marca a casa do clube")
            }
            if (hasClan) {
                player.sendMessage("/clube casa - Teletransporta para a casa do clube")
            }
            if (hasClan) {
                player.sendMessage("/. - Chat do Clube")
            }
            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")

        }
    }

    @Subcommand(["criar", "create"])
    fun create(player: Player, tag: String, args: Array<String>) {
        val name = args.joinToString(" ")

        if (name.isBlank()) {
            player.sendMessage("${DreamClubes.PREFIX} §cO nome do seu clube é inválido!")
            return
        }

        if (150_000 > player.balance) {
            player.sendMessage("${DreamClubes.PREFIX} §cVocê precisa ter 150000 sonecas para criar um clube!")
            return
        }

        async {
            val clube = ClubeAPI.getPlayerClube(player)

            if (clube != null) {
                toSync()
                player.sendMessage("${DreamClubes.PREFIX} §cVocê já está em um clube!")
                return@async
            }

            val coloredTag = tag.translateColorCodes()
            val coloredName = name.translateColorCodes()
            val cleanName = name.stripColorCode()
            val cleanTag = ChatColor.stripColor(coloredTag)!!

            if (!ClubeAPI.checkIfClubeCanUseTagAndSendMessages(player, clube, coloredTag))
                return@async

            transaction(Databases.databaseNetwork) {
                val theNewClube = Clube.new {
                    this.name = coloredName
                    this.cleanName = cleanName
                    this.shortName = coloredTag
                    this.cleanShortName = cleanTag
                    this.createdAt = System.currentTimeMillis()
                    this.ownerId = player.uniqueId
                }

                ClubeAPI.getOrCreateClubePlayerWrapper(player.uniqueId, theNewClube).apply {
                    this.permissionLevel = ClubePermissionLevel.OWNER
                }
            }
            // Clube criado, yay!
            toSync()
            player.balance -= 150_000

            Bukkit.broadcastMessage("${DreamClubes.PREFIX} §eClube $coloredTag§e/§b$coloredName§e de ${player.displayName}§e foi criado!")
        }
    }

    @Subcommand(["casa", "home"])
    fun goToClubeHome(player: Player) {
        async {
            val clube = ClubeAPI.getPlayerClube(player)

            if (clube != null) {
                val clubeHome = transaction(Databases.databaseNetwork) {
                    clube.home
                }

                if (clubeHome != null) {
                    toSync()

                    val location = Location(
                        Bukkit.getWorld(clubeHome.worldName),
                        clubeHome.x,
                        clubeHome.y,
                        clubeHome.z,
                        clubeHome.yaw,
                        clubeHome.pitch
                    )

                    player.teleport(location)
                    player.world.spawnParticle(Particle.VILLAGER_HAPPY, player.location.add(0.0, 0.5, 0.0), 25, 0.5, 0.5, 0.5)
                    player.sendMessage("§aVocê chegou ao seu destino. §cʕ•ᴥ•ʔ")
                    player.sendTitle("§b${clube.name}", "§3${TextUtils.ROUND_TO_2_DECIMAL.format(location.x)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(location.y)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(location.z)}", 10, 60, 10)
                }
            }
        }
    }

    @Subcommand(["setcasa", "sethome"])
    fun setClubeHome(player: Player) {
        async {
            val clube = ClubeAPI.getPlayerClube(player)

            if (clube != null) {
                val clubeMember = clube.retrieveMember(player) ?: return@async

                if (!clubeMember.permissionLevel.canExecute(ClubePermissionLevel.ADMIN)) {
                    player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão para fazer isto!")
                    return@async
                }

                val location = player.location
                transaction(Databases.databaseNetwork) {
                    val oldHome = clube.home

                    val newHome = ClubeHome.new {
                        this.x = location.x
                        this.y = location.y
                        this.z = location.z
                        this.yaw = location.yaw
                        this.pitch = location.pitch
                        this.worldName = location.world.name
                    }

                    clube.home = newHome
                    oldHome?.delete()
                }

                toSync()
                player.sendMessage("${DreamClubes.PREFIX} §aCasa do clube marcada com sucesso!")
            }
        }
    }
}