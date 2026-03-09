package com.whitefang.stepsofbabylon.data.sensor

import com.whitefang.stepsofbabylon.data.local.DailyLoginDao
import com.whitefang.stepsofbabylon.data.local.DailyStepDao
import com.whitefang.stepsofbabylon.data.local.WeeklyChallengeDao
import com.whitefang.stepsofbabylon.domain.model.DropGeneratorState
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
    private val walkingEncounterRepository: WalkingEncounterRepository,
    private val supplyDropNotificationManager: SupplyDropNotificationManager,
    private val dailyLoginDao: DailyLoginDao,
    private val weeklyChallengeDao: WeeklyChallengeDao,
    private val dailyStepDao: DailyStepDao,
    private val widgetUpdateHelper: WidgetUpdateHelper,
) {
    companion object {
        const val DAILY_CEILING = 50_000L
        private val DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private var currentDate: String = todayDate()
    private var dailySensorTotal: Long = 0L
    private var dailyCreditedTotal: Long = 0L
    private var initialized = false

    private val generateSupplyDrop = GenerateSupplyDrop()
    private var dropState = DropGeneratorState()

    private val trackDailyLogin by lazy { TrackDailyLogin(dailyLoginDao, playerRepository) }
    private val trackWeeklyChallenge by lazy { TrackWeeklyChallenge(weeklyChallengeDao, dailyStepDao, playerRepository) }

    fun getDailyCredited(): Long = dailyCreditedTotal

    fun todayDate(): String = LocalDate.now().format(DATE_FMT)

    suspend fun recordSteps(rawDelta: Long, timestampMs: Long) {
        if (rawDelta <= 0) return

        val today = todayDate()
        if (today != currentDate) {
            currentDate = today
            dailySensorTotal = 0L
            dailyCreditedTotal = 0L
            dropState = DropGeneratorState()
            initialized = false
        }

        if (!initialized) {
            val existing = stepRepository.getDailyRecord(currentDate)
            if (existing != null) {
                dailySensorTotal = existing.sensorSteps
                dailyCreditedTotal = existing.creditedSteps
                dropState = dropState.copy(lastCheckSteps = dailyCreditedTotal)
            }
            initialized = true
        }

        val rateLimited = rateLimiter.credit(rawDelta, timestampMs)
        if (rateLimited <= 0) return

        val remainingCeiling = (DAILY_CEILING - dailyCreditedTotal).coerceAtLeast(0)
        val credited = rateLimited.coerceAtMost(remainingCeiling)
        if (credited <= 0) return

        dailySensorTotal += rawDelta
        dailyCreditedTotal += credited

        stepRepository.updateDailySteps(currentDate, dailySensorTotal, dailyCreditedTotal)
        playerRepository.addSteps(credited)

        // Widget update
        try { widgetUpdateHelper.update(dailyCreditedTotal, 0) } catch (_: Exception) {}

        // Supply drop generation
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

        // Economy rewards
        try {
            trackDailyLogin.checkAndAward(currentDate, dailyCreditedTotal)
            trackWeeklyChallenge.checkAndAward()
        } catch (_: Exception) { /* best-effort */ }
    }

    suspend fun recordActivityMinutes(activityMinutes: Map<String, Int>, stepEquivalents: Long) {
        if (stepEquivalents <= 0) return

        val today = todayDate()
        if (today != currentDate) {
            currentDate = today
            dailySensorTotal = 0L
            dailyCreditedTotal = 0L
            dropState = DropGeneratorState()
            initialized = false
        }

        if (!initialized) {
            val existing = stepRepository.getDailyRecord(currentDate)
            if (existing != null) {
                dailySensorTotal = existing.sensorSteps
                dailyCreditedTotal = existing.creditedSteps
                dropState = dropState.copy(lastCheckSteps = dailyCreditedTotal)
            }
            initialized = true
        }

        val remainingCeiling = (DAILY_CEILING - dailyCreditedTotal).coerceAtLeast(0)
        val credited = stepEquivalents.coerceAtMost(remainingCeiling)
        if (credited <= 0) return

        dailyCreditedTotal += credited

        stepRepository.updateActivityMinutes(currentDate, activityMinutes, credited)
        playerRepository.addSteps(credited)
    }
}
