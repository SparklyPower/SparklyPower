package net.perfectdreams.dreamcore.scriptmanager

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.KtsObjectLoader
import org.bukkit.plugin.Plugin
import java.io.File

class DreamScriptManager(val m: DreamCore) {
	val scripts = mutableListOf<DreamScript>()
	val scriptsFolder = File(m.dataFolder, "scripts")
	val scriptTemplateFile = File(m.dataFolder, "template.kts")

	fun loadScripts() {
		m.logger.info("Carregando DreamScripts...")

		scriptsFolder.mkdirs()
		scriptsFolder.listFiles().forEach {
			if (it.extension == "kt" || it.extension == "kts") {
				loadScript(it)
			}
		}
		m.logger.info("DreamScripts carregados com sucesso! ${scripts.size} scripts foram carregados!")
	}

	fun loadScript(file: File, throwException: Boolean = false) {
		System.setProperty("idea.use.native.fs.for.win", "false") // Necessário para não ficar dando problemas no Windows

		m.logger.info("Carregando DreamScript \"${file.name}\"...")

		if (!scriptTemplateFile.exists()) {
			m.logger.warning("Arquivo \"template.kts\" não existe!")
			return
		}

		val templateContent = scriptTemplateFile.readText()
		val _content = file.readText()
		var content = _content
		if (file.extension == "kts") {
			content = "override fun enable() {\n$_content\n}"
		}
		val className = file.nameWithoutExtension.replace("_", "").toLowerCase().capitalize()
		val script = templateContent.replace("{{ code }}", content).replace("{{ className }}", className)

		val cl = m.javaClass.classLoader
		Thread.currentThread().contextClassLoader = cl

		try {
			val dreamScript = evaluate<DreamScript>(m, script)
			dreamScript.fileName = file.name
			dreamScript.enableScript()
			scripts.add(dreamScript)
		} catch (e: Exception) {
			if (throwException)
				throw e
			m.logger.warning("Erro ao carregar o script  \"${file.name}\"!")
			e.printStackTrace()
		}
	}

	fun unloadScripts() {
		scripts.forEach {
			unloadScript(it, false)
		}
		scripts.clear()
	}

	fun unloadScript(script: DreamScript, removeFromList: Boolean = true) {
		script.disableScript()
		if (removeFromList)
			scripts.remove(script)
	}

	companion object {
		inline fun <reified T> evaluate(plugin: Plugin, code: String): T {
			// Necessário para encontrar as classes
			val cl = plugin.javaClass.classLoader
			Thread.currentThread().contextClassLoader = cl

			val pluginsFolder = File("./plugins").listFiles().filter { it.extension == "jar" }.joinToString(File.pathSeparator, transform = { "plugins/${it.name}" })
			val propClassPath = "cache/patched_1.15.1.jar${File.pathSeparator}$pluginsFolder"

			System.setProperty("kotlin.script.classpath", propClassPath)

			return KtsObjectLoader().load<T>(code)
		}
	}
}