package com.whitefang.stepsofbabylon.domain.time

import java.time.Instant
import java.time.LocalDate

/**
 * Seam for wall-clock access, introduced by Phase B.1 (RO-01).
 *
 * Motivation: 53 direct System.currentTimeMillis() / LocalDate.now() /
 * Instant.now() calls are scattered across 33 files (Phase 4 finding).
 * Midnight-boundary behaviour (daily step cap, daily missions, streaks,
 * battle-step cap) cannot be tested deterministically against the real clock.
 *
 * Scope: intentionally narrow.
 * - [now] covers millisecond-precision contexts.
 * - [today] covers day-bucket contexts.
 *
 * Migration policy for Phase B.1: only three call sites adopt this seam
 * (AwardBattleSteps, BattleViewModel, MissionsViewModel). Non-migrated sites
 * keep their direct calls. The goal is to land the abstraction without a
 * 53-file sweep. Subsequent phases may migrate additional sites as tests
 * demand.
 *
 * Domain-layer purity: this file must not import Android APIs (CONSTRAINTS.md
 * Architecture invariants). Only Kotlin and java.time.* are allowed here.
 */
interface TimeProvider {
    /** Current instant, milliseconds since epoch. */
    fun now(): Instant

    /** Current date in the device's default zone. */
    fun today(): LocalDate
}
