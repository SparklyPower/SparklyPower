package net.perfectdreams.dreamauth.commands

import com.github.salomonbrys.kotson.jsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import org.mindrot.jbcrypt.BCrypt

object LoginCommand : DSLCommandBase<DreamAuth> {
    override fun command(plugin: DreamAuth) = this.create(listOf("login", "logar")) {
        executes {
            val password = args.getOrNull(0)

            if (password == null) {
                player.sendMessage(
                    generateCommandInfo("login",
                        mapOf("Senha" to "A senha da sua conta")
                    )
                )
                return@executes
            }

            val storedPassword = plugin.checkIfNotRegistered(player)

            scheduler().schedule(plugin, SynchronizationContext.ASYNC) {
                // Como verificar uma senha com o BCrypt é pesado, vamos mudar para uma task async
                val correctPassword = BCrypt.checkpw(password, storedPassword)

                if (!correctPassword) {
                    switchContext(SynchronizationContext.SYNC)
                    player.sendMessage("§cSenha incorreta! Verifique se você não colocou nada errado e se o caps lock está desativado!")
                    player.sendMessage("§c")
                    player.sendMessage("§cEsqueceu a sua senha? Então use §6/recuperar email§c para recuperar a sua senha utilizando seu email!")

                    val count = plugin.wrongPasswordCount.getOrDefault(player.address.address, 0) + 1
                    plugin.wrongPasswordCount[player.address.address] = count

                    if (count == 25) { // Geyser
                        plugin.logger.info { "$player errou a senha vezes demais! Irei banir ele..." }

                        DreamNetwork.PANTUFA.sendMessageAsync(
                            "477902981606408222",
                            """<a:yoshi_pulsando:594962593161150483> **|** **`${player.name}`** errou a senha ao logar! Ele errou mais de 25 vezes, então irei banir ele! Nesta tentativa, tentou usar a senha: `$password`)
						  |<:lori_morre_diabo:540656812836519936> **|** **IP do usuário:** ${player.address.address.hostAddress}
						""".trimMargin()
                        )

                        DreamNetwork.PERFECTDREAMS_BUNGEE.sendAsync(
                            jsonObject(
                                "type" to "executeCommand",
                                "command" to "ipban ${player.address.address.hostAddress} Tentar invadir contas de outros players, errou a senha da conta mais de 10 vezes!"
                            )
                        )
                    }

                    if (count >= 3) {
                        DreamNetwork.PANTUFA.sendMessageAsync(
                            "477902981606408222",
                            """<:tobias_nosa:450476856303419432> **|** **`${player.name}`** errou a senha ao logar! (Já tentou ${count} vezes, nesta tentativa, tentou usar a senha: `$password`)
						  |<:lori_morre_diabo:540656812836519936> **|** **IP do usuário:** ${player.address.address.hostAddress}
						""".trimMargin()
                        )
                        return@schedule
                    }
                    return@schedule
                }

                val authInfo = plugin.uniqueId2PlayerInfo[player.uniqueId]!!
                val hasTwoFactorAuth = authInfo.twoFactorAuthEnabled

                if (false) {
                    plugin.playerStatus[player] = PlayerStatus.TWO_FACTOR_AUTH // we are on two factor auth bois
                    player.sendMessage("§aPara continuar o login, abra o seu aplicativo de autenticação e use §6/2fa code CódigoDeAutenticação§a!")
                    while (player.isOnline) {
                        val playerStatus = plugin.playerStatus[player] ?: return@schedule
                        if (playerStatus != PlayerStatus.TWO_FACTOR_AUTH)
                            return@schedule

                        player.sendTitle("§aAutenticação de Duas Etapas", "§6/2fa code CódigoDeAutenticação", 0, 60, 10)
                        waitFor(20)
                    }
                    return@schedule
                }

                switchContext(SynchronizationContext.SYNC)
                plugin.finishLogin(player)
            }
        }
    }
}