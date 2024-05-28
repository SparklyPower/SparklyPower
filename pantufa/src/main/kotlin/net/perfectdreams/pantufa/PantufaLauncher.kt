package net.perfectdreams.pantufa

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File

object PantufaLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		val pantufa = PantufaBot(
			Hocon.decodeFromConfig(
				ConfigFactory.parseFile(
					File(
						"./pantufa.conf"
					)
				)
			)
		)

		pantufa.start()
	}
}