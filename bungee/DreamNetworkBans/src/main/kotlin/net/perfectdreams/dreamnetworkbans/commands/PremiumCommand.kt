package net.perfectdreams.dreamnetworkbans.commands

import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.listeners.LoginListener
import net.perfectdreams.dreamnetworkbans.tables.PremiumUsers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class PremiumCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("premium")) {
	@Subcommand
	fun root(sender: ProxiedPlayer) {
		sender.sendTitle(
				m.proxy.createTitle()
						.title("§cAtenção!".toTextComponent())
						.subTitle("§cLeia o chat antes de ativar!".toTextComponent())
						.fadeIn(5)
						.stay(100)
						.fadeOut(5)
		)

		sender.sendMessage("§4§lATENÇÃO!".toTextComponent())
		sender.sendMessage("§cApenas utilize §6/premium§c se a sua conta for §4realmente§c uma conta original!".toTextComponent())
		sender.sendMessage("§cSe você ativar com uma conta não original, você não irá poder mais usar a sua conta!".toTextComponent())
		sender.sendMessage("§cNão tem Minecraft Original? Então compre para ajudar o desenvolvimento do Minecraft!§b https://www.minecraft.net/get-minecraft".toTextComponent())
		sender.sendMessage("§aCaso você esteja em uma conta original, use §6/premium ativar§a!".toTextComponent())
		sender.sendMessage("§aApós ativar, você será desconectado do servidor e não precisará mais usar senhas para logar!".toTextComponent())
		sender.sendMessage("§4§lNÃO ATIVE SE VOCÊ TEM CONTAS \"SEMI ACESSO\" OU CONTAS NÃO COMPRADAS PELO WEBSITE OFICIAL DA MOJANG!".toTextComponent())
		sender.sendMessage("§cContas compradas fora do website da Mojang são uma bomba relógio: A qualquer momento o dono original da conta pode pedir para a Mojang a conta de volta, e você irá perder a sua conta e o seu dinheiro!".toTextComponent())
	}

	@Subcommand
	fun root(sender: ProxiedPlayer, arg0: String) {
		if (arg0 == "ativar" || arg0 == "enable") {
			val userPremiumUniqueId = runBlocking {
				m.minecraftMojangApi.getUniqueId(sender.name)
			}

			if (userPremiumUniqueId == null) {
				sender.sendMessage("§cVocê não parece estar usando uma conta de Minecraft Original... Para evitar que você perca a sua conta no SparklyPower para sempre, eu irei apenas ignorar o que você pediu! ^-^".toTextComponent())
				return
			}

			transaction(Databases.databaseNetwork) {
				if (PremiumUsers.select { PremiumUsers.crackedUniqueId eq sender.uniqueId }.count() != 0L) {
					sender.sendMessage("§cVocê já está usando uma conta premium!")
					return@transaction
				}

				PremiumUsers.insert {
					it[crackedUniqueId] = sender.uniqueId
					it[premiumUniqueId] = userPremiumUniqueId
					it[crackedUsername] = sender.name
				}

				sender.disconnect("§eSua conta foi marcada como premium, sua conta está mais segura e você não irá precisar logar no servidor manualmente!\n\n§aApenas entre novamente no servidor e divirta-se!\n\n§aObrigado por suportar o desenvolvimento do Minecraft! §d^-^".toTextComponent())
			}
		} else {
			sender.sendMessage("§cEscreva §6/premium ativar§c para ativar a verificação de conta premium da sua conta!"
					.toTextComponent())
		}
	}
}