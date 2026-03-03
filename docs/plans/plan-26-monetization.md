# Plan 26 — Monetization & Ads

**Status:** Not Started
**Dependencies:** Plan 17 (Cards System)
**Layer:** `presentation/` + `data/`

---

## Objective

Implement all monetization features: optional reward ads (post-round Gem, double Power Stones, free Card Pack), ad removal IAP, Gem pack IAPs, Season Pass subscription, and cosmetic theme purchases. All monetization is cosmetic or convenience — Steps are never purchasable.

Reference: `docs/monetization.md` for full spec.

---

## Task Breakdown

### Task 1: Google Play Billing Setup

Create `data/billing/BillingManager.kt`:
- Google Play Billing Library integration
- Connect to `BillingClient`
- Query available products (consumables, subscriptions, one-time)
- Handle purchase flow and acknowledgment
- Verify purchases locally (offline game, no server verification needed)

---

### Task 2: Gem Pack IAPs

Create `domain/usecase/PurchaseGemPack.kt`:
- Three tiers: Small ($0.99 → 50 Gems), Medium ($4.99 → 300 Gems), Large ($9.99 → 700 Gems)
- Consumable products — can be purchased multiple times
- Credits Gems to `PlayerRepository` on successful purchase

---

### Task 3: Ad Removal IAP

Create `domain/usecase/PurchaseAdRemoval.kt`:
- One-time purchase ($3.99)
- Sets `adRemoved: Boolean` flag on player profile
- Hides all ad buttons and ad-related UI when active

---

### Task 4: Season Pass Subscription

Create `domain/usecase/ManageSeasonPass.kt`:
- Monthly subscription ($4.99)
- Benefits: 10 bonus Gems/day, exclusive cosmetic, 1 free Lab rush/day
- Track subscription status via `BillingClient.queryPurchasesAsync()`
- Daily Gem bonus applied in `TrackDailyLogin`
- Free Lab rush tracked per day

---

### Task 5: Reward Ad Integration

Create `data/ads/RewardAdManager.kt`:
- AdMob (or similar) rewarded video ads
- Three ad placements:
  - Post-round: watch ad → 1 bonus Gem
  - Post-round: watch ad → double Power Stone rewards
  - Daily: watch ad → free Card Pack (once/day)
- Ads never auto-play or interrupt gameplay
- Hidden when ad removal purchased

---

### Task 6: Ad UI Integration

Update `PostRoundScreen`:
- "Watch Ad for +1 Gem" button
- "Watch Ad to Double Power Stones" button
- Both hidden if ad removal purchased

Create `presentation/cards/FreeCardPackButton.kt`:
- Daily free Card Pack via ad (once/day)
- Shows cooldown timer if already used today

---

### Task 7: Cosmetic Store

Create `presentation/store/CosmeticStoreScreen.kt`:
- Grid of purchasable cosmetics:
  - Ziggurat skins ($0.99–$2.99)
  - Projectile effects ($0.99–$1.99)
  - Enemy skins ($0.99)
- Each shows preview, price, and "Buy" button
- Purchased cosmetics marked as owned
- Equip/unequip from store or settings

Create `presentation/store/CosmeticStoreViewModel.kt`.

Create `data/local/CosmeticEntity.kt` — tracks owned and equipped cosmetics.

---

### Task 8: Database & Profile Updates

Update `PlayerProfileEntity`:
- Add `adRemoved: Boolean` (default false)
- Add `seasonPassActive: Boolean` (default false)
- Add `seasonPassExpiry: Long?`
- Migration

---

## File Summary

```
data/billing/
└── BillingManager.kt           (new)

data/ads/
└── RewardAdManager.kt          (new)

data/local/
├── CosmeticEntity.kt          (new)
├── CosmeticDao.kt             (new)
├── PlayerProfileEntity.kt     (update)
└── AppDatabase.kt             (update — migration)

domain/usecase/
├── PurchaseGemPack.kt         (new)
├── PurchaseAdRemoval.kt       (new)
└── ManageSeasonPass.kt        (new)

presentation/
├── store/
│   ├── CosmeticStoreScreen.kt (new)
│   └── CosmeticStoreViewModel.kt (new)
├── battle/
│   └── PostRoundScreen.kt     (update — ad buttons)
└── cards/
    └── FreeCardPackButton.kt  (new)
```

## Completion Criteria

- Gem packs purchasable via Google Play Billing (3 tiers)
- Ad removal one-time purchase hides all ad UI
- Season Pass subscription grants daily Gems and free Lab rush
- Reward ads play correctly and grant rewards on completion
- Ads never interrupt gameplay — always opt-in
- Cosmetic store displays and sells skins
- Purchased cosmetics persist and can be equipped
- Steps are never purchasable (hard rule enforced)
