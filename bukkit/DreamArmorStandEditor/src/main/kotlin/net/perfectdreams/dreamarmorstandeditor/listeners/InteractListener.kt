package net.perfectdreams.dreamarmorstandeditor.listeners

import net.perfectdreams.dreamarmorstandeditor.DreamArmorStandEditor
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerQuitEvent

class InteractListener(val m: DreamArmorStandEditor) : Listener {
    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        m.isRotating.remove(e.player)
    }

    @EventHandler
    fun onInteract(e: PlayerInteractAtEntityEvent) {
        val editType = m.isRotating[e.player]
        if (editType != null) {
            val entity = e.rightClicked

            if (editType.armorStand == entity && entity is ArmorStand) {
                val actionType = editType.type

                // x = pitch
                // y = yaw
                // z = roll
                val editType = when {
                    actionType.name.startsWith("HEAD_") -> entity.headPose
                    actionType.name.startsWith("BODY_") -> entity.bodyPose
                    actionType.name.startsWith("LEFT_ARM_") -> entity.leftArmPose
                    actionType.name.startsWith("RIGHT_ARM_") -> entity.rightArmPose
                    actionType.name.startsWith("LEFT_LEG_") -> entity.leftLegPose
                    actionType.name.startsWith("RIGHT_LEG_") -> entity.rightLegPose
                    else -> throw IllegalArgumentException()
                }

                val value = if (e.player.isSneaking) 0.3925 else 0.785

                val newPose = when {
                    actionType.name.endsWith("_PITCH") -> editType.add(value, 0.0, 0.0)
                    actionType.name.endsWith("_YAW") -> editType.add(0.0, value, 0.0)
                    actionType.name.endsWith("_ROLL") -> editType.add(0.0, 0.0, value)
                    else -> throw IllegalArgumentException()
                }

                when {
                    actionType.name.startsWith("HEAD_") -> entity.headPose = newPose
                    actionType.name.startsWith("BODY_") -> entity.bodyPose = newPose
                    actionType.name.startsWith("LEFT_ARM_") -> entity.leftArmPose = newPose
                    actionType.name.startsWith("RIGHT_ARM_") -> entity.rightArmPose = newPose
                    actionType.name.startsWith("LEFT_LEG_") -> entity.leftLegPose = newPose
                    actionType.name.startsWith("RIGHT_LEG_") -> entity.rightLegPose = newPose
                    else -> throw IllegalArgumentException()
                }

                e.isCancelled = true
            }
        }
    }
}