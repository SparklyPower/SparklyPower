package net.perfectdreams.dreamcore.utils

import sun.misc.Unsafe
import java.lang.reflect.Field

object JVMUnsafeUtils {
    // Hacky!
    lateinit var unsafe: Unsafe

    init {
        try {
            val singleoneInstanceField: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
            singleoneInstanceField.isAccessible = true
            unsafe = singleoneInstanceField.get(null) as Unsafe
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
}