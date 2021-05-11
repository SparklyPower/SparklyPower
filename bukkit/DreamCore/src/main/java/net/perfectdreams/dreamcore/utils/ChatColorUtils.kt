/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.perfectdreams.dreamcore.utils

import org.bukkit.ChatColor

object ChatColorUtils {

	fun applyColors(str: String): String {
		var ret: String = str
		for (col in ChatColor.values()) {
			if (!col.isFormat) {
				ret = applySingle(ret, col)
			}
		}
		return ret
	}

	fun applyFormats(str: String): String {
		var ret: String = str
		for (col in ChatColor.values()) {
			if (col.isFormat) {
				ret = applySingle(ret, col)
			}
		}
		return ret
	}

	fun applySingle(str: String, col: ChatColor): String {
		return str.replace("&${col.char}", col.toString())
	}

}

// Bukkit's "stripColor" can be null because it accepts a null input
// We don't care about it, so "!!"
fun String.colorize(ch: Char = '&') = ChatColor.translateAlternateColorCodes(ch, this)
fun String.stripColors(ch: Char = '&') = ChatColor.stripColor(translateColorCodes(ch))!!

fun String.translateColorCodes(ch: Char = '&') = ChatColor.translateAlternateColorCodes(ch, this)
fun String.stripColorCode(ch: Char= '&') = ChatColor.stripColor(translateColorCodes(ch))!!