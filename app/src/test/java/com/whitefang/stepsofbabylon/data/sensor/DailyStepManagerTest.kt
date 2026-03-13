package com.whitefang.stepsofbabylon.data.sensor

import com.whitefang.stepsofbabylon.data.anticheat.AntiCheatPreferences
import com.whitefang.stepsofbabylon.data.local.DailyMissionEntity
import com.whitefang.stepsofbabylon.domain.model.DailyMissionType
import com.whitefang.stepsofbabylon.fakes.FakeDailyLoginDao
import com.whitefang.stepsofbabylon.fakes.FakeDailyMissionDao
import com.whitefang.stepsofbabylon.fakes.FakeDailyStepDao
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeStepRepository
import com.whitefang.stepsofbabylon.fakes.FakeWalkingEncounterRepository
import com.whitefang.stepsofbabylon.fakes.FakeWeeklyChallengeDao
import com.whitefang.stepsofbabylon.service.SupplyDropNotificationManager
import com.whitefang.stepsofbabylon.service.WidgetUpdateHelper
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DailyStepManagerTest {

    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var stepRepo: FakeStepRepository
    private lateinit var dailyMissionDao: FakeDailyMissionDao
    private lateinit var widgetHelper: WidgetUpdateHelper
    private lateinit var manager: DailyStepManager

    private val baseTime = 1_710_000_000_000L
    // 61s apart ensures each call is in a fresh rate-limiter window
    private val minuteGap = 61_000L

    @BeforeEach
    fun setup() {
        playerRepo = FakePlayerRepository()
        stepRepo = FakeStepRepository()
        dailyMissionDao = FakeDailyMissionDao()
        widgetHelper = mock<WidgetUpdateHelper>()

        manager = DailyStepManager(
            stepRepository = stepRepo,
            playerRepository = playerRepo,
            rateLimiter = StepRateLimiter(),
            velocityAnalyzer = StepVelocityAnalyzer(),
            antiCheatPrefs = mock<AntiCheatPreferences>(),
            walkingEncounterRepository = FakeWalkingEncounterRepository(),
            supplyDropNotificationManager = mock<SupplyDropNotificationManager>(),
            dailyLoginDao = FakeDailyLoginDao(),
            weeklyChallengeDao = FakeWeeklyChallengeDao(),
            dailyStepDao = FakeDailyStepDao(),
            dailyMissionDao = dailyMissionDao,
            widgetUpdateHelper = widgetHelper,
        )
    }

    // --- R06: Widget balance ---

    @Test
    fun `widget receives real step balance after crediting`() = runTest {
        manager.recordSteps(100, baseTime)

        val balanceCaptor = argumentCaptor<Long>()
        verify(widgetHelper, atLeastOnce()).update(org.mockito.kotlin.any(), balanceCaptor.capture())
        assertEquals(100L, balanceCaptor.lastValue)
    }

    @Test
    fun `widget balance accumulates across multiple credits`() = runTest {
        manager.recordSteps(100, baseTime)
        manager.recordSteps(100, baseTime + minuteGap)

        val balanceCaptor = argumentCaptor<Long>()
        verify(widgetHelper, atLeastOnce()).update(org.mockito.kotlin.any(), balanceCaptor.capture())
        assertEquals(200L, balanceCaptor.lastValue)
    }

    // --- R07: Walking mission progress ---

    @Test
    fun `walking mission progress updates on step credit`() = runTest {
        val today = manager.todayDate()
        dailyMissionDao.insert(DailyMissionEntity(
            date = today, missionType = DailyMissionType.WALK_5000.name,
            target = 5000, rewardGems = 5,
        ))

        manager.recordSteps(100, baseTime)

        val missions = dailyMissionDao.getByDateOnce(today)
        assertEquals(100, missions[0].progress)
        assertFalse(missions[0].completed)
    }

    @Test
    fun `walking mission completes when target reached`() = runTest {
        val today = manager.todayDate()
        dailyMissionDao.insert(DailyMissionEntity(
            date = today, missionType = DailyMissionType.WALK_5000.name,
            target = 5000, rewardGems = 5,
        ))

        // Alternate 150/250 steps to avoid constant-rate velocity penalty (CV > 5%)
        // Each call in a fresh rate-limiter window (61s apart)
        var total = 0L
        var i = 0
        while (total < 5000) {
            val delta = if (i % 2 == 0) 150L else 250L
            manager.recordSteps(delta, baseTime + i * minuteGap)
            total += delta
            i++
        }

        val missions = dailyMissionDao.getByDateOnce(today)
        assertTrue(missions[0].progress >= 5000)
        assertTrue(missions[0].completed)
    }

    @Test
    fun `battle mission is not updated by step credits`() = runTest {
        val today = manager.todayDate()
        dailyMissionDao.insert(DailyMissionEntity(
            date = today, missionType = DailyMissionType.REACH_WAVE_30.name,
            target = 30, rewardGems = 3,
        ))

        manager.recordSteps(100, baseTime)

        val missions = dailyMissionDao.getByDateOnce(today)
        assertEquals(0, missions[0].progress)
        assertFalse(missions[0].completed)
    }

    // --- R2-01: Activity-minute idempotency ---

    @Test
    fun `activity minutes credit correct step-equivalents`() = runTest {
        manager.recordActivityMinutes(mapOf("cycling" to 10), 500)
        assertEquals(500L, playerRepo.getStepBalance())
    }

    @Test
    fun `duplicate activity-minute call produces zero additional credits`() = runTest {
        val minutes = mapOf("cycling" to 10)
        manager.recordActivityMinutes(minutes, 500)
        manager.recordActivityMinutes(minutes, 500)
        assertEquals(500L, playerRepo.getStepBalance())
    }

    @Test
    fun `incremental activity-minute call credits only delta`() = runTest {
        manager.recordActivityMinutes(mapOf("cycling" to 10), 500)
        manager.recordActivityMinutes(mapOf("cycling" to 20), 900)
        assertEquals(900L, playerRepo.getStepBalance())
    }

    @Test
    fun `combined sensor and activity-minute credits respect 50k ceiling`() = runTest {
        // Credit 49,900 sensor steps (alternate 150/250 to avoid velocity penalty)
        var total = 0L
        var i = 0
        while (total < 49_900) {
            val delta = if (i % 2 == 0) 150L else 250L
            manager.recordSteps(delta, baseTime + i * minuteGap)
            total += delta
            i++
        }
        val sensorBalance = playerRepo.getStepBalance()

        manager.recordActivityMinutes(mapOf("cycling" to 10), 500)

        val finalBalance = playerRepo.getStepBalance()
        val activityCredited = finalBalance - sensorBalance
        assertEquals(DailyStepManager.DAILY_CEILING, finalBalance)
        assertTrue(activityCredited <= 100, "Activity credits should be capped by ceiling, got $activityCredited")
    }

    @Test
    fun `process restart does not re-credit activity minutes`() = runTest {
        val minutes = mapOf("cycling" to 10)
        manager.recordActivityMinutes(minutes, 500)
        assertEquals(500L, playerRepo.getStepBalance())

        // Simulate process restart: new manager, same repos
        val manager2 = DailyStepManager(
            stepRepository = stepRepo,
            playerRepository = playerRepo,
            rateLimiter = StepRateLimiter(),
            velocityAnalyzer = StepVelocityAnalyzer(),
            antiCheatPrefs = mock<AntiCheatPreferences>(),
            walkingEncounterRepository = FakeWalkingEncounterRepository(),
            supplyDropNotificationManager = mock<SupplyDropNotificationManager>(),
            dailyLoginDao = FakeDailyLoginDao(),
            weeklyChallengeDao = FakeWeeklyChallengeDao(),
            dailyStepDao = FakeDailyStepDao(),
            dailyMissionDao = FakeDailyMissionDao(),
            widgetUpdateHelper = mock<WidgetUpdateHelper>(),
        )
        manager2.recordActivityMinutes(minutes, 500)
        assertEquals(500L, playerRepo.getStepBalance())
    }

    // --- R07: Walking mission progress ---

    @Test
    fun `already completed mission is not re-updated`() = runTest {
        val today = manager.todayDate()
        dailyMissionDao.insert(DailyMissionEntity(
            date = today, missionType = DailyMissionType.WALK_5000.name,
            target = 5000, rewardGems = 5, progress = 5000, completed = true,
        ))

        manager.recordSteps(100, baseTime)

        val missions = dailyMissionDao.getByDateOnce(today)
        assertEquals(5000, missions[0].progress)
        assertTrue(missions[0].completed)
    }
}
