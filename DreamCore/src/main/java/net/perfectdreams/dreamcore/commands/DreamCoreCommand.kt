package net.perfectdreams.dreamcore.commands

import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.File
import kotlin.concurrent.thread

class DreamCoreCommand(val m: DreamCore) : SparklyCommand(arrayOf("dreamcore"), permission = "dreamcore.setup") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§aDreamCore! Powered by PerfectDreams' Command Framework :3")
	}

	@Subcommand(["set_spawn"])
	fun setSpawn(sender: Player) {
		DreamCore.dreamConfig.spawn = sender.location
		m.config.set("spawn-location", sender.location)
		m.saveConfig()

		sender.sendMessage("§aSpawn atualizado!")
	}

	@Subcommand(["reload"])
	fun reload(sender: CommandSender, fileName: String) {
		if (fileName == "all") {
			sender.sendMessage("§aRecarregando TODOS os scripts!")
			m.dreamScriptManager.unloadScripts()
			m.dreamScriptManager.loadScripts()
			sender.sendMessage("§aProntinho! ${m.dreamScriptManager.scripts.size} scripts foram carregados ^-^")
		} else {
			val script = m.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
			sender.sendMessage("§aRecarregando script $fileName!")
			if (script != null)
				m.dreamScriptManager.unloadScript(script)
			try {
				m.dreamScriptManager.loadScript(File(m.dataFolder, "scripts/$fileName"), true)
				sender.sendMessage("§aProntinho! $fileName foi carregado com sucesso!")
			} catch (e: Exception) {
				sender.sendMessage("§cAlgo deu errado ao carregar $fileName! ${e.message}")
			}
		}
	}

	@Subcommand(["unload"])
	fun unload(sender: CommandSender, fileName: String) {
		if (fileName == "all") {
			sender.sendMessage("§Descarregando TODOS os scripts!")
			m.dreamScriptManager.unloadScripts()
			sender.sendMessage("§aProntinho! Todos os scripts foram descarregados ^-^")
		} else {
			val script = m.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
			if (script == null) {
				sender.sendMessage("§cO script ${fileName} existe! Use reload seu tosco!")
				return
			}

			sender.sendMessage("§aDescarregando script $fileName!")
			m.dreamScriptManager.unloadScript(script)
			sender.sendMessage("§aProntinho! $fileName foi descarregado com sucesso!")
		}
	}

	@Subcommand(["eval"])
	fun eval(sender: Player) {
		val heldItem = sender.inventory.itemInMainHand

		if (heldItem.type != Material.WRITABLE_BOOK && heldItem.type != Material.WRITTEN_BOOK) {
			throw ExecutedCommandException("§cVocê precisa estar segurando um livro!")
		}

		val bookMeta = heldItem.itemMeta as BookMeta
		val lines = bookMeta.pages.map { it.stripColorCode() }.joinToString("\n")

		sender.sendMessage("§dExecutando...")
		sender.sendMessage(lines)

		val content = """
			${Imports.IMPORTS}

			class EvaluatedCode {
				fun doStuff(player: Player) {
					${lines}
				}
			}

			EvaluatedCode()
		""".trimIndent()

		try {
			val result = DreamScriptManager.evaluate<Any>(m, content)
			result::class.java.getMethod("doStuff", Player::class.java).invoke(result, sender)
		} catch (e: Exception) {
			e.printStackTrace()
			sender.sendMessage("§dDeu ruim! ${e.message}")
		}
	}

	@Subcommand(["plreload"])
	fun pluginReload(sender: Player, pluginName: String, branch: String = "development") {
		// A maior gambiarra já vista na face da terra:tm:
		// Iremos compilar e recarregar o plugin automaticamente, magicamente!
		thread {
			val outputFolder = File("/home/dreamcore_compile/$pluginName")
			val targetFolder = File("/home/dreamcore_compile/$pluginName/target")

			if (targetFolder.exists()) {
				targetFolder.listFiles().forEach {
					if (it.extension == "jar")
						it.delete()
				}
			}

			if (!outputFolder.exists()) {
				sender.sendMessage("§aClonando projeto $pluginName...")
				val processBuilder = ProcessBuilder("git", "clone", "https://github.com/PerfectDreams/$pluginName.git")
						.redirectErrorStream(true)
						.directory(File("/home/dreamcore_compile/"))

				val process = processBuilder.start()
				readProcessInput(process, sender)
				process.waitFor()
			}

			run {
				sender.sendMessage("§aFetcheando a origin de $pluginName...")
				val processBuilder = ProcessBuilder("git", "fetch", "origin")
						.redirectErrorStream(true)
						.directory(outputFolder)

				val process = processBuilder.start()
				readProcessInput(process, sender)
				process.waitFor()
			}

			run {
				sender.sendMessage("§aResetando para a origin/$branch de $pluginName...")
				val processBuilder = ProcessBuilder("git", "reset", "--hard", "origin/$branch")
						.redirectErrorStream(true)
						.directory(outputFolder)

				val process = processBuilder.start()
				readProcessInput(process, sender)
				process.waitFor()
			}

			run {
				sender.sendMessage("§aCompilando $pluginName...")
				val processBuilder = ProcessBuilder("mvn", "package")
						.redirectErrorStream(true)
						.directory(outputFolder)

				val process = processBuilder.start()
				readProcessInput(process, sender)
				process.waitFor()
			}

			run {
				sender.sendMessage("§aCopiando target do projeto para a pasta de plugins do servidor...")
				if (targetFolder.exists()) {
					val jar = targetFolder.listFiles().firstOrNull {it.extension == "jar" }

					if (jar == null) {
						sender.sendMessage("§cJAR não existe!")
						return@thread
					} else {
						val output = File("./plugins/$pluginName.jar")
						output.delete()
						jar.copyTo(output, true)
					}
				} else {
					sender.sendMessage("§cTarget folder não existe!")
					return@thread
				}
			}

			run {
				if (Bukkit.getPluginManager().getPlugin(pluginName) == null) {
					sender.sendMessage("§aCarregando $pluginName...")
					scheduler().schedule(m) {
						Bukkit.dispatchCommand(sender, "plugman load $pluginName.jar")
					}
				} else {
					sender.sendMessage("§aRecarregando $pluginName...")
					scheduler().schedule(m) {
						Bukkit.dispatchCommand(sender, "plugman reload $pluginName")
					}
				}
			}
		}
	}

	fun readProcessInput(process: Process, executor: CommandSender) {
		val reader = process.inputStream.bufferedReader()

		var line : String? = ""
		while (line != null) {
			line = reader.readLine()
			executor.sendMessage(line)
		}
	}
}