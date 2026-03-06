package com.whitefang.stepsofbabylon.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpgradeTypeTest {

    @Test
    fun `23 entries exist`() {
        assertEquals(23, UpgradeType.entries.size)
    }

    @Test
    fun `category counts are 8 attack 9 defense 6 utility`() {
        assertEquals(8, UpgradeType.entries.count { it.category == UpgradeCategory.ATTACK })
        assertEquals(9, UpgradeType.entries.count { it.category == UpgradeCategory.DEFENSE })
        assertEquals(6, UpgradeType.entries.count { it.category == UpgradeCategory.UTILITY })
    }

    @Test
    fun `every entry has valid config`() {
        UpgradeType.entries.forEach { type ->
            assertTrue(type.config.baseCost > 0, "$type baseCost should be positive")
            assertTrue(type.config.scaling > 1.0, "$type scaling should be > 1.0")
        }
    }
}
