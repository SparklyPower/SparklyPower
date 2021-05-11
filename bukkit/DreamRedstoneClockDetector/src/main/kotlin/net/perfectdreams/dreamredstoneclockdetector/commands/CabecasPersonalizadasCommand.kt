package net.perfectdreams.dreamvipstuff.commands

import com.destroystokyo.paper.profile.ProfileProperty
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamredstoneclockdetector.DreamVIPStuff
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jsoup.Jsoup
import java.util.*

object CabecasPersonalizadasCommand : DSLCommandBase<DreamVIPStuff> {
    const val PRICE = 10_000

    override fun command(plugin: DreamVIPStuff) = create(
        listOf("vipcabeças", "vipcabecas", "cabeçasvip", "cabecasvip", "vipcabeça", "vipcabeca", "cabeçavip", "cabecavip")
    ) {
        permission = "dreamcabecas.give"

        executes {
            val url = args.getOrNull(0)

            val count = Math.min(
                2304,
                Math.max(
                    args.getOrNull(1)?.toIntOrNull() ?: 1,
                    1
                )
            )

            val totalPrice = (PRICE * count)
            if (totalPrice > player.balance) {
                player.sendMessage("§cVocê precisa de 10k para cada cabeça que você deseja gerar!")
                return@executes
            }

            if (url != null) {
                if (!url.contains("://minecraft-heads.com/custom-heads/")) {
                    player.sendMessage("§6/vipcabeças url")
                    player.sendMessage("§cCada cabeça custa 10k sonhos")
                    player.sendMessage("§cEnvie URLs do https://minecraft-heads.com/ da seção de \"Custom Heads\"!")
                    return@executes
                }

                player.sendMessage("§ePegando dados da sua cabeça... Não, não é mágica, é só eu pegando da URL, e não lendo a sua mente... deixa, essa piada é muito ruim.")

                plugin.schedule(SynchronizationContext.ASYNC) {
                    val result = Jsoup.connect(url)
                        .execute()

                    val document = result.parse()
                    val uuidValue = document
                        .selectFirst("#UUID-Value")
                        .text()

                    val uuidTextureUrl = document
                        .selectFirst("#UUID-Skin")
                        .text()

                    switchContext(SynchronizationContext.SYNC)

                    if (totalPrice > player.balance) // Check again
                        return@schedule

                    player.sendMessage("§aCabeça criada com sucesso!")

                    val skull = ItemStack(Material.PLAYER_HEAD)

                    val newSkull = skull.meta<SkullMeta> {
                        this.playerProfile = Bukkit.createProfile(UUID.randomUUID()) // Pretty sure that the server doesn't care
                            .apply {
                                this.setProperty(
                                    ProfileProperty(
                                        "textures",
                                        uuidValue
                                    )
                                )
                            }
                    }

                    var totalGiven = 0

                    for (i in 1..count) {
                        if (player.inventory.canHoldItem(newSkull))
                            player.inventory.addItem(newSkull)
                        else
                            break
                        totalGiven += 1
                    }

                    player.balance -= totalGiven * 10_000

                    player.sendMessage("§aProntinho! Divirta-se com as suas novas $totalGiven cabeças personalizadas ^-^")
                }
            } else {
                player.sendMessage("§6/vipcabeças url")
                player.sendMessage("§cCada cabeça custa 10k sonhos")
                player.sendMessage("§cEnvie URLs do https://minecraft-heads.com/ da seção de \"Custom Heads\"!")
            }
        }
    }
}