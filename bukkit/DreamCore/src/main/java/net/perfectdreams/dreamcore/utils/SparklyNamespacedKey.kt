package net.perfectdreams.dreamcore.utils

import org.bukkit.NamespacedKey

// Yeah yeah, deprecated because it is "internal use only", but we don't really care tbh, we are 99,99% sure that Mojang will never use "sparklypower" as the namespace
// and this is useful if we end up changing the plugin name
fun SparklyNamespacedKey(key: String) = NamespacedKey("sparklypower", key)