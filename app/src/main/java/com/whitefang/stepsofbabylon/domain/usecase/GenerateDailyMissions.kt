package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.DailyMissionDao
import com.whitefang.stepsofbabylon.data.local.DailyMissionEntity
import com.whitefang.stepsofbabylon.domain.model.DailyMissionType
import com.whitefang.stepsofbabylon.domain.model.MissionCategory
import kotlin.random.Random

class GenerateDailyMissions(private val dailyMissionDao: DailyMissionDao) {

    suspend operator fun invoke(todayDate: String) {
        if (dailyMissionDao.getByDateOnce(todayDate).isNotEmpty()) return

        val rng = Random(todayDate.hashCode())
        for (category in MissionCategory.entries) {
            val candidates = DailyMissionType.byCategory(category)
            val picked = candidates[rng.nextInt(candidates.size)]
            dailyMissionDao.insert(DailyMissionEntity(
                date = todayDate,
                missionType = picked.name,
                target = picked.target,
                rewardGems = picked.rewardGems,
                rewardPowerStones = picked.rewardPowerStones,
            ))
        }
    }
}
