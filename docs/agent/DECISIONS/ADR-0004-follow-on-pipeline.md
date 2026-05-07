# ADR-0004: FollowOnPipeline extraction from DailyStepManager

**Date:** 2026-05-07
**Status:** Proposed (stub — concrete decision deferred to B.4 PR 1 scoping)

## Context

`DailyStepManager` has grown to 12 constructor parameters and orchestrates five conceptually separate concerns after a step credit:

1. Widget state update (`WidgetUpdateHelper`).
2. Supply-drop generation and notification (`GenerateSupplyDrop` + `WalkingEncounterRepository` + `SupplyDropNotificationManager`).
3. Economy rewards (`TrackDailyLogin` + `TrackWeeklyChallenge`).
4. Walking mission progress (`DailyMissionDao`).
5. Season Pass Gem bonus — currently threaded via a `playerRepository.observeProfile().first()` read inside `runFollowOnPipeline` (tactical patch from Phase A.6).

The manager's core responsibility — anti-cheat-gated step crediting — is ~40 lines; the follow-on pipeline is ~80 lines across four `try / catch (_: Exception) {}` blocks. This violates single-responsibility and makes it hard to test the follow-on stages in isolation from the rate limiter and velocity analyser.

Phase 14 Part 1 (RO-04) and Phase 4 §4 both propose an extraction. Phase 11 Q8 scheduled the Season Pass leak as a tactical patch (landed in A.6) and named B.4 PR 4 as the place where that duplication should be removed.

## Decision (stub)

**TBD.** The concrete design — class shape, Hilt wiring, test-splitting strategy, exact method signatures — will be finalised when B.4 PR 1 is scoped. The decision recorded here is only the commitment to:

1. Extract the five follow-on stages into a new `data/sensor/FollowOnPipeline.kt` as `@Singleton class FollowOnPipeline @Inject constructor(...)`.
2. Shrink `DailyStepManager`'s constructor from 12 parameters to 6 (rate limiter, velocity analyser, anti-cheat preferences, step repository, player repository, and the new `FollowOnPipeline`).
3. Preserve behaviour parity — every assertion currently covered by `DailyStepManagerTest` must pass verbatim under the new composition, split between `DailyStepManagerTest` (crediting-only) and a new `FollowOnPipelineTest` (follow-on-only).
4. Move `dropState: DropGeneratorState` ownership from `DailyStepManager` into `FollowOnPipeline`. Both types are `@Singleton`, so lifetime is identical — flag in review.
5. Remove the A.6 tactical patch (`observeProfile().first()` in `DailyStepManager`) in a final PR once the pipeline owns the single `TrackDailyLogin` call site with Season Pass flags.

## Rationale

- **Single responsibility.** `DailyStepManager` becomes "credit steps safely"; `FollowOnPipeline` becomes "cascade effects of a credit". The two can evolve and be tested independently.
- **Unblocks B.5.** `FollowOnPipeline` is the natural first consumer of the new `UpdateMissionProgress` use case (RO-05). B.5 can't land cleanly without something that owns the walking-mission call site outside a VM.
- **Constructor shrink is real.** 12-parameter constructors are a code smell and were called out explicitly in Phase 8 `architecture_analysis.md` as a "fat module".
- **Mechanical extraction, not a behaviour change.** Ordering of the five stages and their exception-handling strategy are preserved. The PR is a move, not a rewrite.

## Consequences

- New file `data/sensor/FollowOnPipeline.kt` becomes a Hilt `@Singleton`. Existing `SensorModule`-style DI wiring gets one new `@Provides` or `@Inject constructor` (style TBD in PR 1).
- `DailyStepManager` loses 6 constructor dependencies — any test constructing it manually must be updated (`DailyStepManagerTest`).
- Test mass moves: roughly half of `DailyStepManagerTest`'s existing assertions migrate into `FollowOnPipelineTest`. The crediting-only tests stay.
- `dropState` is no longer visible to `DailyStepManager`. Any external reader (none today) would need to go through `FollowOnPipeline`.
- The five `try / catch (_: Exception) {}` blocks stay. Converting them to proper error surfacing is a separate, larger discussion — out of scope for B.4.

## Non-goals / future work

- **Do not** collapse the five try-catches into one. The isolation-per-stage is deliberate — a widget failure must not swallow a supply-drop generation, and vice versa.
- **Do not** turn `FollowOnPipeline` into a chain-of-responsibility or an observer bus. The explicit ordering (widget → drop → economy → missions) is a documented requirement per the step-tracking docs.
- **Do not** migrate the anti-cheat-gated ceiling or rate-limiter logic. Those stay on `DailyStepManager`.
- Error-surfacing (logging the swallowed exceptions) is worth considering but is a separate Phase-11 Q item (surfacing anti-cheat effects on the Stats screen, Phase 4 §5). Not B.4 scope.

## Open questions (to be resolved in B.4 PR 1 scoping)

- **Dependency direction.** Does `FollowOnPipeline` take `DailyStepManager` dependencies as constructor parameters (simpler, duplicates the Hilt graph), or does it accept `DailyStepSummary` / current state as method parameters (purer, requires `DailyStepManager` to assemble a DTO)?
- **Timestamp plumbing.** After B.1 (TimeProvider landed), should `FollowOnPipeline.run()` take `timeProvider` by constructor or accept `now: Instant` per call?
- **Return contract.** `run()` as `suspend fun` returning `Unit`, or returning a structured `FollowOnResult` for observability?
- **Mission progress path.** Does B.5 PR 1 or B.4 PR 2 own the first `UpdateMissionProgress` wiring? Roadmap §B.4 PR 2 says "composes with B.5" — PR boundary needs to be nailed down before the first B.4 commit.

All four will be decided in the PR description for B.4 PR 1 (which will upgrade this ADR from "Proposed (stub)" to "Accepted" with concrete answers).

## References

- `devdocs/evolution/implementation_roadmap.md` §B.4
- `devdocs/evolution/refactoring_opportunities.md` RO-04
- `devdocs/archaeology/5_things_or_not.md` §4
- `devdocs/archaeology/architecture_analysis.md` "fat modules" section
- Phase A.6 (Season Pass flags in background) — this ADR's prerequisite regression coverage
