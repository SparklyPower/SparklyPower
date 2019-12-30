package net.perfectdreams.dreamcore.utils

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Test

class LocationUtilsTest {
	private val world = mockk<World>()

	init {
		every { world.getBlockAt(0, 80, 0) } returns mockk()
	}

	@Test
	fun `check rounded location`() {
		val location = Location(world, 0.0, 80.0, 0.0)
		val rounded = LocationUtils.getRoundedDestination(location)

		assertThat(rounded.world).isEqualTo(world)
		assertThat(rounded.x).isEqualTo(0.5)
		assertThat(rounded.y).isEqualTo(80.0)
		assertThat(rounded.z).isEqualTo(0.5)
		assertThat(rounded.yaw).isEqualTo(0f)
		assertThat(rounded.pitch).isEqualTo(0f)
	}

	@Test
	fun `is block damaging`() {

	}
}