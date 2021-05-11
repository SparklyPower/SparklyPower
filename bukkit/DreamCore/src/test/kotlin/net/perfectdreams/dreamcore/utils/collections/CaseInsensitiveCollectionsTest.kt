package net.perfectdreams.dreamcore.utils.collections

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CaseInsensitiveCollectionsTest {
	@Test
	fun `test case sensitive set`() {
		val set = CaseInsensitiveStringSet()

		set.add("mrpowergamerbr")
		set.add("MrPowerGamerBR")

		assertThat(set.size).isEqualTo(1)

		assertThat(set.contains("mrpowerGAMERBR")).isTrue()
		assertThat(set.contains("Loritta")).isFalse()
	}

	@Test
	fun `test case sensitive map`() {
		val map = CaseInsensitiveStringMap<Boolean>()

		map.put("mrpowergamerbr", true)
		map.put("MrPowerGamerBR", true)

		assertThat(map.size).isEqualTo(1)

		assertThat(map.contains("mrpowerGAMERBR")).isTrue()
		assertThat(map.contains("Loritta")).isFalse()
	}
}