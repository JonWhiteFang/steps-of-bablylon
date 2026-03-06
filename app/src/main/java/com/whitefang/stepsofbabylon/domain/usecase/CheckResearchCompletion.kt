package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.ResearchType
import com.whitefang.stepsofbabylon.domain.repository.LabRepository
import kotlinx.coroutines.flow.first

class CheckResearchCompletion(
    private val labRepository: LabRepository,
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()): List<ResearchType> {
        val activeList = labRepository.observeActiveResearch().first()
        val completed = mutableListOf<ResearchType>()
        for (research in activeList) {
            if (now >= research.completesAt) {
                labRepository.completeResearch(research.type)
                completed += research.type
            }
        }
        return completed
    }
}
