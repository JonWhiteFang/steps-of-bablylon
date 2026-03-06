package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.OverdriveType

class ActivateOverdrive {

    sealed class Result {
        data object Success : Result()
        data class Failure(val reason: String) : Result()
    }

    operator fun invoke(type: OverdriveType, stepBalance: Long, alreadyUsed: Boolean): Result = when {
        alreadyUsed -> Result.Failure("Already used this round")
        stepBalance < type.stepCost -> Result.Failure("Insufficient steps")
        else -> Result.Success
    }
}
