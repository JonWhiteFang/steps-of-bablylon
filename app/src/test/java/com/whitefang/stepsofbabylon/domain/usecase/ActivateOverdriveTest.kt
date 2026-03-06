package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.OverdriveType
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class ActivateOverdriveTest {

    private val sut = ActivateOverdrive()

    @Test
    fun `sufficient steps and not used returns Success`() {
        assertInstanceOf(ActivateOverdrive.Result.Success::class.java, sut(OverdriveType.ASSAULT, 500, false))
    }

    @Test
    fun `insufficient steps returns Failure`() {
        assertInstanceOf(ActivateOverdrive.Result.Failure::class.java, sut(OverdriveType.ASSAULT, 499, false))
    }

    @Test
    fun `already used returns Failure`() {
        assertInstanceOf(ActivateOverdrive.Result.Failure::class.java, sut(OverdriveType.ASSAULT, 10000, true))
    }

    @Test
    fun `each type checks its own cost`() {
        assertInstanceOf(ActivateOverdrive.Result.Success::class.java, sut(OverdriveType.FORTUNE, 300, false))
        assertInstanceOf(ActivateOverdrive.Result.Failure::class.java, sut(OverdriveType.FORTUNE, 299, false))
        assertInstanceOf(ActivateOverdrive.Result.Success::class.java, sut(OverdriveType.SURGE, 750, false))
        assertInstanceOf(ActivateOverdrive.Result.Failure::class.java, sut(OverdriveType.SURGE, 749, false))
    }
}
