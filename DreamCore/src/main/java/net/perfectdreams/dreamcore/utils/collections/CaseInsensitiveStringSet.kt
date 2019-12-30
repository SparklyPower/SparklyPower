/**
 * The MIT License
 * Copyright (c) 2014-2015 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.perfectdreams.dreamcore.utils.collections

import com.google.common.collect.ForwardingSet

class CaseInsensitiveStringSet : ForwardingSet<String>() {
	private val backing = CaseInsensitiveStringMap<Any>()

	override fun delegate(): MutableSet<String> {
		return backing.keys
	}

	override fun add(element: String): Boolean {
		try {
			backing[element] = VALUE
			return true
		} catch (e: Exception) {
			return false
		}
	}

	override fun contains(element: String?): Boolean {
		return backing.containsKey(element)
	}

	override fun addAll(collection: Collection<String>): Boolean {
		return standardAddAll(collection)
	}

	companion object {
		val VALUE = Any()
	}
}
