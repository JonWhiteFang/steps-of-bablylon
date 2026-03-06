# Database Schema

Room is the single source of truth for all game state. Offline-first — no server required.

## Entities

### PlayerProfile

Primary player record. One row per player (single-player game).

| Column | Type | Notes |
|---|---|---|
| id | Int (PK) | Always 1 |
| totalStepsEarned | Long | Lifetime steps earned |
| currentStepBalance | Long | Spendable step balance |
| gems | Long | Premium currency |
| powerStones | Long | UW currency |
| cardDust | Long | Card Dust currency |
| currentTier | Int | Selected play tier |
| highestUnlockedTier | Int | Highest tier unlocked (default 1) |
| bestWavePerTier | String (JSON) | Map<Int, Int> serialized |
| createdAt | Long | Epoch millis |
| lastActiveAt | Long | Epoch millis |

### WorkshopUpgrade

One row per upgrade type (23 rows total).

| Column | Type | Notes |
|---|---|---|
| upgradeType | String (PK) | UpgradeType enum name |
| level | Int | Current level (0 = not purchased) |

### LabResearch

One row per research type (10 rows total).

| Column | Type | Notes |
|---|---|---|
| researchType | String (PK) | ResearchType enum name |
| level | Int | Completed level |
| startedAt | Long? | Epoch millis, null if idle |
| completesAt | Long? | Epoch millis, null if idle |

### CardInventory

One row per owned card.

| Column | Type | Notes |
|---|---|---|
| id | Int (PK, auto) | |
| cardType | String | CardType enum name |
| level | Int | 1–5 |
| isEquipped | Boolean | Max 3 equipped |

### UltimateWeaponState

One row per unlocked UW.

| Column | Type | Notes |
|---|---|---|
| weaponType | String (PK) | UltimateWeaponType enum name |
| level | Int | Upgrade level |
| isEquipped | Boolean | Max 3 equipped |

### DailyStepRecord

Historical step data, one row per day.

| Column | Type | Notes |
|---|---|---|
| date | String (PK) | ISO date (yyyy-MM-dd) |
| sensorSteps | Long | Raw TYPE_STEP_COUNTER |
| healthConnectSteps | Long | Health Connect reported |
| creditedSteps | Long | After anti-cheat validation |
| escrowSteps | Long | Steps held pending cross-validation |
| escrowSyncCount | Int | Number of sync attempts for escrow resolution |
| activityMinutes | String (JSON) | Map<ActivityType, Int> |
| stepEquivalents | Long | From Activity Minute Parity |

### WalkingEncounter

Unclaimed and historical supply drops.

| Column | Type | Notes |
|---|---|---|
| id | Int (PK, auto) | |
| triggerType | String | What triggered the drop |
| rewardType | String | Steps/Gems/PowerStones/CardDust |
| rewardAmount | Int | |
| claimed | Boolean | |
| createdAt | Long | Epoch millis |
| claimedAt | Long? | Epoch millis |

## Relationships

```
PlayerProfile (1) ──── (*) WorkshopUpgrade
PlayerProfile (1) ──── (*) LabResearch
PlayerProfile (1) ──── (*) CardInventory
PlayerProfile (1) ──── (*) UltimateWeaponState
PlayerProfile (1) ──── (*) DailyStepRecord
PlayerProfile (1) ──── (*) WalkingEncounter
```

All relationships are implicit (single player, no foreign keys needed). Queries filter by type/date.

## DAOs

Each entity gets its own DAO:

- `PlayerProfileDao` — CRUD + balance updates
- `WorkshopDao` — get/update levels, bulk query by category
- `LabDao` — active research queries, completion checks
- `CardDao` — inventory, equipped loadout, dust operations
- `UltimateWeaponDao` — unlocked list, equipped loadout
- `DailyStepDao` — insert/update daily, history range queries
- `WalkingEncounterDao` — unclaimed list, claim, history

## Migration Strategy

- Export schemas to `app/schemas/` (commit these files)
- Use Room auto-migrations where possible
- Write manual migrations for complex changes (column renames, data transforms)
- Version numbering: increment by 1 per plan that touches the schema
- Test migrations with `MigrationTestHelper` in instrumented tests
- Current schema version: 2
- v1→v2: Added `highestUnlockedTier` column to `player_profile` (Plan 13). Uses `fallbackToDestructiveMigration` during development.

## Type Converters

- JSON maps (bestWavePerTier, activityMinutes): `org.json.JSONObject` (Android SDK built-in)
- Enums: stored as String (enum name), no converter needed — entities use String columns
- Dates: stored as Long (epoch millis)

## Notes

- `RoundState` is NOT persisted — it's transient, held in ViewModel during battle
- `Cash` is NOT persisted — it resets each round
- `cardDust: Long` is stored on `PlayerProfile`

## Security

- Database is encrypted at rest using SQLCipher (`net.zetetic:sqlcipher-android`)
- Encryption passphrase is generated randomly on first run, encrypted with an Android Keystore AES-256-GCM key, and stored in SharedPreferences
- Uses `fallbackToDestructiveMigration()` during pre-release development
