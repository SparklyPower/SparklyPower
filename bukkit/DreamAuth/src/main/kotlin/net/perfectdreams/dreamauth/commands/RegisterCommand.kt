package net.perfectdreams.dreamauth.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.dao.AuthInfo
import net.perfectdreams.dreamauth.tables.AuthStorage
import net.perfectdreams.dreamauth.utils.ilike
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object RegisterCommand : DSLCommandBase<DreamAuth> {
    override fun command(plugin: DreamAuth) = this.create(listOf("register", "registrar")) {
        executes {
            val password1 = args.getOrNull(0)
            val password2 = args.getOrNull(0)

            if (password1 == null) {
                player.sendMessage(
                    generateCommandInfo(label,
                        mapOf("Senha1" to "Sua senha", "Senha2" to "Sua senha novamente"),
                        listOf(
                            "Não use senhas que você já usou em outros servidores!",
                            "Coloque caracteres especiais (como \$#@!%&*) para deixar a sua senha mais complexa!",
                            "Não coloque senhas simples como \"1234\" ou \"${player.name}\"!"
                        )
                    )
                )
                return@executes
            }

            plugin.checkIfRegistered(player)

            if (password2 == null) {
                player.sendMessage("§cVocê precisa confirmar a sua senha! Confirme ela utilizando §6/registrar $password1 $password1")
                return@executes
            }

            if (!plugin.isPasswordSecure(player, label, password1, password2))
                return@executes

            scheduler().schedule(plugin, SynchronizationContext.ASYNC) {
                val ipCount = transaction(Databases.databaseNetwork) {
                    AuthInfo.find {
                        AuthStorage.lastIp eq player.address.address.hostAddress
                    }.count()
                }

                if (ipCount > 3 && !player.address.address.hostAddress.startsWith("127.0.0.1")) { // PSPE
                    player.sendMessage("§cVocê já tem várias contas registradas no mesmo IP!")
                    return@schedule
                }

                val matchedUsers = transaction(Databases.databaseNetwork) {
                    val users = User.find {
                        Users.username ilike player.name
                            .replace("_", "\\_") // We need to escape _ pattern matching
                    }

                    AuthInfo.find {
                        AuthStorage.uniqueId inList users.map { it.id.value }
                    }.toMutableList()
                }

                if (matchedUsers.isNotEmpty()) {
                    player.sendMessage("§cVocê já registrou uma conta com esse nome!")
                    return@schedule
                }

                // Como criar uma senha com o BCrypt é pesado, vamos mudar para uma task async
                // gerar hashed password
                val salt = BCrypt.gensalt(DreamAuth.WORKLOAD)
                val hashed = BCrypt.hashpw(password1, salt)

                val authInfo = transaction(Databases.databaseNetwork) {
                    AuthInfo.new(player.uniqueId) {
                        password = hashed
                        lastIp = player.address.address.hostAddress
                        lastLogin = System.currentTimeMillis()
                        remember = false
                        twoFactorAuthEnabled = false
                    }
                }

                plugin.uniqueId2PlayerInfo[player.uniqueId] = authInfo

                switchContext(SynchronizationContext.SYNC)
                plugin.finishLogin(player)
            }
        }
    }
}