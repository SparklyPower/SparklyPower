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

import java.util.concurrent.ConcurrentHashMap

import com.google.common.collect.ForwardingMap

class CaseInsensitiveStringMap<V> : ForwardingMap<String, V>() {
	private val delegate = ConcurrentHashMap<String, V>()

	override fun delegate(): MutableMap<String, V> {
		return delegate
	}

	override fun remove(key: String?): V? {
		var key = key
		if (key is String) key = key.toLowerCase()
		return delegate().remove(key)
	}

	override operator fun get(key: String?): V? {
		var key = key
		if (key is String) key = key.toLowerCase()
		return delegate().get(key)
	}

	override fun put(key: String, value: V): V? {
		var key = key
		key = key.toLowerCase()
		return delegate().put(key, value)
	}

	override fun putAll(map: Map<out String, V>) {
		standardPutAll(map)
	}

	override fun containsKey(key: String?): Boolean {
		var key = key
		if (key is String) key = key.toLowerCase()
		return delegate().containsKey(key)
	}
}
