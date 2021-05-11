package net.perfectdreams.dreamquiz.utils

import org.bukkit.Bukkit
import org.bukkit.Location

class QuizConfig(var questions: MutableList<QuizQuestion>, var spawn: LocationWrapper)

class QuizQuestion(val question: String,
                   val answer: Boolean)

class LocationWrapper(val world: String,
                      val x: Double,
                      val y: Double,
                      val z: Double,
                      val yaw: Float,
                      val pitch: Float) { fun toLocation(): Location = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch) }
