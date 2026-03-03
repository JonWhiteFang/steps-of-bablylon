# Room Database — Reference Guide

## Core Annotations

- `@Entity` — defines a table; use `@PrimaryKey`, `@ColumnInfo` for customization
- `@Dao` — interface with query methods
- `@Database` — abstract class extending `RoomDatabase`, lists entities and version

## DAO Patterns

- `@Query` returning `Flow<T>` — re-emits when observed tables change (reactive)
- `@Insert(onConflict = OnConflictStrategy.REPLACE)` — insert or replace
- `@Upsert` — insert if new, update if exists (preferred over manual check)
- `@Update`, `@Delete` — standard mutations
- Use `suspend` for one-shot operations; `Flow` for observable queries

```kotlin
@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = :id")
    fun observePlayer(id: Long): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = :id")
    suspend fun getPlayer(id: Long): PlayerProfileEntity?

    @Upsert
    suspend fun upsertPlayer(player: PlayerProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerProfileEntity)
}
```

## TypeConverters

- Use `@TypeConverter` for types Room can't store natively (enums, lists, custom objects)
- Register via `@TypeConverters` on the `@Database` class

```kotlin
class Converters {
    @TypeConverter
    fun fromCurrency(value: Currency): String = value.name

    @TypeConverter
    fun toCurrency(value: String): Currency = Currency.valueOf(value)
}
```

## Migrations

- Auto migrations: `@Database(autoMigrations = [@AutoMigration(from = 1, to = 2)])`
- Manual migrations for complex changes (column renames, data transforms)
- Export schemas to `app/schemas/` — commit these files
- Configure in build.gradle: `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`

## Flow Integration

- `Flow<List<T>>` queries automatically re-emit when underlying table data changes
- Collect in ViewModel, expose as `StateFlow` to Compose
- No manual invalidation needed — Room handles it

```kotlin
class PlayerRepositoryImpl(private val dao: PlayerDao) : PlayerRepository {
    override fun observeWallet(playerId: Long): Flow<PlayerWallet> =
        dao.observePlayer(playerId).map { it?.toWallet() ?: PlayerWallet() }
}
```

## Project Conventions

- Entity files: `*Entity.kt` in `data/local/`
- DAO files: `*Dao.kt` in `data/local/`
- Database: `AppDatabase.kt` in `data/local/`
- Room is the single source of truth for all game state
- Schema version starts at 1; increment with each migration
