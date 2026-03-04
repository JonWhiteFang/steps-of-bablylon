---
inclusion: always
---

# Project Memory System (Always-On)

## Canonical memory sources (treat these as truth)
- docs/agent/START_HERE.md
- docs/agent/STATE.md
- docs/agent/CONSTRAINTS.md
- docs/agent/RUN_LOG.md
- docs/agent/DECISIONS/*.md (ADRs)

## Rules
- Do NOT rely on chat history as the project source of truth.
- Before planning or changing code: read the memory spine + check git state.
- After finishing work: update STATE.md + append RUN_LOG.md.
- If you made/changed a meaningful decision: create/update an ADR.
- Keep STATE.md to one page. Push detail into RUN_LOG/ADRs.
