package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.DailyLoginDao
import com.whitefang.stepsofbabylon.data.local.DailyLoginEntity
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrackDailyLogin(
    private val dailyLoginDao: DailyLoginDao,
    private val playerRepository: PlayerRepository,
) {
    companion object {
        private const val PS_STEP_THRESHOLD = 1_000L
        private const val MAX_STREAK = 7
        private const val MAX_GEM_REWARD = 5
    }

    suspend fun checkAndAward(todayDate: String, todayCreditedSteps: Long) {
        val login = dailyLoginDao.getByDate(todayDate) ?: DailyLoginEntity(date = todayDate)
        var updated = login.copy(stepsWalked = todayCreditedSteps)

        // PS for walking 1k+ steps
        if (todayCreditedSteps >= PS_STEP_THRESHOLD && !login.powerStoneClaimed) {
            playerRepository.addPowerStones(1)
            updated = updated.copy(powerStoneClaimed = true)
        }

        // Gem streak on first check of the day
        if (!login.gemsClaimed) {
            val profile = playerRepository.observeProfile().first()
            val yesterday = LocalDate.parse(todayDate, DateTimeFormatter.ISO_LOCAL_DATE).minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)

            val newStreak = when (profile.lastLoginDate) {
                todayDate -> profile.currentStreak // Already logged in today
                yesterday -> (profile.currentStreak % MAX_STREAK) + 1
                else -> 1 // Streak broken
            }

            if (profile.lastLoginDate != todayDate) {
                val gemReward = newStreak.coerceAtMost(MAX_GEM_REWARD).toLong()
                playerRepository.addGems(gemReward)
                playerRepository.updateStreak(newStreak, todayDate)
            }
            updated = updated.copy(gemsClaimed = true)
        }

        dailyLoginDao.upsert(updated)
    }
}
