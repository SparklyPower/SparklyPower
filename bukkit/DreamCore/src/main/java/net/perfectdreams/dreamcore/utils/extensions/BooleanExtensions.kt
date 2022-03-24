package net.perfectdreams.dreamcore.utils.extensions

fun Boolean.humanize(mode: Mode, addSpace: Boolean = false): String {
    val string = when (mode) {
        Mode.ACTIVATED -> if (this) "ativado" else "desativado"
        Mode.YES_OR_NO -> if (this) "sim" else "não"
        Mode.YES_OR_NOTHING -> if (this) "sim" else ""
        Mode.NO_OR_NOTHING -> if (!this) "não" else ""
        Mode.TRUE_OR_FALSE -> if (this) "verdadeiro" else "falso"
    }
    return string + if (addSpace && string.isNotEmpty()) " " else ""
}

enum class Mode {
    ACTIVATED,
    YES_OR_NO,
    YES_OR_NOTHING,
    NO_OR_NOTHING,
    TRUE_OR_FALSE,
}