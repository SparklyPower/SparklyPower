package net.perfectdreams.dreamauth.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import org.apache.commons.lang3.RandomStringUtils
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class RecuperarCommand(val m: DreamAuth) : SparklyCommand(arrayOf("recovery", "recuperar")) {
	@Subcommand
	fun email(player: Player) {
		player.sendMessage(
				generateCommandInfo("recovery",
						mapOf("Email" to "Seu email"),
						listOf(
								"É necessário saber o email que você utilizou na hora de registrar para confirmar que realmente é você!"
						)
				)
		)
	}

	@Subcommand
	fun handleEmail(player: Player, email: String) {
		m.checkIfNotRegistered(player)
		val authInfo = m.uniqueId2PlayerInfo[player.uniqueId] ?: return

		val storedEmail = authInfo.email
		if (storedEmail == null) {
			player.sendMessage("§cVocê não associou nenhum email para essa conta! Você não poderá recuperar ela...");
			player.sendMessage("");
			player.sendMessage("§cSerá que todos os avisos pedindo para colocar o seu email não foram suficientes?");
			return
		}

		if (!email.contains("@")) {
			player.sendMessage("§c$email não é um email válido!")
			return
		}

		if (email != storedEmail) {
			player.sendMessage("§cO email registrado nesta conta não é o mesmo email registrado na conta!")
			return
		}

		val requestedPasswordChangeAt = authInfo.requestedPasswordChangeAt ?: 0

		if (60000 > System.currentTimeMillis() - requestedPasswordChangeAt) {
			player.sendMessage("§cUm email de recuperação já foi enviado para você! Verifique a sua caixa de entrada novamente! Não encontrou? Então tente verificar a sua caixa de spam!");
			return
		}

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			val token = RandomStringUtils.random(32, 0, 62, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray());

			transaction(Databases.databaseNetwork) {
				authInfo.token = token
				authInfo.requestedPasswordChangeAt = System.currentTimeMillis()
			}

			switchContext(SynchronizationContext.SYNC)

			player.sendMessage("§3Enviando email para §9$email§3...")
			switchContext(SynchronizationContext.ASYNC)

			/* val message = Message().addTo(email)
					.setFrom("no-reply@perfectdreams.net")
					.setSubject("Solicitação de Mudança de Senha")
					.setHtml("""<body>
<div style="background:#5280a6">
<div style="background:url('https://perfectdreams.net/assets/img/website_bg.png');">
<div style="margin:0px auto;max-width:640px;background:transparent"><table role="presentation" cellpadding="0" cellspacing="0" style="font-size:0px;width:100%;background:transparent" align="center" border="0"><tbody><tr><td style="text-align:center;vertical-align:top;direction:ltr;font-size:0px;padding:10px 0px"><div aria-labelledby="mj-column-per-100" class="m_47973453007754971mj-column-per-100 m_47973453007754971outlook-group-fix" style="vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%"><table role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0"><tbody><tr><td style="word-break:break-word;font-size:0px;padding:0px" align="center"><table role="presentation" cellpadding="0" cellspacing="0" style="border-collapse:collapse;border-spacing:0px" align="center" border="0"><tbody><tr><td style="width:138px"><a href="https://perfectdreams.net/" target="_blank"><img alt="" title="" src="https://perfectdreams.net/assets/img/perfectdreams_logo.png" width="600"></a></td></tr></tbody></table></td></tr></tbody></table></div></td></tr></tbody></table></div>
<div style="max-width:640px;margin:0 auto;border-radius:4px;overflow:hidden"><div style="margin:0px auto;max-width:640px;background:#ffffff"><table role="presentation" cellpadding="0" cellspacing="0" style="font-size:0px;width:100%;background:#ffffff" align="center" border="0"><tbody><tr><td style="text-align:center;vertical-align:top;direction:ltr;font-size:0px;padding:40px 70px"><div aria-labelledby="mj-column-per-100" class="m_47973453007754971mj-column-per-100 m_47973453007754971outlook-group-fix" style="vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%"><table role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0"><tbody><tr><td style="word-break:break-word;font-size:0px;padding:0px" align="left"><div style="color:#737f8d;font-family:Whitney,Helvetica Neue,Helvetica,Arial,Lucida Grande,sans-serif;font-size:16px;line-height:24px;text-align:left">
<h2 style="font-family:Whitney,Helvetica Neue,Helvetica,Arial,Lucida Grande,sans-serif;font-weight:500;font-size:20px;color:#4f545c;letter-spacing:0.27px">Olá ${player.name},</h2>
<p>Nós recebemos um pedido para recuperar a sua conta, para recuperar ela, clique no link abaixo.</p>
<p>Não foi você quem pediu para recuperar a sua conta? Fique tranquilo, apenas ignore o email e envie para a nossa equipe o IP de quem pediu para recuperar a conta. 😉</p>
<p>IP: ${player.address.address.hostAddress}</p>
<p>Localização: São Paulo</p>
</div></td></tr><tr><td style="word-break:break-word;font-size:0px;padding:10px 25px;padding-top:20px" align="center"><table role="presentation" cellpadding="0" cellspacing="0" style="border-collapse:separate" align="center" border="0"><tbody><tr><td style="border:none;border-radius:3px;color:white;padding:15px 19px" align="center" valign="middle" bgcolor="#02afd4"><a href="https://perfectdreams.net/auth/recovery?account=${player.name}?token=$token" style="text-decoration:none;line-height:100%;background:#02afd4;color:white;font-family:Ubuntu,Helvetica,Arial,sans-serif;font-size:15px;font-weight:normal;text-transform:none;margin:0px">
Clique aqui para recuperar a sua conta
</a></td></tr></tbody></table></td></tr></tbody></table></div></td></tr></tbody></table></div>
 
</br>
 
</br>
 
</div></div></div></body>""")

			m.service.sendEmail(message)

			switchContext(SynchronizationContext.SYNC)

			player.sendMessage("§aO link para recuperar a sua senha foi enviado via email para a caixa de entrada do seu email!")
			player.sendMessage("§aUse ela para recuperar a sua senha, após recuperar, entre na conta utilizando §6/login NovaSenha") */
		}
	}
}