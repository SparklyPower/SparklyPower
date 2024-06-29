package net.perfectdreams.pantufa

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File
import kotlin.system.exitProcess

object PantufaLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		val configFile = File("./pantufa.conf")

		// This is just in case we can't find the configuration file, we will generate one.
		if (!configFile.exists()) {
			println("Welcome to Pantufa, Loritta's best friend and SparklyPower support! :3")
			println("")
			println("We share a dream to make the world a better place, I hope we succed!")
			println("")
			println("But first, you need to configure me! :D")
			println("I created a configuration file named \"pantufa.conf\" where you can configure a lot of things that I need to finally run!")
			println("")
			println("See you later.")

			copyFromJar("/pantufa.conf", "./pantufa.conf")

			exitProcess(1)
		}

		val pantufa = PantufaBot(
			Hocon.decodeFromConfig(
				ConfigFactory.parseFile(configFile)
			)
		)

		pantufa.start()
	}

	private fun copyFromJar(inputPath: String, outputPath: String) {
		val inputStream = PantufaLauncher::class.java.getResourceAsStream(inputPath)
		File(outputPath).writeBytes(inputStream.readAllBytes())
	}
}