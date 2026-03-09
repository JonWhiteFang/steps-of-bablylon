package com.whitefang.stepsofbabylon.presentation.battle.effects

import kotlin.random.Random

object ProjectileTrailEffect {
    fun spawn(pool: ParticlePool, x: Float, y: Float, color: Int) {
        val p = pool.acquire()
        p.x = x + Random.nextFloat() * 4f - 2f
        p.y = y + Random.nextFloat() * 4f - 2f
        p.vx = Random.nextFloat() * 10f - 5f
        p.vy = Random.nextFloat() * 10f - 5f
        p.color = color; p.size = 2f + Random.nextFloat() * 2f
        p.lifetime = 0.3f; p.alpha = 0.8f
    }
}
