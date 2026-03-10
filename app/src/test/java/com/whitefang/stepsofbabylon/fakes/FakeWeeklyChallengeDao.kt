package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.data.local.WeeklyChallengeDao
import com.whitefang.stepsofbabylon.data.local.WeeklyChallengeEntity

class FakeWeeklyChallengeDao : WeeklyChallengeDao {
    private val data = mutableMapOf<String, WeeklyChallengeEntity>()

    override suspend fun getByWeek(weekStart: String): WeeklyChallengeEntity? = data[weekStart]

    override suspend fun upsert(entity: WeeklyChallengeEntity) { data[entity.weekStartDate] = entity }
}
