package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import net.perfectdreams.pantufa.utils.DateUtils
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.selectAll

class VIPInfoCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("vipinfo", "Veja as informações sobre o seu VIP!", CommandCategory.MINECRAFT) {
        enableLegacyMessageSupport = true
        requireMinecraftAccount = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("vip")
            add("vipinfo")
        }

        executor = VIPInfoCommandExecutor()
    }

    inner class VIPInfoCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val accountInfo = context.retrieveConnectedMinecraftAccount()!!
            val playerUniqueId = accountInfo.uniqueId
            val userPerms = context.pantufa.transactionOnLuckPermsDatabase {
                LuckPermsUserPermissions.selectAll().where {
                    LuckPermsUserPermissions.uuid eq playerUniqueId.toString()
                }.toList()
            }

            val vipPlusPlusPermission = userPerms.firstOrNull { it[LuckPermsUserPermissions.permission] == "group.vip++" }
            val vipPlusPermission = userPerms.firstOrNull { it[LuckPermsUserPermissions.permission] == "group.vip+" }
            val vipPermission = userPerms.firstOrNull { it[LuckPermsUserPermissions.permission] == "group.vip" }

            val data = when {
                vipPlusPlusPermission != null -> Pair("VIP++", vipPlusPlusPermission[LuckPermsUserPermissions.expiry])
                vipPlusPermission != null -> Pair("VIP+", vipPlusPermission[LuckPermsUserPermissions.expiry])
                vipPermission != null -> Pair("VIP", vipPermission[LuckPermsUserPermissions.expiry])
                else -> null
            }

            if (data != null && System.currentTimeMillis() >= (data.second * 1000)) {
                context.reply(false) {
                    styled(
                        "Seu **${data.first}** irá expirar em *${
                            DateUtils.formatDateDiff(data.second.toLong() * 1000)
                        }*",
                        "<a:cooldoge:543220304223404042>"
                    )
                }
            } else {
                context.reply(false) {
                    styled(
                        "Você não tem nenhum **VIP** no momento... Então que tal comprar pesadelos em nossa loja para você poder comprar um VIP lindíssimo? https://sparklypower.net/loja",
                        "<:pantufa_sob:1009905676925026325>"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
}