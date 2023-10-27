package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Duration

class PremiumCommand(val m: SparklyNeonVelocity) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source() as Player
        val arguments = invocation.arguments()
        val arg0 = arguments.firstOrNull()

        if (arg0 == "ativar" || arg0 == "enable") {
            val userPremiumUniqueId = runBlocking { m.minecraftMojangApi.getUniqueId(sender.username) }

            if (userPremiumUniqueId == null) {
                sender.sendMessage("§cVocê não parece estar usando uma conta de Minecraft Original... Para evitar que você perca a sua conta no SparklyPower para sempre, eu irei apenas ignorar o que você pediu! ^-^".fromLegacySectionToTextComponent())
                return
            }

            runBlocking {
                m.pudding.transaction {
                    if (PremiumUsers.select { PremiumUsers.crackedUniqueId eq sender.uniqueId }.count() != 0L) {
                        sender.sendMessage("§cVocê já está usando uma conta premium!".fromLegacySectionToTextComponent())
                        return@transaction
                    }

                    PremiumUsers.insert {
                        it[crackedUniqueId] = sender.uniqueId
                        it[premiumUniqueId] = userPremiumUniqueId
                        it[crackedUsername] = sender.username
                    }

                    sender.disconnect("§eSua conta foi marcada como premium, sua conta está mais segura e você não irá precisar logar no servidor manualmente!\n\n§aApenas entre novamente no servidor e divirta-se!\n\n§aObrigado por suportar o desenvolvimento do Minecraft! §d^-^".fromLegacySectionToTextComponent())
                }
            }
        }


        sender.showTitle(
            Title.title(
                "§cAtenção!".fromLegacySectionToTextComponent(),
                "§cLeia o chat antes de ativar!".fromLegacySectionToTextComponent(),
                Title.Times.times(
                    Duration.ofMillis(250),
                    Duration.ofMillis(5_000),
                    Duration.ofMillis(250)
                )
            )
        )

        sender.sendMessage("§4§lATENÇÃO!".fromLegacySectionToTextComponent())
        sender.sendMessage("§cApenas utilize §6/premium§c se a sua conta for §4realmente§c uma conta original!".fromLegacySectionToTextComponent())
        sender.sendMessage("§cSe você ativar com uma conta não original, você não irá poder mais usar a sua conta!".fromLegacySectionToTextComponent())
        sender.sendMessage("§cNão tem Minecraft Original? Então compre para ajudar o desenvolvimento do Minecraft!§b https://www.minecraft.net/get-minecraft".fromLegacySectionToTextComponent())
        sender.sendMessage("§aCaso você esteja em uma conta original, use §6/premium ativar§a!".fromLegacySectionToTextComponent())
        sender.sendMessage("§aApós ativar, você será desconectado do servidor e não precisará mais usar senhas para logar!".fromLegacySectionToTextComponent())
        sender.sendMessage("§4§lNÃO ATIVE SE VOCÊ TEM CONTAS \"SEMI ACESSO\" OU CONTAS NÃO COMPRADAS PELO WEBSITE OFICIAL DA MOJANG!".fromLegacySectionToTextComponent())
        sender.sendMessage("§cContas compradas fora do website da Mojang são uma bomba relógio: A qualquer momento o dono original da conta pode pedir para a Mojang a conta de volta, e você irá perder a sua conta e o seu dinheiro!".fromLegacySectionToTextComponent())
    }
}