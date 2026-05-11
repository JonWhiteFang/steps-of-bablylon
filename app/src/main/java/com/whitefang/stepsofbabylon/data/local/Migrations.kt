package com.whitefang.stepsofbabylon.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit Room migrations for [AppDatabase]. Add a new [Migration] object
 * for every schema version bump and register it in [AppDatabase.Migrations.ALL].
 *
 * Schema exports live in `app/schemas/` — always commit them after bumping
 * the database version.
 */
object AppMigrations {

    /**
     * v7 → v8: Adds [DailyStepRecordEntity.battleStepsEarned] to track the
     * per-day count of Steps awarded by in-battle enemy kills. Defaults to 0
     * for all existing rows.
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE daily_step_record " +
                    "ADD COLUMN battleStepsEarned INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    /**
     * v8 → v9: Adds the `billing_receipt` table — the local Play Billing idempotency store
     * keyed by `purchaseToken`. Introduced by C.5 PR 1 / ADR-0005 to guarantee wallet credits
     * run exactly once per purchase across crash/retry boundaries. No existing rows to migrate;
     * the table is created empty.
     *
     * Schema mirrors [BillingReceiptEntity] — any field change requires a new migration and a
     * schema version bump.
     */
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `billing_receipt` (
                    `purchaseToken` TEXT NOT NULL,
                    `orderId` TEXT DEFAULT NULL,
                    `productId` TEXT NOT NULL,
                    `purchaseTime` INTEGER NOT NULL,
                    `granted` INTEGER NOT NULL DEFAULT 0,
                    `grantedAt` INTEGER DEFAULT NULL,
                    `acknowledged` INTEGER NOT NULL DEFAULT 0,
                    `acknowledgedAt` INTEGER DEFAULT NULL,
                    `consumed` INTEGER NOT NULL DEFAULT 0,
                    `consumedAt` INTEGER DEFAULT NULL,
                    PRIMARY KEY(`purchaseToken`)
                )
                """.trimIndent(),
            )
        }
    }

    /** All migrations in version order. Wire this into the Room builder. */
    val ALL: Array<Migration> = arrayOf(MIGRATION_7_8, MIGRATION_8_9)
}
