package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.UltimateWeaponRepository

class UpgradeUltimateWeapon(
    private val uwRepository: UltimateWeaponRepository,
    private val playerRepository: PlayerRepository,
) {
    suspend operator fun invoke(type: UltimateWeaponType, currentLevel: Int, powerStones: Long): Boolean {
        if (currentLevel >= UltimateWeaponType.MAX_LEVEL) return false
        val cost = type.upgradeCost(currentLevel)
        if (powerStones < cost) return false
        playerRepository.spendPowerStones(cost.toLong())
        uwRepository.upgradeWeapon(type, currentLevel + 1)
        return true
    }
}
