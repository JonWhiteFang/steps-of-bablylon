package com.whitefang.stepsofbabylon.presentation.battle.engine

import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ProjectileEntity
import kotlin.math.hypot

object CollisionSystem {

    fun checkCollisions(
        entities: List<Entity>,
        zigX: Float, zigY: Float, zigWidth: Float,
        onProjectileHitEnemy: (ProjectileEntity, EnemyEntity) -> Unit,
        onEnemyProjectileHitZiggurat: (EnemyProjectileEntity) -> Unit,
    ) {
        val projectiles = entities.filterIsInstance<ProjectileEntity>().filter { it.isAlive }
        val enemies = entities.filterIsInstance<EnemyEntity>().filter { it.isAlive }
        val enemyProjectiles = entities.filterIsInstance<EnemyProjectileEntity>().filter { it.isAlive }

        for (proj in projectiles) {
            for (enemy in enemies) {
                if (hypot(proj.x - enemy.x, proj.y - enemy.y) < (proj.width + enemy.width) / 2f) {
                    onProjectileHitEnemy(proj, enemy)
                    break
                }
            }
        }

        for (proj in enemyProjectiles) {
            if (hypot(proj.x - zigX, proj.y - zigY) < zigWidth / 2f + proj.width / 2f) {
                onEnemyProjectileHitZiggurat(proj)
            }
        }
    }
}
