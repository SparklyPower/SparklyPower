package net.perfectdreams.dreamcore.utils

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import java.io.File

object TranslationUtils {
    val localeStrings = mutableMapOf<String, Map<String, String>>()

    fun loadLocale(dataFolder: File, localeId: String) {
        val i18nFile = File(dataFolder, "i18n")

        val json = DreamUtils.jsonParser.parse(File(i18nFile, "$localeId.json").readText()).obj

        localeStrings[localeId] = json.entrySet().map { it.key to it.value.string }.toMap()
    }
}