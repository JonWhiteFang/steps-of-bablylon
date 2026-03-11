package com.whitefang.stepsofbabylon.data.sensor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests the step ingestion coordination logic between service and worker.
 * Uses in-memory fakes to verify no double-crediting under all scenarios.
 */
class StepIngestionTest {

    private lateinit var prefs: FakeStepIngestionPreferences
    private var roomSensorSteps: Long = 0L
    private var totalCredited: Long = 0L

    // Use realistic epoch millis so heartbeat logic works correctly
    private val baseTime = 1_710_000_000_000L // ~March 2024
    private val threeMinMs = 3 * 60 * 1000L

    @BeforeEach
    fun setup() {
        prefs = FakeStepIngestionPreferences()
        roomSensorSteps = 0L
        totalCredited = 0L
    }

    /**
     * Simulates the worker's sensorCatchUp logic.
     * Returns the gap credited (0 if skipped).
     */
    private fun workerCatchUp(
        today: String,
        currentCounter: Long,
        nowMs: Long,
    ): Long {
        if (prefs.isServiceAlive(nowMs)) return 0

        val dayStart = prefs.getCounterAtDayStart(today)
        if (dayStart == null) {
            prefs.setCounterAtDayStart(today, currentCounter)
            return 0
        }

        val rawToday = currentCounter - dayStart
        if (rawToday <= 0) return 0

        val gap = rawToday - roomSensorSteps
        if (gap > 0) {
            roomSensorSteps += gap
            totalCredited += gap
            return gap
        }
        return 0
    }

    /**
     * Simulates the service crediting steps (updates Room sensorSteps + heartbeat).
     */
    private fun serviceCredit(steps: Long, nowMs: Long) {
        roomSensorSteps += steps
        totalCredited += steps
        prefs.updateServiceHeartbeat(nowMs)
    }

    @Test
    fun `worker skips when service heartbeat is fresh`() {
        prefs.setCounterAtDayStart("2026-03-11", 10000)
        prefs.updateServiceHeartbeat(baseTime)

        val credited = workerCatchUp("2026-03-11", 10500, baseTime + 500)
        assertEquals(0, credited)
    }

    @Test
    fun `worker establishes baseline on first run and credits nothing`() {
        val credited = workerCatchUp("2026-03-11", 50000, baseTime)
        assertEquals(0, credited)
        assertEquals(50000L, prefs.getCounterAtDayStart("2026-03-11"))
    }

    @Test
    fun `worker credits gap when service is dead`() {
        prefs.setCounterAtDayStart("2026-03-11", 50000)
        // No heartbeat — service never ran

        val credited = workerCatchUp("2026-03-11", 50200, baseTime)
        assertEquals(200, credited)
    }

    @Test
    fun `worker credits only uncredited gap after service death`() {
        val today = "2026-03-11"
        prefs.setCounterAtDayStart(today, 50000)

        // Service credits 500 steps
        serviceCredit(500, baseTime)

        // Service dies. Worker runs 3 min after last heartbeat.
        // Counter is now 50700 (500 from service + 200 new)
        val credited = workerCatchUp(today, 50700, baseTime + threeMinMs)
        assertEquals(200, credited)
    }

    @Test
    fun `no double credit when service and worker both active`() {
        val today = "2026-03-11"
        prefs.setCounterAtDayStart(today, 50000)

        // Service credits 300 steps
        serviceCredit(300, baseTime)

        // Worker fires while service is alive (heartbeat fresh)
        val credited = workerCatchUp(today, 50300, baseTime + 500)
        assertEquals(0, credited)
        assertEquals(300, totalCredited)
    }

    @Test
    fun `day rollover resets baseline`() {
        prefs.setCounterAtDayStart("2026-03-11", 50000)
        roomSensorSteps = 5000

        // New day — worker runs, day-start is null for new date
        val credited = workerCatchUp("2026-03-12", 55000, baseTime)
        assertEquals(0, credited) // just establishes baseline
        assertEquals(55000L, prefs.getCounterAtDayStart("2026-03-12"))
    }

    @Test
    fun `worker credits correctly after day rollover baseline established`() {
        roomSensorSteps = 0
        prefs.setCounterAtDayStart("2026-03-12", 55000)

        val credited = workerCatchUp("2026-03-12", 55300, baseTime)
        assertEquals(300, credited)
    }

    @Test
    fun `multiple worker runs credit incrementally without duplication`() {
        val today = "2026-03-11"
        prefs.setCounterAtDayStart(today, 50000)

        val c1 = workerCatchUp(today, 50200, baseTime)
        assertEquals(200, c1)

        val c2 = workerCatchUp(today, 50350, baseTime + 15 * 60 * 1000)
        assertEquals(150, c2)

        val c3 = workerCatchUp(today, 50350, baseTime + 30 * 60 * 1000)
        assertEquals(0, c3)

        assertEquals(350, totalCredited)
    }

    @Test
    fun `service credits then dies then worker recovers exactly the gap`() {
        val today = "2026-03-11"
        prefs.setCounterAtDayStart(today, 50000)

        // Service credits 1000 steps over time
        serviceCredit(400, baseTime)
        serviceCredit(300, baseTime + 1000)
        serviceCredit(300, baseTime + 2000)
        assertEquals(1000, totalCredited)

        // Service dies. 500 more steps happen.
        val credited = workerCatchUp(today, 51500, baseTime + 2000 + threeMinMs)
        assertEquals(500, credited)
        assertEquals(1500, totalCredited)
    }

    @Test
    fun `counter reboot mid-day produces no negative credit`() {
        val today = "2026-03-11"
        prefs.setCounterAtDayStart(today, 50000)
        roomSensorSteps = 3000

        // After device reboot, counter resets to a low value
        val credited = workerCatchUp(today, 100, baseTime)
        assertEquals(0, credited)
    }
}
