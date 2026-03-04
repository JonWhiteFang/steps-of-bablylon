# Kiro CLI Project Memory Playbook (Repo‑Backed “Brain” for Agents)

This guide gives you a **repeatable project-memory system** for Kiro CLI agents, so they don’t keep re‑deriving “what happened last time”.

It assumes:
- You run Kiro CLI from the repo root.
- The agent can run local `git` and use Kiro’s built‑in `grep/glob/read/write/shell` tools.

---

## 1) Target outcome

After this setup:

- Every session starts with a **Context Preflight** (reads the same “spine” files + recent git state).
- The agent executes work with minimal re‑explaining.
- Every session ends with **Memory Writes** (STATE + RUN_LOG + ADR if needed).
- Optional: Kiro indexes your docs/code as a **knowledge base** for semantic search (without bloating the context window).

---

## 2) Repo structure to add (memory “spine”)

Add these paths to your repository:

```
.kiro/
  steering/
    00-product.md
    01-tech.md
    02-structure.md
    10-project-memory.md
    11-agent-protocol.md
  agents/
    project-memory.json

docs/
  agent/
    START_HERE.md
    STATE.md
    CONSTRAINTS.md
    RUN_LOG.md
    DECISIONS/
      ADR-0001-template.md
    state.json
```

### Why split between `.kiro/steering/` and `docs/agent/`?
- `.kiro/steering/` is Kiro’s **persistent guidance layer** (always/conditionally included). Keep it **stable + short**.
- `docs/agent/` is your **living state + history** (updated every run).

---

## 3) Create the steering files (persistent “truth”)

### 3.1 Foundational steering docs (if you don’t already have them)
Create these files in `.kiro/steering/`:

- `00-product.md`: what you’re building and who it’s for.
- `01-tech.md`: stack, tooling, constraints (platforms, languages, CI stance, etc.).
- `02-structure.md`: folder/module boundaries, naming conventions, patterns.

> Tip: In Kiro, steering files can be “always included” via YAML front matter.

### 3.2 Add a project memory steering file: `.kiro/steering/10-project-memory.md`

Paste this template:

```md
---
inclusion: always
---

# Project Memory System (Always-On)

## Canonical memory sources (the agent MUST treat these as truth)
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
```

### 3.3 Add an agent protocol steering file: `.kiro/steering/11-agent-protocol.md`

```md
---
inclusion: always
---

# Agent Protocol

## Context Preflight (mandatory)
1. Read docs/agent/START_HERE.md, STATE.md, CONSTRAINTS.md
2. Review latest RUN_LOG entry and any ADRs referenced in STATE.md
3. Check repo state via git:
   - status (clean? what changed?)
   - last 10 commits (high-level context)
4. Output a brief "Session Brief" (10 bullets):
   - What the project is
   - Current state
   - Constraints/invariants
   - Today’s objective
   - Risks/unknowns

## End-of-Run Memory Writes (mandatory)
- Update docs/agent/STATE.md (what changed + what’s next)
- Append docs/agent/RUN_LOG.md with what you did and what remains
- Add/update ADR if you made a non-trivial decision
```

---

## 4) Create the living memory docs in `docs/agent/`

### 4.1 `docs/agent/START_HERE.md` (the “contract”)

```md
# START HERE (Agent Contract)

## What this project is
- <1–2 lines>
- Target user: <...>
- Primary goals: <...>

## Non-negotiable constraints
- <e.g., determinism, platform support, security constraints>
- <things the agent must never do>

## How to work in this repo
- Build/test commands: <...>
- Lint/format commands: <...>
- Branch/PR conventions: <...>

## Where project memory lives
- STATE: docs/agent/STATE.md
- Constraints: docs/agent/CONSTRAINTS.md
- Run history: docs/agent/RUN_LOG.md
- Decisions: docs/agent/DECISIONS/
```

### 4.2 `docs/agent/STATE.md` (one-page snapshot)

```md
# Project State

## Current objective
- (1) <most important next outcome>
- (2) <...>

## What works
- <...>

## Known issues / debt
- [ ] BUG-001: <summary> (owner: <...>)
- [ ] BUG-002: <summary>

## Top priorities (next 5)
1. <...>
2. <...>

## Next actions (explicit order)
1. <concrete step>
2. <concrete step>

## Do-not-touch / fragile zones
- <paths/modules>
- <reasons>

## References
- ADRs: ADR-000X, ADR-000Y
- Last run: <date> (see RUN_LOG)
```

### 4.3 `docs/agent/CONSTRAINTS.md` (invariants)

```md
# Constraints & Invariants

## Architecture invariants
- <module boundaries, layering, API contracts>

## Determinism / correctness rules
- <...>

## Security / compliance rules
- <...>

## Performance budgets
- <...>

## “Never do” list
- <...>
```

### 4.4 `docs/agent/RUN_LOG.md` (append-only)

```md
# Run Log

## YYYY-MM-DD — <short title>
- Goal:
- Plan:
- Changes made:
- Commands/tests run + results:
- Open questions / blockers:
- Follow-ups created:
- Memory updated: STATE ✅ / ADR ✅ / (notes)
```

### 4.5 ADR template: `docs/agent/DECISIONS/ADR-0001-template.md`

```md
# ADR-0001: <Decision Title>

## Context
- What problem are we solving?

## Decision
- What did we decide?

## Alternatives considered
- A:
- B:
- C:

## Consequences
- Positive:
- Negative / tradeoffs:
- Follow-ups:

## Links
- PR: <...>
- Commit(s): <...>
- Related ADRs: <...>
```

### 4.6 Optional machine-readable state: `docs/agent/state.json`

```json
{
  "currentObjective": "TBD",
  "topPriorities": [],
  "knownIssues": [],
  "doNotTouch": [],
  "references": {
    "lastRun": "",
    "adrs": []
  }
}
```

---

## 5) Create a Kiro CLI custom agent for this workflow

### 5.1 Create the agent (two ways)

**Option A (interactive in a chat session):**
1. Run `kiro-cli` (or `kiro-cli chat`)
2. Use: `/agent create`
3. Name: `project-memory`
4. Scope: Local (workspace)

**Option B (direct command):**
```bash
kiro-cli agent create project-memory
```

Either way, you’ll end up with a JSON file under:
```
.kiro/agents/project-memory.json
```

### 5.2 Replace contents with this hardened agent config

This config:
- loads the “spine” docs every run (`resources`)
- enables `read/glob/grep` freely (safe)
- restricts `write` to the memory folders by default
- allows limited, read-only git via `shell` allow-list
- optionally indexes docs/code as a knowledge base

```json
{
  "name": "project-memory",
  "description": "Keeps repo-backed memory up to date and enforces preflight + run logging.",
  "prompt": "file://.kiro/steering/11-agent-protocol.md",
  "tools": [
    "read",
    "write",
    "glob",
    "grep",
    "shell",
    "knowledge",
    "todo"
  ],
  "allowedTools": [
    "read",
    "glob",
    "grep",
    "knowledge"
  ],
  "toolsSettings": {
    "read": {
      "allowedPaths": [
        "./**"
      ]
    },
    "write": {
      "allowedPaths": [
        "./docs/agent/**",
        "./.kiro/steering/**",
        "./AGENTS.md"
      ],
      "deniedPaths": [
        "./.git/**",
        "./node_modules/**",
        "./dist/**",
        "./build/**"
      ]
    },
    "shell": {
      "allowedCommands": [
        "git status*",
        "git log*",
        "git show*",
        "git diff*",
        "git branch*",
        "git rev-parse*",
        "git remote -v",
        "git describe*"
      ],
      "deniedCommands": [
        "git commit*",
        "git push*",
        "git reset*",
        "git rebase*",
        "git cherry-pick*",
        "git clean*"
      ],
      "autoAllowReadonly": true
    },
    "grep": {
      "allowedPaths": [
        "./**"
      ]
    },
    "glob": {
      "allowedPaths": [
        "./**"
      ],
      "allowReadOnly": true
    }
  },
  "resources": [
    "file://AGENTS.md",
    "file://.kiro/steering/**/*.md",
    "file://docs/agent/START_HERE.md",
    "file://docs/agent/STATE.md",
    "file://docs/agent/CONSTRAINTS.md",
    "file://docs/agent/RUN_LOG.md",
    "file://docs/agent/DECISIONS/*.md"
  ],
  "hooks": {
    "agentSpawn": [
      { "command": "git status --porcelain=v1 -b" },
      { "command": "git log -n 10 --oneline --decorate" }
    ]
  }
}
```

> If your repo is huge, keep `resources` to only the smallest “spine” files and rely on the knowledge base approach below.

---

## 6) Optional: Add semantic retrieval via knowledge bases (recommended for large repos)

### 6.1 Enable knowledge in Kiro CLI settings
```bash
kiro-cli settings chat.enableKnowledge true
```

### 6.2 Add knowledge base indexing in the agent config
In `.kiro/agents/project-memory.json`, add (or extend) the `resources` field with a `knowledgeBase` object:

```json
{
  "resources": [
    "file://docs/agent/STATE.md",
    {
      "type": "knowledgeBase",
      "source": "file://./",
      "name": "RepoIndex",
      "description": "Repo-wide semantic index (docs + code)",
      "indexType": "best",
      "autoUpdate": true
    }
  ]
}
```

This lets the agent search “on demand” without stuffing everything into the context window.

---

## 7) Add `AGENTS.md` at repo root (fast “index card”)

Kiro supports `AGENTS.md` as always-included steering-like directives.

Create `AGENTS.md`:

```md
# Agent Notes (Repo Index)

## Memory spine (read first)
- docs/agent/START_HERE.md
- docs/agent/STATE.md
- docs/agent/CONSTRAINTS.md
- docs/agent/RUN_LOG.md
- docs/agent/DECISIONS/

## Operating rules
- Always do Context Preflight before planning.
- Always update STATE + RUN_LOG at end of run.
- Record meaningful decisions in ADRs.
```

---

## 8) How to run the workflow day-to-day

### Start a session with the memory agent
```bash
kiro-cli chat --agent project-memory
```

### Use this prompt pattern for any task
Paste this (edit the objective):

```text
Objective: <what you want done>

Constraints:
- Follow the Context Preflight and End-of-Run Memory Writes protocol.
- Use built-in grep/glob tools for search.
- Do not make changes outside allowed write paths unless you ask first.

Definition of done:
- The code/doc changes are complete.
- STATE.md updated.
- RUN_LOG appended.
- ADR added/updated if any non-trivial decision was made.
```

---

## 9) Optional: a lightweight “context pack” generator script

If you prefer a repeatable snapshot, create `scripts/context-pack.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

OUT="docs/agent/CONTEXT_PACK.md"
mkdir -p "$(dirname "$OUT")"

{
  echo "# Context Pack"
  echo
  echo "Generated: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  echo
  echo "## Git status"
  git status --porcelain=v1 -b || true
  echo
  echo "## Recent commits"
  git log -n 15 --oneline --decorate || true
  echo
  echo "## Diff (staged)"
  git diff --staged || true
  echo
  echo "## Diff (unstaged)"
  git diff || true
} > "$OUT"

echo "Wrote $OUT"
```

Then allow it in the agent’s shell allow-list (add `scripts/context-pack.sh`), or run it manually before starting a session.

---

## 10) “Definition of Done” checklist (for every agent run)

- [ ] Context Preflight completed (spine docs + git state)
- [ ] Plan is consistent with CONSTRAINTS.md + ADRs
- [ ] Changes implemented and validated (tests/lint as relevant)
- [ ] docs/agent/STATE.md updated (one page)
- [ ] docs/agent/RUN_LOG.md appended
- [ ] ADR created/updated if a meaningful decision happened
- [ ] Commit/PR includes memory updates (no “orphan runs”)

---

## 11) Troubleshooting / common failure modes

**Agent ignores memory files**
- Ensure `.kiro/steering/10-project-memory.md` and `11-agent-protocol.md` exist with `inclusion: always`
- Ensure the agent config `resources` includes the spine files.

**Too much context / slow startup**
- Remove broad globs from `resources`.
- Use a `knowledgeBase` index instead and keep file resources to 3–6 small files.

**Agent wants to change code outside allowedPaths**
- Keep `write.allowedPaths` strict.
- Allow expansion via an explicit instruction (or temporarily edit the agent config).

**Agent keeps re-opening decisions**
- Add an ADR and reference it from `STATE.md` under “References”.

---

## 12) What to implement first (fastest path)

- [ ] Create `.kiro/steering/10-project-memory.md` and `11-agent-protocol.md`
- [ ] Create `docs/agent/START_HERE.md`, `STATE.md`, `CONSTRAINTS.md`, `RUN_LOG.md`, ADR template
- [ ] Add `AGENTS.md`
- [ ] Create `.kiro/agents/project-memory.json`
- [ ] Start sessions with: `kiro-cli chat --agent project-memory`
