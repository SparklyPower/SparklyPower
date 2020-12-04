package net.perfectdreams.dreamcore.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import java.util.*

class DreamUtilsTest {
	@Test
	fun `assert main thread in main thread`() {
		DreamUtils.assertMainThread(true)
	}

	@Test
	fun `assert async thread in main thread`() {
		assertThatThrownBy {
			DreamUtils.assertAsyncThread(true)
		}.isInstanceOf(UnsupportedOperationException::class.java)
	}
}