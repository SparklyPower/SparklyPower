package net.perfectdreams.dreamcore.utils

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Test

class ArmorStandHologramTest {
	companion object {
		const val STARTING_Y_POSITION = 80.0
	}

	private val world = mockk<World>()

	@Test
	fun `isSpawned after creation`() {
		val location = Location(world, 0.0, STARTING_Y_POSITION, 0.0)
		val armorStand = ArmorStandHologram(location, "Hello World")
		assertThat(armorStand.isSpawned()).isFalse()
	}

	@Test
	fun `check addLineBelow hologram location`() {
		val location = Location(world, 0.0, STARTING_Y_POSITION, 0.0)
		val armorStand = ArmorStandHologram(location, "Hello World")
		val newHologram = armorStand.addLineBelow("Hello World Below")
		assertThat(newHologram.location.y).isEqualTo(STARTING_Y_POSITION - 0.285)
	}

	@Test
	fun `check addLineAbove hologram location`() {
		val location = Location(world, 0.0, STARTING_Y_POSITION, 0.0)
		val armorStand = ArmorStandHologram(location, "Hello World")
		val newHologram = armorStand.addLineAbove("Hello World Above")
		assertThat(newHologram.location.y).isEqualTo(STARTING_Y_POSITION + 0.285)
	}
}