# Battle Formulas

All math used in combat, economy, and progression. Single reference for balance tuning.

## Workshop Upgrade Cost

```
cost = ceil(baseCost × scaling ^ currentLevel)
```

- `baseCost` and `scaling` are per-UpgradeType (see GDD §4)
- Returns `Long` (rounded up)
- Used for both Workshop (Step-funded) and in-round (Cash-funded) upgrades

### Example: Damage upgrade at level 10

```
50 × 1.12^10 = 50 × 3.1058 = 155.29 → 156 Steps
```

## Stats Resolution

Workshop (permanent) and in-round (temporary) upgrades combine multiplicatively:

```
effectiveStat = baseStat × (1 + workshopBonus) × (1 + inRoundBonus)
```

This applies to all stats: damage, attack speed, health, regen, etc.

## Damage Calculation

```
rawDamage = baseDamage × (1 + damageLevel × 0.02) × (1 + inRoundDamageLevel × 0.02)

isCrit = random() < critChance
critChance = min(critChanceLevel × 0.005, 0.80)
critMultiplier = 2.0 + critFactorLevel × 0.1

finalDamage = isCrit ? rawDamage × critMultiplier : rawDamage
```

### Damage/Meter Bonus

```
distanceBonus = 1 + (distanceToEnemy / maxRange) × damagePerMeterLevel × 0.01
finalDamage *= distanceBonus
```

## Defense Calculation

```
damageReduction = defensePercentLevel × 0.003  // cap 75%
flatBlock = defenseAbsoluteLevel × flatBlockPerLevel

damageTaken = max(0, incomingDamage × (1 - damageReduction) - flatBlock)
```

Defense % is diminishing — each point adds 0.3% but the cap is 75%.

## Health & Regen

```
maxHealth = baseHealth × (1 + healthLevel × 0.03) × (1 + inRoundHealthLevel × 0.03)
regenPerSecond = baseRegen × (1 + regenLevel × 0.02)
```

## Lifesteal

```
healAmount = finalDamage × min(lifestealLevel × 0.002, 0.15)
```

Cap: 15% of damage dealt.

## Knockback

```
knockbackForce = baseKnockback × (1 + knockbackLevel × 0.02)
```

Reduced by Knockback Resistance battle condition at higher tiers.

## Death Defy

```
deathDefyChance = min(deathDefyLevel × 0.01, 0.50)
```

On lethal hit: roll against chance. If success, survive at 1 HP. Once per hit.

## Thorn Damage

```
reflectedDamage = incomingDamage × thornLevel × 0.01
```

Reduced by Thorn Resistance battle condition at higher tiers.

## Orbs

Orbiting projectiles that damage nearby enemies. Count = orbLevel (cap 6). Damage scales with orb level. Reduced by Orb Resistance battle condition.

## Multishot & Bounce

```
targets = 1 + floor(multishotLevel / 20)  // cap 5
bounces = floor(bounceShotLevel / 15)      // cap 4
```

Each bounce deals full damage to the next target.

## Cash Economy (In-Round)

```
cashFromKill = baseKillCash × tierCashMultiplier × (1 + cashBonusLevel × 0.03)
cashPerWave = baseCashPerWave + cashPerWaveLevel × flatBonusPerLevel
interest = min(heldCash × interestLevel × 0.005, heldCash × 0.10)  // cap 10%
freeUpgradeChance = min(freeUpgradeLevel × 0.01, 0.25)             // cap 25%
```

### Tier Cash Multipliers

| Tier | Multiplier |
|---|---|
| 1 | 1.0× |
| 2 | 1.8× |
| 3 | 2.6× |
| 4 | 3.4× |
| 5 | 4.2× |
| 6 | 5.0× |
| 7 | 6.0× |
| 8 | 7.2× |
| 9 | 8.5× |
| 10 | 10.0× |

## Wave Timing

```
spawnPhaseDuration = 26 seconds
cooldownDuration = 9 seconds
totalWaveDuration = 35 seconds (at 1x speed)
```

Speed controls: 1x / 2x / 4x (multiply game tick rate).

## Enemy Scaling

Base stats scale per wave:

```
enemyHealth = baseHealth × healthMultiplier × waveScalingFactor(wave)
enemyDamage = baseDamage × damageMultiplier × waveScalingFactor(wave)
```

### Enemy Type Multipliers

| Type | Speed | Health | Damage |
|---|---|---|---|
| Basic | 1.0× | 1.0× | 1.0× |
| Fast | 2.0× | 0.5× | 0.7× |
| Tank | 0.5× | 5.0× | 2.0× |
| Ranged | 0.8× | 0.8× | 1.2× |
| Boss | 0.3× | 20.0× | 3.0× |
| Scatter | 1.2× | 1.5× | 0.8× |

Bosses spawn every 10 waves (every 7 at Tier 9+ with More Bosses condition).

## Overdrive Effects

| Type | Cost | Duration | Effect |
|---|---|---|---|
| Assault | 500 Steps | 60s | 2× Attack Speed + 1.5× Damage |
| Fortress | 500 Steps | 60s | 2× Health Regen + 50% Damage Reduction |
| Fortune | 300 Steps | 60s | 3× Cash earned |
| Surge | 750 Steps | 60s | All UW cooldowns reset |

Once per round. Stacks multiplicatively with existing stats.

## Lab Research Scaling

```
researchCost = baseCostSteps × scalingFactor ^ level
researchTime = baseTimeHours × timeScalingFactor ^ level
```

Research effects are additive per level (e.g., Damage Research: +5% per level).

## Step Multiplier

```
bonusSteps = rawSteps × min(stepMultiplierLevel × 0.01, 1.00)
totalSteps = rawSteps + bonusSteps
```

Cap: 100% bonus (double steps). Early investment compounds over time.
