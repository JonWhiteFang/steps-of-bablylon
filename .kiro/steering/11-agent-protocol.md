---
inclusion: always
---

# Agent Protocol

## Context Preflight (mandatory at session start)
1. Read docs/agent/START_HERE.md, STATE.md, CONSTRAINTS.md
2. Review latest RUN_LOG entry and any ADRs referenced in STATE.md
3. Check repo state via git:
   - `git status` (clean? what changed?)
   - `git log -n 10 --oneline` (recent context)
4. Output a brief "Session Brief" (~10 bullets):
   - What the project is
   - Current state
   - Constraints/invariants
   - Today's objective
   - Risks/unknowns

## PR Task-List Convention (mandatory for every code-changing PR)

Every task list for a PR that changes production code, tests, or configuration MUST include
these two steps, in this order, immediately before the commit step:

1. **Sync current-state docs** affected by the change — this task runs BEFORE the
   STATE.md / RUN_LOG.md update.
2. **Update STATE.md + append RUN_LOG.md entry**.

Current-state docs to audit for every PR (touch only if the PR actually invalidates them):

- `AGENTS.md` — test count, fakes list, coverage summary. ALWAYS update when test count changes.
- `CHANGELOG.md` — add a new section for the PR; update `Current state` block if phase status,
  test count, or roadmap items shifted.
- `.kiro/steering/source-files.md` — add entries for new files; update existing entries
  when a file's responsibility shape changed (new method, new dependency, new capability).
- `.kiro/steering/structure.md` — update when new modules, directories, or architectural
  elements land.
- `docs/database-schema.md` — only if Room schema or migration changed.
- `.kiro/steering/tech.md`, `.kiro/steering/lib-*.md` — only if dependency versions, conventions,
  or library-specific patterns changed.
- `README.md` — only if user-facing build/run instructions changed.

Historical artifacts — **NEVER modify** as part of a current-PR doc sweep:

- `docs/agent/RUN_LOG.md` prior entries (appending a new entry for the current PR is fine;
  editing old entries is not).
- `docs/plans/plan-R*.md`, `docs/plans/plan-R2*.md` — historical at their authoring date.
- `docs/external-reviews/*` — historical reviews at review date.
- `devdocs/archaeology/*`, `devdocs/evolution/*`, `smoke_tests/*` — historical per HEAD pin.
- Individual `docs/agent/DECISIONS/ADR-*.md` files — amend status only if explicitly
  warranted by the PR; otherwise leave.

Rationale: this prevents the drift that accumulates when a run of PRs lands in sequence
and nobody explicitly checks the current-state docs. The sync becomes routine, not an
afterthought, and each PR's commit diff tells a complete story.

## End-of-Run Memory Writes (mandatory before session ends)

1. Current-state docs synced per the PR Task-List Convention above.
2. Update `docs/agent/STATE.md` (what changed + what's next).
3. Append `docs/agent/RUN_LOG.md` with what you did and what remains.
4. Add/update ADR if you made a non-trivial decision.
