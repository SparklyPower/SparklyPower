package net.perfectdreams.dreamloja.listeners

import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.coroutines.InternalCoroutinesApi
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.UserShopVote
import net.perfectdreams.dreamloja.dao.VoteSign
import net.perfectdreams.dreamloja.tables.UserShopVotes
import net.perfectdreams.dreamloja.tables.VoteSigns
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SignListener(val m: DreamLoja) : Listener {
    suspend fun BukkitSchedulerController.updateVoteSigns(owner: UUID) {
        val voteSigns = transaction(Databases.databaseNetwork) {
            VoteSign.find {
                VoteSigns.owner eq owner
            }.toMutableList()
        }

        val voteCount = transaction(Databases.databaseNetwork) {
            net.perfectdreams.dreamloja.tables.UserShopVotes.select {
                UserShopVotes.receivedBy eq owner
            }.count()
        }

        switchContext(SynchronizationContext.SYNC)

        voteSigns.forEach {
            val location = it.getLocation()
            val sign = location.block.state

            if (sign is Sign) {
                sign.setLine(3, "§bVotos: §3$voteCount")
                sign.update()
            } else {
                switchContext(SynchronizationContext.ASYNC)
                transaction(Databases.databaseNetwork) {
                    it.delete()
                }
                switchContext(SynchronizationContext.SYNC)
            }
        }
    }

    @InternalCoroutinesApi
    @EventHandler
    fun onSignEdit(e: SignChangeEvent) {
        if (!e.lines[0].equals("[Votar]", true))
            return

        e.setLine(0, "§8[§a§lVotar§8]")
        e.setLine(1, "§cClique para votar")
        e.setLine(2, "§b${e.player.name}")

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            transaction(Databases.databaseNetwork) {
                VoteSign.new {
                    this.owner = e.player.uniqueId
                    this.setLocation(e.block.location)
                }
            }

            updateVoteSigns(e.player.uniqueId)

            switchContext(SynchronizationContext.SYNC)

            e.player.sendMessage("${DreamLoja.PREFIX} §aPlaca de votação criada com sucesso!")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        if (!e.block.type.name.endsWith("_SIGN"))
            return

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val hasVoteSign = transaction(Databases.databaseNetwork) {
                val voteSign = VoteSign.find {
                    (VoteSigns.x eq e.block.location.x) and
                            (VoteSigns.y eq e.block.location.y) and
                            (VoteSigns.z eq e.block.location.z) and
                            (VoteSigns.worldName eq e.block.world.name)
                }.firstOrNull()

                voteSign?.delete()

                return@transaction voteSign != null
            }

            switchContext(SynchronizationContext.SYNC)

            if (hasVoteSign)
                e.player.sendMessage("${DreamLoja.PREFIX} §cA placa de votação foi quebrada! Mas não se preocupe, os votos ainda estão firmes e fortes!")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onClick(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        if (e.clickedBlock?.type?.name?.endsWith("_SIGN") == false)
            return

        val block = e.clickedBlock ?: return

        val sign = block.state as Sign

        if (sign.lines[0] != "§8[§a§lVotar§8]")
            return

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val voteSign = transaction(Databases.databaseNetwork) {
                VoteSign.find {
                    (VoteSigns.x eq block.location.x) and
                            (VoteSigns.y eq block.location.y) and
                            (VoteSigns.z eq block.location.z) and
                            (VoteSigns.worldName eq block.world.name)
                }.firstOrNull()
            } ?: return@schedule // Não é uma placa de votação

            if (e.player.uniqueId == voteSign.owner) {
                e.player.sendMessage("${DreamLoja.PREFIX} §cVocê vê a sua placa de votação atentamente, e pensa... \"Um dia, eu serei um empreendedor incrível!\" e, como você respeita as regras dos comerciantes da LorittaLand, você sabe que não pode votar na sua própria loja.")
                return@schedule
            }

            val shopVote = transaction(Databases.databaseNetwork) {
                UserShopVote.find { UserShopVotes.givenBy eq e.player.uniqueId and (UserShopVotes.receivedBy eq voteSign.owner) }.firstOrNull()
            }

            if (shopVote != null) {
                val diff = System.currentTimeMillis() - shopVote.receivedAt

                if (300_000 > diff) {
                    e.player.sendMessage("${DreamLoja.PREFIX} §cVocê tem que esperar ${DateUtils.formatDateDiff(Calendar.getInstance(), Calendar.getInstance().apply { this.timeInMillis = shopVote.receivedAt })} antes de tirar o seu voto! Que tal conversar com o proprietário da loja para ver se vocês resolvem as suas diferenças?")
                    return@schedule
                }

                transaction(Databases.databaseNetwork) {
                    shopVote.delete()
                }

                updateVoteSigns(e.player.uniqueId)

                switchContext(SynchronizationContext.SYNC)

                e.player.sendMessage("${DreamLoja.PREFIX} §aVocê removeu o voto da loja d${MeninaAPI.getArtigo(voteSign.owner)} §b${Bukkit.getOfflinePlayer(voteSign.owner)?.name ?: "???"}§a...");
                e.player.sendMessage("${DreamLoja.PREFIX} §7Lembre-se, votos é algo que ajuda os donos das lojas, não seja vacilão para votar e depois tirar só para ser v1d4 l0k4.");
                return@schedule
            }

            transaction(Databases.databaseNetwork) {
                UserShopVote.new {
                    this.givenBy = e.player.uniqueId
                    this.receivedAt = System.currentTimeMillis()
                    this.receivedBy = voteSign.owner
                }
            }

            updateVoteSigns(voteSign.owner)

            switchContext(SynchronizationContext.SYNC)

            e.player.sendMessage("${DreamLoja.PREFIX} §aVocê votou na loja do §b${Bukkit.getOfflinePlayer(voteSign.owner)?.name ?: "???"}§a!")

            InstantFirework.spawn(block.location.add(0.0, 0.5, 0.0), FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL)
                    .withColor(Color.AQUA)
                    .flicker(true)
                    .build()
            )
        }
    }
}