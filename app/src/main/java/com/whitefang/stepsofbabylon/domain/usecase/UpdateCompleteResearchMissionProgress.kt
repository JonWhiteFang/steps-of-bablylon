package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.DailyMissionDao
import com.whitefang.stepsofbabylon.domain.model.DailyMissionType
import java.time.LocalDate

/**
 * Increments today's COMPLETE_RESEARCH daily-mission progress when one or more research
 * projects have actually completed.
 *
 * Encapsulates the DAO lookup + progress update logic in a single testable unit so the
 * call sites in [com.whitefang.stepsofbabylon.presentation.labs.LabsViewModel]
 * (init / rushResearch / freeRush) cannot diverge — anyone calling the use case gets the
 * same gating semantics for free.
 *
 * Idempotent at the row level: if today's mission row is missing, already claimed, or
 * already completed, the use case is a no-op. Exceptions from the DAO are swallowed
 * (matches the prior `LabsViewModel.updateResearchMission` fail-open contract — a
 * transient DAO outage must not crash the Labs screen).
 *
 * R3-03 / GitHub #1: this use case must NOT advance the mission counter when
 * [completedCount] is zero or negative. The gating guard is added in the second commit
 * of the fix branch; the staging commit deliberately omits it so the regression tests
 * fail RED against the historical bug shape.
 */
class UpdateCompleteResearchMissionProgress(
    private val dailyMissionDao: DailyMissionDao,
) {
    suspend operator fun invoke(
        completedCount: Int,
        today: String = LocalDate.now().toString(),
    ) {
        // STAGING SHAPE — matches the pre-fix `LabsViewModel.updateResearchMission`
        // behaviour: always sets the mission to (target, completed=true) regardless of
        // [completedCount]. The fix in commit 2 replaces this with proper gating.
        try {
            val missions = dailyMissionDao.getByDateOnce(today)
            val m = missions.find {
                it.missionType == DailyMissionType.COMPLETE_RESEARCH.name &&
                    !it.claimed &&
                    !it.completed
            } ?: return
            dailyMissionDao.updateProgress(m.id, m.target, true)
        } catch (_: Exception) {
            // Swallowed: matches the prior LabsViewModel.updateResearchMission contract.
        }
    }
}
