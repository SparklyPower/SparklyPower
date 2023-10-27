package net.sparklypower.common.utils

import java.text.DecimalFormat

object TextUtils {
    val ROUND_TO_2_DECIMAL = DecimalFormat("##.##")
    val ROUND_TO_1_DECIMAL = DecimalFormat("##.##")

    fun String.stripCodeMarks(): String {
        return this.replace("`", "")
    }

    fun Int.convertToNumeroNomeAdjetivo(): String? {
        when (this) {
            1 -> {
                return "Primeiro"
            }
            2 -> {
                return "Segundo"
            }
            3 -> {
                return "Terceiro"
            }
            4 -> {
                return "Quarto"
            }
            5 -> {
                return "Quinto"
            }
            6 -> {
                return "Sexto"
            }
            7 -> {
                return "S\u00e9timo"
            }
            8 -> {
                return "Oitavo"
            }
            9 -> {
                return "Nono"
            }
            10 -> {
                return "D\u00e9cimo"
            }
            11 -> {
                return "D\u00e9cimo primeiro"
            }
            12 -> {
                return "D\u00e9cimo segundo"
            }
            13 -> {
                return "D\u00e9cimo terceiro"
            }
            14 -> {
                return "D\u00e9cimo quarto"
            }
            15 -> {
                return "D\u00e9cimo quinto"
            }
            16 -> {
                return "D\u00e9cimo sexto"
            }
            17 -> {
                return "D\u00e9cimo s\u00e9timo"
            }
            18 -> {
                return "D\u00e9cimo oitavo"
            }
            19 -> {
                return "D\u00e9cimo nono"
            }
            20 -> {
                return "Vig\u00e9simo"
            }
            21 -> {
                return "Vig\u00e9simo primeiro"
            }
            22 -> {
                return "Vig\u00e9simo segundo"
            }
            23 -> {
                return "Vig\u00e9simo terceiro"
            }
            24 -> {
                return "Vig\u00e9simo quarto"
            }
            25 -> {
                return "Vig\u00e9simo quinto"
            }
            26 -> {
                return "Vig\u00e9simo sexto"
            }
            27 -> {
                return "Vig\u00e9simo s\u00e9timo"
            }
            28 -> {
                return "Vig\u00e9simo oitavo"
            }
            29 -> {
                return "Vig\u00e9simo nono"
            }
            30 -> {
                return "Trig\u00e9simo"
            }
            31 -> {
                return "Trig\u00e9simo primeiro"
            }
            32 -> {
                return "Trig\u00e9simo segundo"
            }
            33 -> {
                return "Trig\u00e9simo terceiro"
            }
            34 -> {
                return "Trig\u00e9simo quarto"
            }
            35 -> {
                return "Trig\u00e9simo quinto"
            }
            36 -> {
                return "Trig\u00e9simo sexto"
            }
            37 -> {
                return "Trig\u00e9simo s\u00e9timo"
            }
            38 -> {
                return "Trig\u00e9simo oitavo"
            }
            39 -> {
                return "Trig\u00e9simo nono"
            }
            40 -> {
                return "Quadrag\u00e9simo"
            }
            41 -> {
                return "Quadrag\u00e9simo primeiro"
            }
            42 -> {
                return "Quadrag\u00e9simo segundo"
            }
            43 -> {
                return "Quadrag\u00e9simo terceiro"
            }
            44 -> {
                return "Quadrag\u00e9simo quarto"
            }
            45 -> {
                return "Quadrag\u00e9simo quinto"
            }
            46 -> {
                return "Quadrag\u00e9simo sexto"
            }
            47 -> {
                return "Quadrag\u00e9simo s\u00e9timo"
            }
            48 -> {
                return "Quadrag\u00e9simo oitavo"
            }
            49 -> {
                return "Quadrag\u00e9simo nono"
            }
            50 -> {
                return "Quinquag\u00e9simo"
            }
            51 -> {
                return "Quinquag\u00e9simo primeiro"
            }
            52 -> {
                return "Quinquag\u00e9simo segundo"
            }
            53 -> {
                return "Quinquag\u00e9simo terceiro"
            }
            54 -> {
                return "Quinquag\u00e9simo quarto"
            }
            55 -> {
                return "Quinquag\u00e9simo quinto"
            }
            56 -> {
                return "Quinquag\u00e9simo sexto"
            }
            57 -> {
                return "Quinquag\u00e9simo s\u00e9timo"
            }
            58 -> {
                return "Quinquag\u00e9simo oitavo"
            }
            59 -> {
                return "Quinquag\u00e9simo nono"
            }
            60 -> {
                return "Sexag\u00e9simo"
            }
            61 -> {
                return "Sexag\u00e9simo primeiro"
            }
            62 -> {
                return "Sexag\u00e9simo segundo"
            }
            63 -> {
                return "Sexag\u00e9simo terceiro"
            }
            64 -> {
                return "Sexag\u00e9simo quarto"
            }
            65 -> {
                return "Sexag\u00e9simo quinto"
            }
            66 -> {
                return "Sexag\u00e9simo sexto"
            }
            67 -> {
                return "Sexag\u00e9simo s\u00e9timo"
            }
            68 -> {
                return "Sexag\u00e9simo oitavo"
            }
            69 -> {
                return "Sexag\u00e9simo nono"
            }
            70 -> {
                return "Septuag\u00e9simo"
            }
            71 -> {
                return "Septuag\u00e9simo primeiro"
            }
            72 -> {
                return "Septuag\u00e9simo segundo"
            }
            73 -> {
                return "Septuag\u00e9simo terceiro"
            }
            74 -> {
                return "Septuag\u00e9simo quarto"
            }
            75 -> {
                return "Septuag\u00e9simo quinto"
            }
            76 -> {
                return "Septuag\u00e9simo sexto"
            }
            77 -> {
                return "Septuag\u00e9simo s\u00e9timo"
            }
            78 -> {
                return "Septuag\u00e9simo oitavo"
            }
            79 -> {
                return "Septuag\u00e9simo nono"
            }
            80 -> {
                return "Octog\u00e9simo"
            }
            81 -> {
                return "Octog\u00e9simo primeiro"
            }
            82 -> {
                return "Octog\u00e9simo segundo"
            }
            83 -> {
                return "Octog\u00e9simo terceiro"
            }
            84 -> {
                return "Octog\u00e9simo quarto"
            }
            85 -> {
                return "Octog\u00e9simo quinto"
            }
            86 -> {
                return "Octog\u00e9simo sexto"
            }
            87 -> {
                return "Octog\u00e9simo s\u00e9timo"
            }
            88 -> {
                return "Octog\u00e9simo oitavo"
            }
            89 -> {
                return "Octog\u00e9simo nono"
            }
            90 -> {
                return "Nonag\u00e9simo"
            }
            91 -> {
                return "Nonag\u00e9simo primeiro"
            }
            92 -> {
                return "Nonag\u00e9simo segundo"
            }
            93 -> {
                return "Nonag\u00e9simo terceiro"
            }
            94 -> {
                return "Nonag\u00e9simo quarto"
            }
            95 -> {
                return "Nonag\u00e9simo quinto"
            }
            96 -> {
                return "Nonag\u00e9simo sexto"
            }
            97 -> {
                return "Nonag\u00e9simo s\u00e9timo"
            }
            98 -> {
                return "Nonag\u00e9simo oitavo"
            }
            99 -> {
                return "Nonag\u00e9simo nono"
            }
            100 -> {
                return "Cent\u00e9simo"
            }
            else -> {
                return null
            }
        }
    }

    fun getCenteredMessage(message: String): String {
        return getCenteredMessage(message, 154, false)
    }

    fun getCenteredMessage(message: String, CENTER_PX: Int): String {
        return getCenteredMessage(message, CENTER_PX, false)
    }

    fun getCenteredMessage(message: String?, CENTER_PX: Int, spaceOnRight: Boolean): String {
        var message = message
        if (message == null || message == "") {
            return ""
        }
        message = message.replace("&", "§")
        var messagePxSize = 0
        var previousCode = false
        var isBold = false
        for (c in message!!.toCharArray()) {
            if (c == '§') {
                previousCode = true
            } else if (previousCode) {
                previousCode = false
                isBold = c == 'l' || c == 'L'
            } else {
                val dFI = DefaultFontInfo.getDefaultFontInfo(c)
                messagePxSize += if (isBold) dFI.boldLength else dFI.length
                ++messagePxSize
            }
        }
        val halvedMessageSize = messagePxSize / 2
        val toCompensate = CENTER_PX - halvedMessageSize
        val spaceLength = DefaultFontInfo.SPACE.length + 1
        var compensated = 0
        val sb = StringBuilder()
        while (compensated < toCompensate) {
            sb.append(" ")
            compensated += spaceLength
        }
        return sb.toString() + message + if (spaceOnRight) sb.toString() else ""
    }

    fun getHeader(message: String?, CENTER_PX: Int): String {
        var message = message
        if (message == null || message == "") {
            return ""
        }
        message = message.replace("&", "§")
        var messagePxSize = 0
        var previousCode = false
        var isBold = false
        for (c in message!!.toCharArray()) {
            if (c == '§') {
                previousCode = true
            } else if (previousCode) {
                previousCode = false
                isBold = c == 'l' || c == 'L'
            } else {
                val dFI = DefaultFontInfo.getDefaultFontInfo(c)
                messagePxSize += if (isBold) dFI.boldLength else dFI.length
                ++messagePxSize
            }
        }
        val halvedMessageSize = messagePxSize / 2
        val toCompensate = CENTER_PX - halvedMessageSize
        val spaceLength = DefaultFontInfo.MINUS.length + 1
        var compensated = 0
        val sb = StringBuilder()
        var colorSwitch = false
        while (compensated < toCompensate) {
            sb.append((if (colorSwitch) "§b" else "§3") + "§m-")
            compensated += spaceLength
            colorSwitch = !colorSwitch
        }
        return sb.toString() + message + sb.toString()
    }

    fun getCenteredHeader(message: String): String {
        return getCenteredMessage(getHeader(message, 154))
    }
}