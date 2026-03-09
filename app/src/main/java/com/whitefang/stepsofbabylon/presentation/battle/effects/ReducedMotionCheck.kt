package com.whitefang.stepsofbabylon.presentation.battle.effects

import android.content.Context
import android.provider.Settings

object ReducedMotionCheck {
    fun isReducedMotionEnabled(context: Context): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}
