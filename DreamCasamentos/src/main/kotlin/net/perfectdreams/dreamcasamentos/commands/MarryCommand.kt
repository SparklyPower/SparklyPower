package net.perfectdreams.dreamcasamentos.commands

import com.google.gson.Gson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcasamentos.DreamCasamentos
import net.perfectdreams.dreamcasamentos.DreamCasamentos.Companion.PREFIX
import net.perfectdreams.dreamcasamentos.LocationWrapper
import net.perfectdreams.dreamcasamentos.dao.Adoption
import net.perfectdreams.dreamcasamentos.dao.Marriage
import net.perfectdreams.dreamcasamentos.dao.Request
import net.perfectdreams.dreamcasamentos.tables.Adoptions
import net.perfectdreams.dreamcasamentos.utils.MarriageParty
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class MarryCommand(val m: DreamCasamentos) : SparklyCommand(arrayOf("marry", "casar", "casamento")) {

    @Subcommand
    fun root(sender: CommandSender) {
        sender as Player

        sender.sendMessage("$PREFIX Use /marry (player|sethome|tp|home|divorce)")
    }

    @Subcommand
    fun marry(sender: Player, playerName: String) {
        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("$PREFIX Este jogador não foi encontrado!")
            return
        }

        if (sender == player) {
            sender.sendMessage("$PREFIX Você não pode casar consigo mesm${sender.artigo}, bobinh${sender.artigo}!")
            return
        }

        val selfMarriage = m.getMarriageFor(sender)

        if (selfMarriage != null) {
            sender.sendMessage("$PREFIX §cVocê já está casado! Se você quer casar com outra pessoa, use §6/marry divorciar§c para divorciar!")
            return
        }

        val playerMarriage = m.getMarriageFor(player)
        val playerRequest = m.getMarriageRequestFor(player)

        if (playerMarriage != null) {
            sender.sendMessage("$PREFIX §b${player.name}§e já está casad${player.artigo}!")
            return
        }

        if (playerRequest != null) {
            sender.sendMessage("$PREFIX §b${player.name}§e já tem um pedido de casamento ativo!")
            return
        }

        if (sender.balance < 7500) {
            sender.sendMessage("$PREFIX É, casamentos são caros e necessitam de dinheiro para serem mantidos. Você precisa de mais §b${7500 - sender.balance}§e sonhos para se casar!")
            return
        }

        if (player.balance < 7500) {
            sender.sendMessage("$PREFIX É, casamentos são caros e necessitam de dinheiro para serem mantidos. §b${player.name}§e precisa de mais §b${7500 - player.balance}§e sonhos para se casar!")
            return
        }

        val request = Request(Request.RequestKind.MARRIAGE, sender, player)
        m.requests.add(request)

        sender.sendMessage("$PREFIX Proposta enviada para §b${player.name}§e, boa sorte!")
        player.sendMessage("$PREFIX Você recebeu uma proposta de §b${sender.name}§e, você deseja aceitar (§a§n/marry aceitar§r§e) ou recusar (§c§n/marry recusar§r§e)?")

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            waitFor(20 * 60)

            if (m.getMarriageRequestFor(sender) != null) {
                sender.sendMessage("$PREFIX A proposta de casamento expirou pois passou-se um minuto e não houve resposta!")

                m.requests.remove(request)
            }
        }
    }

    @Subcommand(["aceitar", "accept"])
    fun accept(sender: Player) {
        val request = m.getMarriageRequestFor(sender)

        if (request == null) {
            val request = m.getAdoptionRequestFor(sender)

            if (request != null) {
                val requestSender = request.sender

                m.requests.remove(request)

                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                    val marriage = m.getMarriageFor(requestSender)

                    if (marriage == null) {
                        sender.sendMessage("$PREFIX Pelo visto a pessoa que queria te adotar não está casada!")
                        return@schedule
                    }

                    val adoption = transaction(Databases.databaseServer) {
                        Adoption.new {
                            this.player = sender.uniqueId
                            this.adotedAt = System.currentTimeMillis()
                            this.adoptedBy = marriage
                        }
                    }

                    switchContext(SynchronizationContext.SYNC)

                    sender.balance -= 7500
                    requestSender.balance -= 7500

                    sender.sendMessage("$PREFIX Você aceitou o pedido de adoção de §b${requestSender.name}§e!")
                    requestSender.sendMessage("$PREFIX §b${sender.name}§e aceitou o seu pedido de adoção!")

                    Bukkit.broadcastMessage("$PREFIX §b${sender.name}§e aceitou o pedido de adoção de §b${requestSender.name}§e!")
                }
                return
            }

            sender.sendMessage("$PREFIX Você não tem nenhum pedido de casamento ou de adoção pendente!")

            return
        }

        val requestSender = request.sender

        if (sender.balance < 7500) {
            sender.sendMessage("$PREFIX É, casamentos são caros e necessitam de dinheiro para serem mantidos. Você precisa de mais §b${7500 - sender.balance}§e sonhos para se casar!")
            return
        }

        if (requestSender.balance < 7500) {
            sender.sendMessage("$PREFIX É, casamentos são caros e necessitam de dinheiro para serem mantidos. §b${requestSender.name}§e precisa de mais §b${7500 - requestSender.balance}§e sonhos para se casar!")
            return
        }

        m.requests.remove(request)

        val shipName = m.getShipName(requestSender.name, sender.name)

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val marriage = transaction(Databases.databaseServer) {
                Marriage.new {
                    this.player1 = requestSender.uniqueId
                    this.player2 = sender.uniqueId
                }
            }

            switchContext(SynchronizationContext.SYNC)

            sender.balance -= 7500
            requestSender.balance -= 7500

            sender.sendMessage("$PREFIX Você aceitou o pedido de casamento de §b${requestSender.name}§e! Felicidades ao casal §b$shipName§e!")
            requestSender.sendMessage("$PREFIX §b${sender.name}§e aceitou o seu pedido de casamento! Felicidades ao casal §b$shipName§e!")

            Bukkit.broadcastMessage("$PREFIX §b${sender.name}§e aceitou o pedido de casamento de §b${requestSender.name}§e! Felicidades ao casal §b$shipName§e!")

            MarriageParty.startMarriageParty(marriage, sender, requestSender)

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                waitFor(20 * 30)

                sender.sendMessage("$PREFIX Agora vocês precisam de uma casa! Combine com §b${requestSender.name}§e e demarquem a casa usando §b/marry sethome§e!")
                requestSender.sendMessage("$PREFIX Agora vocês precisam de uma casa! Combine com §b${sender.name}§e e demarquem a casa usando §b/marry sethome§e!")
            }
        }
    }

    @Subcommand(["recusar", "rejeitar", "reject"])
    fun reject(sender: Player) {
        val request = m.getMarriageRequestFor(sender)

        if (request == null) {
            sender.sendMessage("$PREFIX Você não tem nenhum pedido de casamento pendente!")
            return
        }

        val requestSender = request.sender

        sender.sendMessage("$PREFIX Você rejeitou o pedido de casamento de §b${requestSender.name}§e!")
        requestSender.sendMessage("$PREFIX §b${sender.name}§e rejeitou o seu pedido de casamento! :(")

        m.requests.remove(request)
    }

    @Subcommand(["sethome", "set_home"])
    fun setHome(sender: Player) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val marriage = m.getMarriageFor(sender)

            switchContext(SynchronizationContext.SYNC)

            if (marriage == null) {
                sender.sendMessage("$PREFIX Você não está casado com ninguém!")
                return@schedule
            }

            val location = sender.location

            if (location.blacklistedTeleport) {
                sender.sendMessage("$PREFIX Você está em um lugar que o sistema de GPS não consegue te encontrar!")
                return@schedule
            }

            switchContext(SynchronizationContext.ASYNC)

            transaction(Databases.databaseServer) {
                marriage.homeWorld = location.world.name
                marriage.homeX = location.x
                marriage.homeY = location.y
                marriage.homeZ = location.z
            }

            switchContext(SynchronizationContext.SYNC)

            val partnerId = marriage.getPartnerOf(sender)
            val partner = Bukkit.getPlayer(partnerId)


            sender.sendMessage("$PREFIX Você mudou a localização da casa do casal!")
            partner?.sendMessage("$PREFIX §b${sender.name}§e mudou a localização da casa! (Localização atual: X: §b${location.x}§e, Y: §b${location.y}§e, Z: §b${location.z}§e)")
        }
    }

    @Subcommand(["tp"])
    fun tp(sender: Player) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val marriage = m.getMarriageFor(sender)

            switchContext(SynchronizationContext.SYNC)

            if (marriage == null) {
                sender.sendMessage("$PREFIX Você não está casado com ninguém!")
                return@schedule
            }

            val partnerId = marriage.getPartnerOf(sender)
            val partner = Bukkit.getPlayer(partnerId)

            if (partner.location.blacklistedTeleport) {
                sender.sendMessage("$PREFIX §b${partner.name}§e está em um lugar que o sistema de GPS não consegue te encontrar!")
                return@schedule
            }

            if (sender.location.blacklistedTeleport) {
                sender.sendMessage("$PREFIX Você está em um lugar que o sistema de GPS não consegue te encontrar!")
                return@schedule
            }

            sender.teleport(partner)
            sender.sendMessage("$PREFIX Você foi até §b${partner.name}§e!")
        }
    }

    @Subcommand(["home", "casa"])
    fun home(sender: Player) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val marriage = m.getMarriageFor(sender)

            switchContext(SynchronizationContext.SYNC)

            if (marriage == null) {
                sender.sendMessage("$PREFIX Você não está casado com ninguém!")
                return@schedule
            }

            if (marriage.homeWorld == null) {
                sender.sendMessage("$PREFIX O seu casal não tem uma casa!")
                return@schedule
            }

            if (sender.location.blacklistedTeleport) {
                sender.sendMessage("$PREFIX Você está em uma localização que o sistema de GPS não consegue te encontrar!")
                return@schedule
            }

            val home = marriage.getHomeLocation()

            sender.teleport(home)
            sender.sendMessage("$PREFIX Você foi até a casa do casal!")
        }
    }

    @Subcommand(["divorce", "divorciar"])
    fun divorce(sender: Player) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val marriage = m.getMarriageFor(sender)

            if (marriage == null) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("$PREFIX Você não está casado com ninguém!")
                return@schedule
            }

            val partnerId = marriage.getPartnerOf(sender)
            val partner = Bukkit.getPlayer(partnerId)

            transaction(Databases.databaseNetwork) {
                Adoptions.deleteWhere { Adoptions.adoptedBy eq marriage.id }
                marriage.delete()
            }

            switchContext(SynchronizationContext.SYNC)

            partner?.sendMessage("$PREFIX §b${sender.name}§e se divorciou de você! :(")
            sender.sendMessage("$PREFIX Você se divorciou! :(")

            val offlinePlayer = Bukkit.getOfflinePlayer(partnerId) // Sempre assumir que o player está offline para não dar erro se o cara não tiver online
            Bukkit.broadcastMessage("$PREFIX §b${sender.name}§e se divorciou de §b${offlinePlayer.name}§e! :(")
        }
    }

    @Subcommand(["loc1"])
    fun loc1(sender: Player) {
        sender as Player

        if (sender.hasPermission("dreamcasamentos.manage")) {
            m.config.loc1 = LocationWrapper(sender.location.world.name, sender.location.x, sender.location.y, sender.location.z, sender.location.yaw, sender.location.pitch)

            val file = File(m.dataFolder, "config.json")
            file.writeText(Gson().toJson(m.config))
        }
    }

    @Subcommand(["loc2"])
    fun loc2(sender: Player) {
        sender as Player

        if (sender.hasPermission("dreamcasamentos.manage")) {
            m.config.loc2 = LocationWrapper(sender.location.world.name, sender.location.x, sender.location.y, sender.location.z, sender.location.yaw, sender.location.pitch)

            val file = File(m.dataFolder, "config.json")
            file.writeText(Gson().toJson(m.config))
        }
    }

    @Subcommand(["adotar", "adopt", "filho", "filha"])
    fun adotar(sender: Player) {
        sender.sendMessage("§cMas você vai adotar quem?")
    }


    @Subcommand(["adotar", "adopt", "filho", "filha"])
    fun adotar(sender: Player, playerName: String) {
        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage("$PREFIX Este jogador não foi encontrado!")
            return
        }

        if (sender == player) {
            sender.sendMessage("$PREFIX Você não pode adotar você mesm${sender.artigo}, bobinh${sender.artigo}!")
            return
        }

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val adoptionStatus = canAdopt(sender, player)

            switchContext(SynchronizationContext.SYNC)

            when (adoptionStatus) {
                AdoptReturn.ALREADY_ADOPTED -> {
                    sender.sendMessage("$PREFIX §b${player.name}§e já tem uma família... que pena.")
                    return@schedule
                }
                else -> {
                }
            }

            val request = Request(Request.RequestKind.ADOPTION, sender, player)
            m.requests.add(request)

            sender.sendMessage("$PREFIX Proposta enviada para §b${player.name}§e, boa sorte!")
            player.sendMessage("$PREFIX Você recebeu um pedido de adoção de §b${sender.name}§e, você deseja aceitar (§a§n/marry aceitar§r§e) ou recusar (§c§n/marry recusar§r§e)?")

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                waitFor(20 * 60)

                if (m.getAdoptionRequestFor(sender) != null) {
                    sender.sendMessage("$PREFIX A proposta de adoção expirou pois passou-se um minuto e não houve resposta!")

                    m.requests.remove(request)
                }
            }
        }
    }

    fun getGenderSymbol(player: Player): String {
        return if (!player.girl) { "§3♂" } else { "§d♀" }
    }

    fun canAdopt(adopter: Player, adoptee: Player): AdoptReturn {
        DreamUtils.assertAsyncThread()

        val adopteeMarriage = m.getMarriageFor(adoptee)

        if (adopteeMarriage != null && (adopteeMarriage.player1 == adopter.uniqueId || adopteeMarriage.player2 == adopter.uniqueId))
            return AdoptReturn.ALREADY_MARRIED

        val adoption = m.getAdoptionStatus(adoptee)

        if (adoption != null)
            return AdoptReturn.ALREADY_ADOPTED

        return AdoptReturn.ALLOWED
    }

    enum class AdoptReturn {
        ALREADY_MARRIED,
        ALREADY_ADOPTED,
        ALLOWED
    }
}