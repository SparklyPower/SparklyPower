package protocolsupport.api

import net.perfectdreams.dreamcore.utils.TranslationUtils

// WORKAROUND DO NOT USE
object TranslationAPI {
    fun getTranslationString(str: String) = TranslationUtils.localeStrings["pt_br"]!![str] ?: str
    fun getTranslationString(locale: String, str: String) = TranslationUtils.localeStrings[locale.toLowerCase()]!![str]
}