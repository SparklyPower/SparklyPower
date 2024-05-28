package net.perfectdreams.pantufa.commands

import net.perfectdreams.pantufa.commands.server.*
import net.perfectdreams.pantufa.commands.server.administration.AdvDupeIpCommand
import net.perfectdreams.pantufa.commands.server.administration.BanCommand
import net.perfectdreams.pantufa.commands.server.administration.CheckBanCommand
import net.perfectdreams.pantufa.commands.server.administration.DupeIpCommand
import net.perfectdreams.pantufa.commands.server.administration.ExecuteCommand
import net.perfectdreams.pantufa.commands.server.administration.FingerprintCommand
import net.perfectdreams.pantufa.commands.server.administration.GeoIpCommand
import net.perfectdreams.pantufa.commands.server.administration.IpBanCommand
import net.perfectdreams.pantufa.commands.server.administration.IpUnbanCommand
import net.perfectdreams.pantufa.commands.server.administration.KickCommand
import net.perfectdreams.pantufa.commands.server.administration.TpsCommand
import net.perfectdreams.pantufa.commands.server.administration.UnbanCommand
import net.perfectdreams.pantufa.commands.server.administration.UnwarnCommand
import net.perfectdreams.pantufa.commands.server.administration.WarnCommand

class CommandManager {
	val commands = mutableListOf<AbstractCommand>()

	init {
		commands.add(OnlineCommand())
		commands.add(LSXCommand())
		commands.add(RegistrarCommand())
		commands.add(TpsCommand())
		commands.add(ExecuteCommand())

		commands.add(BanCommand())
		commands.add(UnbanCommand())
		commands.add(KickCommand())
		commands.add(WarnCommand())
		commands.add(UnwarnCommand())
		commands.add(CheckBanCommand())
		commands.add(AdvDupeIpCommand())
		commands.add(DupeIpCommand())
		commands.add(FingerprintCommand())
		commands.add(GeoIpCommand())

		commands.add(IpBanCommand())
		commands.add(IpUnbanCommand())
	}
}