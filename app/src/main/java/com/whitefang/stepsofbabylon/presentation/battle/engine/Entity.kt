package com.whitefang.stepsofbabylon.presentation.battle.engine

import android.graphics.Canvas

abstract class Entity(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var isAlive: Boolean = true,
) {
    abstract fun update(deltaTime: Float)
    abstract fun render(canvas: Canvas)
}
