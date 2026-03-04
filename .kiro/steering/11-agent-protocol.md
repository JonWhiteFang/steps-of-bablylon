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

## End-of-Run Memory Writes (mandatory before session ends)
- Update docs/agent/STATE.md (what changed + what's next)
- Append docs/agent/RUN_LOG.md with what you did and what remains
- Add/update ADR if you made a non-trivial decision
