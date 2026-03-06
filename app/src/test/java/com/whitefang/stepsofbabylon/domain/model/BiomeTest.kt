package com.whitefang.stepsofbabylon.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BiomeTest {

    @Test
    fun `tier to biome mapping`() {
        (1..3).forEach { assertEquals(Biome.HANGING_GARDENS, Biome.forTier(it)) }
        (4..6).forEach { assertEquals(Biome.BURNING_SANDS, Biome.forTier(it)) }
        (7..8).forEach { assertEquals(Biome.FROZEN_ZIGGURATS, Biome.forTier(it)) }
        (9..10).forEach { assertEquals(Biome.UNDERWORLD_OF_KUR, Biome.forTier(it)) }
        assertEquals(Biome.CELESTIAL_GATE, Biome.forTier(11))
        assertEquals(Biome.CELESTIAL_GATE, Biome.forTier(100))
    }
}
