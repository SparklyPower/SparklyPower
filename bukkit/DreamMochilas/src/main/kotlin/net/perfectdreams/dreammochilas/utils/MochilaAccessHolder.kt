package net.perfectdreams.dreammochilas.utils

class MochilaAccessHolder(
    val wrapper: MochilaWrapper
) {
    var isHolding = false
    val mochila = wrapper.mochila

    suspend fun getOrCreateMochilaInventoryAndHold() = wrapper.getOrCreateMochilaInventory().also {
        (it.holder as MochilaInventoryHolder).accessHolders.add(this)
    }

    suspend fun hold(triggerType: String? = null) {
        wrapper.hold(triggerType)
        isHolding = true
    }

    suspend fun release(triggerType: String? = null) {
        if (!isHolding)
            error("Tried to release a mochila ${mochila.id.value} (${wrapper}) hold status that is not being held anymore! Triggered by $triggerType")
        wrapper.release(triggerType)
        isHolding = false
    }
}