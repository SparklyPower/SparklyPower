package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.administration.*

class AdminConsoleBungeeCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "adminconsole",
        "Executa algo no console"
    ) {
        subcommand("advdupeip", "Executa addupeip") {
            executor = AdvDupeIpExecutor(m)
        }

        subcommand("ban", "Executa ban") {
            executor = BanExecutor(m)
        }

        subcommand("checkban", "Executa checkban") {
            executor = CheckBanExecutor(m)
        }

        subcommand("dupeip", "Executa dupeip") {
            executor = DupeIpExecutor(m)
        }

        subcommand("fingerprint", "Executa fingerprint") {
            executor = FingerprintExecutor(m)
        }

        subcommand("geoip", "Executa geoip") {
            executor = GeoIpExecutor(m)
        }

        subcommand("ipban", "Executa ipban") {
            executor = IpBanExecutor(m)
        }

        subcommand("ipunban", "Executa ipunban") {
            executor = IpUnbanExecutor(m)
        }

        subcommand("kick", "Executa kick") {
            executor = KickExecutor(m)
        }

        subcommand("unban", "Executa unban") {
            executor = UnbanExecutor(m)
        }

        subcommand("warn", "Executa warn") {
            executor = UnbanExecutor(m)
        }

        subcommand("unwarn", "Executa unwarn") {
            executor = UnbanExecutor(m)
        }
    }
}