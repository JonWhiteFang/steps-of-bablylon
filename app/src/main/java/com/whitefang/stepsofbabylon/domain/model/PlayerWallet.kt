package com.whitefang.stepsofbabylon.domain.model

data class PlayerWallet(
    val stepBalance: Long = 0,
    val cash: Long = 0,
    val gems: Long = 0,
    val powerStones: Long = 0,
)
