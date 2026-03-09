package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.MilestoneDao
import com.whitefang.stepsofbabylon.domain.model.Milestone

class CheckMilestones(private val milestoneDao: MilestoneDao) {

    suspend operator fun invoke(totalStepsEarned: Long): List<Milestone> {
        val claimed = milestoneDao.getAllOnce().filter { it.claimed }.map { it.milestoneId }.toSet()
        return Milestone.entries.filter { it.requiredSteps <= totalStepsEarned && it.name !in claimed }
    }
}
