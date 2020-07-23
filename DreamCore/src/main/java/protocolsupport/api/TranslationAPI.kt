package protocolsupport.api

// WORKAROUND DO NOT USE
object TranslationAPI {
    fun getTranslationString(str: String) = str
    fun getTranslationString(locale: String, str: String) = str
}