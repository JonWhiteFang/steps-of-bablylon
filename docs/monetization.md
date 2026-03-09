# Monetization

## Hard Rule

Steps can **never** be purchased with real money. This is non-negotiable. All monetization is cosmetic or convenience.

## Revenue Streams

### In-App Purchases

| Product | Price | Type | What Player Gets |
|---|---|---|---|
| Gem Pack (Small) | $0.99 | Consumable | 50 Gems |
| Gem Pack (Medium) | $4.99 | Consumable | 300 Gems |
| Gem Pack (Large) | $9.99 | Consumable | 700 Gems |
| Ad Removal | $3.99 | One-time | No more optional ads |
| Season Pass | $4.99/month | Subscription | Daily bonus Gems, exclusive cosmetics, 1 free Lab rush/day |

### Cosmetics

| Category | Price Range | Examples |
|---|---|---|
| Ziggurat Skins | $0.99–$2.99 | Obsidian, Crystal, Golden, seasonal themes |
| Projectile Effects | $0.99–$1.99 | Fire trails, lightning arcs, star particles |
| Enemy Skins | $0.99 | Reskinned enemy types (cosmetic only) |

### Optional Reward Ads

| Trigger | Reward | Frequency |
|---|---|---|
| Post-round | 1 bonus Gem | After each round |
| Post-round | Double Power Stone rewards | After each round |
| Daily | Free Card Pack | Once per day |

Ads are always optional. They never interrupt gameplay or walking. No forced ads ever.

## Gem Economy

Gems are the bridge currency between free and paid:

- Free sources: daily login streaks, walking milestones, daily missions
- Paid source: Gem Packs (IAP)
- Spent on: Card Packs, Lab slot unlocks (up to 4), Lab rush timers, cosmetics

### Gem Earning Rate (Free)

| Source | Amount | Frequency |
|---|---|---|
| Daily login | 1–5 Gems | Daily (streak scaling) |
| Daily missions (3/day) | 2–10 Gems each | Daily |
| Walking milestones | 10–500 Gems | One-time |
| Supply Drops | 1–3 Gems | Per drop |
| Post-round ad | 1 Gem | Per round |

Estimated free Gem income: ~15–30/day for an active player.

### Gem Spending

| Item | Cost | Notes |
|---|---|---|
| Card Pack (Common) | 50 Gems | 3 cards, mostly Common |
| Card Pack (Rare) | 150 Gems | 3 cards, guaranteed 1+ Rare |
| Card Pack (Epic) | 500 Gems | 3 cards, guaranteed 1 Epic |
| Lab Slot Unlock | 200 Gems | Slots 2–4 |
| Lab Rush (instant complete) | 50–200 Gems | Scales with remaining time |

## Season Pass

Monthly subscription ($4.99):
- 10 bonus Gems/day
- Exclusive cosmetic each month
- 1 free Lab rush per day
- Season Pass badge on profile

No gameplay advantages beyond convenience. A non-subscriber can earn everything through play.

## Design Principles

- No pay-to-win. Steps are effort-gated, not money-gated.
- No loot boxes with real money — Card Packs use Gems (earnable for free).
- No energy systems or play-gating.
- No FOMO mechanics — missing a day has zero penalty.
- Cosmetics are the primary revenue driver.
- Ads are opt-in rewards, never interruptions.

## Implementation Status

### Architecture

All billing and ad functionality uses interface-based abstractions in the domain layer (pure Kotlin), with stub implementations in the data layer:

| Interface | Stub Implementation | Location |
|---|---|---|
| `BillingManager` | `StubBillingManager` | `data/billing/` |
| `RewardAdManager` | `StubRewardAdManager` | `data/ads/` |

DI bindings in `di/BillingModule.kt` and `di/AdModule.kt` wire stubs to interfaces. To integrate real SDKs, create new implementations and swap the DI bindings — no other code changes needed.

### What's Implemented (Stub)

- Gem Pack purchases (3 tiers) — simulated with 500ms delay, credits Gems immediately
- Ad Removal — sets flag, hides all ad UI across the app
- Season Pass — sets flag + 30-day expiry, awards 10 bonus Gems/day via TrackDailyLogin, 1 free Lab rush/day
- Post-round reward ads — +1 Gem and double PS buttons (simulated with 1s delay)
- Daily free Card Pack ad — once per day, opens Common pack without Gem cost
- Cosmetic store — 7 placeholder items (3 ziggurat skins, 2 projectile effects, 2 enemy skins), purchase/equip/unequip

### What's Deferred (Real SDK Integration)

- Google Play Billing Library v7 (replace StubBillingManager)
- AdMob SDK (replace StubRewardAdManager)
- Purchase verification and receipt validation
- Subscription renewal, grace periods, billing retries
- Real cosmetic content and visual application in battle
- Play Console product configuration and test tracks
- Ad mediation for fill rate optimization
