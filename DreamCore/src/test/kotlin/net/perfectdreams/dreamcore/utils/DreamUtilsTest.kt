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
	companion object {
		const val TEST_USERNAME = "MrPowerGamerBR"
		val TEST_UUID = UUID.nameUUIDFromBytes(("OfflinePlayer:$TEST_USERNAME").toByteArray(Charsets.UTF_8))
	}

	val player = mockk<Player>()

	init {
		every { player.name } returns TEST_USERNAME
		every { player.uniqueId } returns TEST_UUID

		mockkStatic("org.bukkit.Bukkit")
		every { Bukkit.isPrimaryThread() } returns true
	}

	@Test
	fun `test player info`() {
		val playerInfo = DreamUtils.createPerfectDreamsPlayerInfo(player)

		assertThat(playerInfo.username).isEqualTo(TEST_USERNAME)
		assertThat(playerInfo.lowerCaseUsername).isEqualToIgnoringCase(TEST_USERNAME)
		assertThat(playerInfo.uniqueId).isEqualTo(TEST_UUID)
	}

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