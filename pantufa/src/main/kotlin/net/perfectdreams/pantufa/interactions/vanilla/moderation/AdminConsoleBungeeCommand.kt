package net.perfectdreams.pantufa.interactions.vanilla.moderation

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.pantufa.interactions.vanilla.moderation.stuff.AdminConsoleUtils
import net.perfectdreams.pantufa.utils.Server

class AdminConsoleBungeeCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("adminconsole", "Executa alguma coisa no console.", CommandCategory.MODERATION) {
        enableLegacyMessageSupport = true
        requireMinecraftAccount = true

        subcommand("advdupeip", "Executa advdupeip") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("advdupeip")
            }

            executor = AdvDupeIpExecutor()
        }

        subcommand("ban", "Executa ban") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ban")
            }

            executor = BanExecutor()
        }

        subcommand("checkban", "Executa checkban") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("checkban")
            }

            executor = CheckBanExecutor()
        }

        subcommand("dupeip", "Executa dupeip") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("dupeip")
            }

            executor = DupeIpExecutor()
        }

        subcommand("fingerprint", "Executa fingerprint") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("fingerprint")
            }

            executor = FingerprintExecutor()
        }

        subcommand("geoip", "Executa geoip") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("geoip")
            }

            executor = GeoIpExecutor()
        }

        subcommand("ipban", "Executa ipban") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ipban")
            }

            executor = IpBanExecutor()
        }

        subcommand("ipunban", "Executa ipunban") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ipunban")
            }

            executor = IpUnbanExecutor()
        }

        subcommand("kick", "Executa kick") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("kick")
            }

            executor = KickExecutor()
        }

        subcommand("unban", "Executa unban") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("unban")
            }

            executor = UnbanExecutor()
        }

        subcommand("warn", "Executa warn") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("warn")
            }

            executor = WarnExecutor()
        }

        subcommand("unwarn", "Executa unwarn") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("unwarn")
            }

            executor = UnwarnExecutor()
        }
    }

    inner class AdvDupeIpExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.advdupeip",
        "advdupeip",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class BanExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.ban",
        "ban",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class CheckBanExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.checkban",
        "checkban",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class DupeIpExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.dupeip",
        "dupeip",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class FingerprintExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.fingerprint",
        "fingerprint",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class GeoIpExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.geoip",
        "geoip",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class IpBanExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.ipban",
        "ipban",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class IpUnbanExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.ipunban",
        "ipunban",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class KickExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.kick",
        "kick",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class UnbanExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.unban",
        "unban",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class WarnExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.warn",
        "warn",
        Server.PERFECTDREAMS_BUNGEE
    )

    inner class UnwarnExecutor : AdminConsoleUtils.AdminConsoleBungeeExecutor(
        "dreamnetworkbans.unwarn",
        "unwarn",
        Server.PERFECTDREAMS_BUNGEE
    )
}