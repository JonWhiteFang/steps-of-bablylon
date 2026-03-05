package com.whitefang.stepsofbabylon.presentation.battle.engine

import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ZigguratEntity
import kotlin.math.hypot

object CollisionSystem {

    fun checkCollisions(entities: List<Entity>, ziggurat: ZigguratEntity) {
        val projectiles = entities.filterIsInstance<ProjectileEntity>().filter { it.isAlive }
        val enemies = entities.filterIsInstance<EnemyEntity>().filter { it.isAlive }
        val enemyProjectiles = entities.filterIsInstance<EnemyProjectileEntity>().filter { it.isAlive }

        // Ziggurat projectiles → enemies
        for (proj in projectiles) {
            for (enemy in enemies) {
                if (hypot(proj.x - enemy.x, proj.y - enemy.y) < (proj.width + enemy.width) / 2f) {
                    enemy.takeDamage(proj.damage)
                    proj.isAlive = false
                    break
                }
            }
        }

        // Enemy projectiles → ziggurat
        for (proj in enemyProjectiles) {
            if (hypot(proj.x - ziggurat.x, proj.y - ziggurat.y) < ziggurat.width / 2f + proj.width / 2f) {
                ziggurat.currentHp = (ziggurat.currentHp - proj.damage).coerceAtLeast(0.0)
                proj.isAlive = false
            }
        }
        // Melee damage is handled via EnemyEntity.onMeleeHit callback
    }
}
