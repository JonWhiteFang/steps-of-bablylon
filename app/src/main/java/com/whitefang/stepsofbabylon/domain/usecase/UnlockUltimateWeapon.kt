package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.OwnedWeapon
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.UltimateWeaponRepository

class UnlockUltimateWeapon(
    private val uwRepository: UltimateWeaponRepository,
    private val playerRepository: PlayerRepository,
) {
    suspend operator fun invoke(type: UltimateWeaponType, powerStones: Long, owned: List<OwnedWeapon>): Boolean {
        if (owned.any { it.type == type }) return false
        if (powerStones < type.unlockCost) return false
        playerRepository.spendPowerStones(type.unlockCost.toLong())
        uwRepository.unlockWeapon(type)
        return true
    }
}
