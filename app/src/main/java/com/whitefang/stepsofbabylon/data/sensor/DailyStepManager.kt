package com.whitefang.stepsofbabylon.data.sensor

import com.whitefang.stepsofbabylon.data.anticheat.AntiCheatPreferences
import com.whitefang.stepsofbabylon.data.local.DailyMissionDao
import com.whitefang.stepsofbabylon.data.local.DailyLoginDao
import com.whitefang.stepsofbabylon.data.local.DailyStepDao
import com.whitefang.stepsofbabylon.data.local.WeeklyChallengeDao
import com.whitefang.stepsofbabylon.domain.model.DropGeneratorState
import com.whitefang.stepsofbabylon.domain.model.DailyMissionType
import com.whitefang.stepsofbabylon.domain.model.MissionCategory
import com.whitefang.stepsofbabylon.domain.model.SupplyDropTrigger
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import com.whitefang.stepsofbabylon.domain.repository.WalkingEncounterRepository
import com.whitefang.stepsofbabylon.domain.usecase.GenerateSupplyDrop
import com.whitefang.stepsofbabylon.domain.usecase.TrackDailyLogin
import com.whitefang.stepsofbabylon.domain.usecase.TrackWeeklyChallenge
import com.whitefang.stepsofbabylon.service.SupplyDropNotificationManager
import com.whitefang.stepsofbabylon.service.WidgetUpdateHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates step crediting: rate limit → daily ceiling → persist to Room → supply drops → economy rewards.
 */
@Singleton
class DailyStepManager @Inject constructor(
    private val stepRepository: StepRepository,
    private val playerRepository: PlayerRepository,
    private val rateLimiter: StepRateLimiter,
    private val velocityAnalyzer: StepVelocityAnalyzer,
    private val antiCheatPrefs: AntiCheatPreferences,
    private val walkingEncounterRepository: WalkingEncounterRepository,
    private val supplyDropNotificationManager: SupplyDropNotificationManager,
    private val dailyLoginDao: DailyLoginDao,
    private val weeklyChallengeDao: WeeklyChallengeDao,
    private val dailyStepDao: DailyStepDao,
    private val dailyMissionDao: DailyMissionDao,
    private val widgetUpdateHelper: WidgetUpdateHelper,
) {
    companion object {
        const val DAILY_CEILING = 50_000L
        private val DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private var currentDate: String = todayDate()
    private var dailySensorTotal: Long = 0L
    private var dailySensorCredited: Long = 0L
    private var dailyCreditedTotal: Long = 0L
    private var dailyActivityMinuteTotal: Long = 0L
    private var initialized = false

    private val generateSupplyDrop = GenerateSupplyDrop()
    private var dropState = DropGeneratorState()

    private val stepsPerMinute = mutableMapOf<Long, Long>()

    private val trackDailyLogin by lazy { TrackDailyLogin(dailyLoginDao, playerRepository) }
    private val trackWeeklyChallenge by lazy { TrackWeeklyChallenge(weeklyChallengeDao, dailyStepDao, playerRepository) }

    fun getDailyCredited(): Long = dailyCreditedTotal

    fun getSensorStepsPerMinute(): Map<Long, Long> = stepsPerMinute.toMap()

    fun todayDate(): String = LocalDate.now().format(DATE_FMT)

    private suspend fun ensureInitialized() {
        val today = todayDate()
        if (today != currentDate) {
            currentDate = today
            dailySensorTotal = 0L
            dailySensorCredited = 0L
            dailyCreditedTotal = 0L
            dailyActivityMinuteTotal = 0L
            dropState = DropGeneratorState()
            stepsPerMinute.clear()
            initialized = false
            antiCheatPrefs.resetDailyCounters(today)
        }

        if (!initialized) {
            val existing = stepRepository.getDailyRecord(currentDate)
            if (existing != null) {
                dailySensorTotal = existing.sensorSteps
                dailySensorCredited = existing.creditedSteps
                dailyActivityMinuteTotal = existing.stepEquivalents
                dailyCreditedTotal = existing.creditedSteps + existing.stepEquivalents
                dropState = dropState.copy(lastCheckSteps = dailyCreditedTotal)
            }
            initialized = true
        }
    }

    suspend fun recordSteps(rawDelta: Long, timestampMs: Long) {
        if (rawDelta <= 0) return

        ensureInitialized()

        val rateLimited = rateLimiter.credit(rawDelta, timestampMs)
        val rateRejected = rawDelta - rateLimited
        if (rateRejected > 0) antiCheatPrefs.incrementRateRejected(rateRejected)
        if (rateLimited <= 0) return

        // Velocity analysis — penalize unnatural patterns
        val velocityMultiplier = velocityAnalyzer.analyze(rawDelta, timestampMs)
        val velocityAdjusted = (rateLimited * velocityMultiplier).toLong()
        val velocityPenalized = rateLimited - velocityAdjusted
        if (velocityPenalized > 0) antiCheatPrefs.incrementVelocityPenalized(velocityPenalized)
        if (velocityAdjusted <= 0) return

        val remainingCeiling = (DAILY_CEILING - dailyCreditedTotal).coerceAtLeast(0)
        val credited = velocityAdjusted.coerceAtMost(remainingCeiling)
        if (credited <= 0) return

        dailySensorTotal += rawDelta
        dailyCreditedTotal += credited

        stepRepository.updateDailySteps(currentDate, dailySensorTotal, dailySensorCredited + credited)
        dailySensorCredited += credited
        playerRepository.addSteps(credited)

        // Per-minute tracking for overlap deduction
        val epochMin = timestampMs / 60_000
        stepsPerMinute[epochMin] = (stepsPerMinute[epochMin] ?: 0) + credited
        if (stepsPerMinute.size > 1440) {
            val oldest = stepsPerMinute.keys.min()
            stepsPerMinute.remove(oldest)
        }

        runFollowOnPipeline(timestampMs)
    }

    suspend fun recordActivityMinutes(
        activityMinutes: Map<String, Int>,
        stepEquivalents: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ) {
        if (stepEquivalents <= 0) return

        ensureInitialized()

        val delta = stepEquivalents - dailyActivityMinuteTotal
        if (delta <= 0) return

        val remainingCeiling = (DAILY_CEILING - dailyCreditedTotal).coerceAtLeast(0)
        val credited = delta.coerceAtMost(remainingCeiling)
        if (credited <= 0) return

        dailyActivityMinuteTotal += credited
        dailyCreditedTotal += credited

        stepRepository.updateActivityMinutes(currentDate, activityMinutes, dailyActivityMinuteTotal)
        playerRepository.addSteps(credited)

        runFollowOnPipeline(timestampMs)
    }

    private suspend fun runFollowOnPipeline(timestampMs: Long) {
        // Widget update
        try {
            val balance = playerRepository.getStepBalance()
            widgetUpdateHelper.update(dailyCreditedTotal, balance)
        } catch (_: Exception) {}

        // Supply drop generation
        try {
            val prevSteps = dropState.lastCheckSteps
            val unclaimedCount = walkingEncounterRepository.getUnclaimedCount()
            val drop = generateSupplyDrop(dailyCreditedTotal, prevSteps, timestampMs, unclaimedCount)
            if (drop != null) {
                walkingEncounterRepository.enforceInboxCap(GenerateSupplyDrop.MAX_INBOX)
                val id = walkingEncounterRepository.createDrop(drop.trigger, drop.reward, drop.rewardAmount)
                supplyDropNotificationManager.notify(drop.copy(id = id.toInt()))
            }
            dropState = dropState.copy(
                lastCheckSteps = dailyCreditedTotal,
                milestoneTriggered = dropState.milestoneTriggered || (drop?.trigger == SupplyDropTrigger.DAILY_MILESTONE),
            )
        } catch (_: Exception) {}

        // Economy rewards
        try {
            trackDailyLogin.checkAndAward(currentDate, dailyCreditedTotal)
            trackWeeklyChallenge.checkAndAward()
        } catch (_: Exception) {}

        // Walking mission progress
        try { updateWalkingMissions() } catch (_: Exception) {}
    }

    private suspend fun updateWalkingMissions() {
        val missions = dailyMissionDao.getByDateOnce(currentDate)
        for (m in missions) {
            if (m.claimed || m.completed) continue
            val type = DailyMissionType.entries.find { it.name == m.missionType } ?: continue
            if (type.category != MissionCategory.WALKING) continue
            val progress = dailyCreditedTotal.toInt().coerceAtMost(m.target)
            dailyMissionDao.updateProgress(m.id, progress, progress >= m.target)
        }
    }
}
