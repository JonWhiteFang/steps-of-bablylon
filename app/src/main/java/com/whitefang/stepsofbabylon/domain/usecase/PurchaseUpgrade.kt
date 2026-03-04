package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.PlayerWallet
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository

class PurchaseUpgrade(
    private val workshopRepository: WorkshopRepository,
    private val playerRepository: PlayerRepository,
    private val calculateCost: CalculateUpgradeCost = CalculateUpgradeCost(),
) {
    suspend operator fun invoke(type: UpgradeType, currentLevel: Int, wallet: PlayerWallet): Boolean {
        val maxLevel = type.config.maxLevel
        if (maxLevel != null && currentLevel >= maxLevel) return false

        val cost = calculateCost(type, currentLevel)
        if (wallet.stepBalance < cost) return false

        playerRepository.spendSteps(cost)
        workshopRepository.setUpgradeLevel(type, currentLevel + 1)
        return true
    }
}
