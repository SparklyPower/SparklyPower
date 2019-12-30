package net.perfectdreams.dreamcore.utils

infix fun Double.percentOf(val2: Double): Double = this / val2 * 100

fun chance(e: Double): Boolean {
	val d = Math.random();
	return d < e / 100.0;
}