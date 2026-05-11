package com.whitefang.stepsofbabylon.data.billing.internal

import android.app.Activity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton holder for the currently foregrounded [Activity], used by
 * [com.whitefang.stepsofbabylon.data.billing.BillingManagerImpl] to obtain an Activity
 * reference for `BillingClient.launchBillingFlow()` — which requires an Activity and cannot
 * accept a Context.
 *
 * **Lifecycle wiring (deferred to C.5 PR 2):** `MainActivity.onResume()` will call
 * [set] and `onPause()` will call [clear]. PR 1 leaves this class wired into DI but no
 * caller registers into it — `@Binds` still points at `StubBillingManager`, which does not
 * consult this holder, so the class stays dormant in PR 1. The seam exists here to make the
 * C.5 PR 2 diff a pure binding + lifecycle-observer change.
 *
 * **Memory safety.** Uses a [WeakReference] so a missed [clear] cannot leak the Activity
 * past its finalizer. Callers of [current] MUST handle the `null` case gracefully —
 * purchases attempted while no Activity is foregrounded return
 * [com.whitefang.stepsofbabylon.data.billing.internal.StartPurchaseResult.NotCompleted].
 *
 * Introduced by C.5 PR 1 / ADR-0005.
 */
@Singleton
internal class ActivityProvider @Inject constructor() {

    @Volatile
    private var activityRef: WeakReference<Activity>? = null

    /** Registers [activity] as the current foregrounded Activity. Idempotent. */
    fun set(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    /** Clears the registered Activity reference. Safe to call when nothing is registered. */
    fun clear() {
        activityRef = null
    }

    /**
     * Returns the currently foregrounded Activity, or `null` if nothing is registered or the
     * registered Activity has been garbage-collected.
     */
    fun current(): Activity? = activityRef?.get()
}
