package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.ActiveResearch
import com.whitefang.stepsofbabylon.domain.model.ResearchType
import com.whitefang.stepsofbabylon.fakes.FakeLabRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CheckResearchCompletionTest {

    private lateinit var labRepo: FakeLabRepository
    private lateinit var useCase: CheckResearchCompletion

    @BeforeEach
    fun setup() {
        labRepo = FakeLabRepository()
        useCase = CheckResearchCompletion(labRepo)
    }

    @Test
    fun `completes expired research`() = runTest {
        labRepo.active.value = listOf(
            ActiveResearch(ResearchType.DAMAGE_RESEARCH, 0, 1000, 5000),
            ActiveResearch(ResearchType.HEALTH_RESEARCH, 0, 1000, 3000),
        )
        val completed = useCase(now = 6000)
        assertEquals(2, completed.size)
        assertTrue(labRepo.active.value.isEmpty())
    }

    @Test
    fun `skips not-ready research`() = runTest {
        labRepo.active.value = listOf(
            ActiveResearch(ResearchType.DAMAGE_RESEARCH, 0, 1000, 5000),
            ActiveResearch(ResearchType.HEALTH_RESEARCH, 0, 1000, 10000),
        )
        val completed = useCase(now = 6000)
        assertEquals(1, completed.size)
        assertEquals(ResearchType.DAMAGE_RESEARCH, completed[0])
        assertEquals(1, labRepo.active.value.size)
    }

    @Test
    fun `handles empty list`() = runTest {
        val completed = useCase(now = 6000)
        assertTrue(completed.isEmpty())
    }
}
