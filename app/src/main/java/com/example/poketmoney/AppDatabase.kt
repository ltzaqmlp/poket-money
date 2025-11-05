package com.example.poketmoney

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * !!! 数据库升级 (v4 -> v5) !!!
 * 1. version 从 4 修改为 5
 */
@Database(
    entities = [Transaction::class, Ledger::class, Account::class],
    version = 5, // 1. 版本升级为 5
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // (无变化)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            // ... (迁移代码无变化)
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- 账本 (Ledger) 表 ---
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ledgers` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_ledgers_name` ON `ledgers` (`name`)"
                )

                // --- 交易 (Transaction) 表 ---
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `transactions_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`amount` REAL NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`category` TEXT, " +
                            "`date` INTEGER NOT NULL, " +
                            "`note` TEXT, " +
                            "`ledgerId` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_ledgerId` ON `transactions_new` (`ledgerId`)"
                )
                // (根据你的 v2 需求，我们为旧交易的 ledgerId 填充 0)
                db.execSQL(
                    "INSERT INTO `transactions_new` (id, amount, type, category, date, note, ledgerId) " +
                            "SELECT id, amount, type, category, date, note, 0 FROM `transactions`"
                )
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
            }
        }

        // (无变化)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            // ... (迁移代码无变化)
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建 "accounts" (账户) 表
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `accounts` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`currency` TEXT NOT NULL, " +
                            "`balance` REAL NOT NULL, " +
                            "`note` TEXT)"
                )
                // 创建索引
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_accounts_type` ON `accounts` (`type`)"
                )
            }
        }

        // (无变化)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            // ... (迁移代码无变化)
            override fun migrate(db: SupportSQLiteDatabase) {
                // 我们必须使用 "表复制" 策略来安全地添加外键

                // 步骤 1: 创建一个 *新的* 临时表 (`transactions_v4_new`)
                // (这个表结构必须与 Transaction.kt v4 的定义完全一致)
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `transactions_v4_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`amount` REAL NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`category` TEXT, " +
                            "`date` INTEGER NOT NULL, " +
                            "`note` TEXT, " +
                            "`ledgerId` INTEGER NOT NULL, " + // (v2 列)
                            "`accountId` INTEGER, " + // (!!! v4 新列, 可为空 !!!)
                            "FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE, " + // (v2 FK)
                            "FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON DELETE SET NULL)" // (!!! v4 新FK !!!)
                )

                // 步骤 2: 为新表创建 *所有* 索引 (v2 和 v4)
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_ledgerId` ON `transactions_v4_new` (`ledgerId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions_v4_new` (`accountId`)"
                )

                // 步骤 3: 将数据从 *v3旧表* (`transactions`) 复制到 *v4新表* (`transactions_v4_new`)
                //         并为所有旧记录的 accountId 赋值为 NULL
                db.execSQL(
                    "INSERT INTO `transactions_v4_new` (id, amount, type, category, date, note, ledgerId, accountId) " +
                            "SELECT id, amount, type, category, date, note, ledgerId, NULL FROM `transactions`"
                )

                // 步骤 4: 删除 *v3旧的* `transactions` 表
                db.execSQL("DROP TABLE `transactions`")

                // 步骤 5: 将 *v4新表* (`transactions_v4_new`) 重命名为 `transactions`
                db.execSQL("ALTER TABLE `transactions_v4_new` RENAME TO `transactions`")
            }
        }

        // 2. !!! 新增：v4 -> v5 迁移脚本 !!!
        /**
         * 数据库从版本 4 到 5 的迁移 (Migration) 脚本
         * 负责为 `accounts` 表添加 `ledgerId` 列和外键
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 同样使用 "表复制" 策略来安全地添加非空列 (NOT NULL) 和外键

                // 步骤 1: 创建新表 `accounts_v5_new`
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `accounts_v5_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`currency` TEXT NOT NULL, " +
                            "`balance` REAL NOT NULL, " +
                            "`note` TEXT, " +
                            "`ledgerId` INTEGER NOT NULL, " + // (!!! v5 新列, 非空 !!!)
                            "FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE)" // (!!! v5 新FK !!!)
                )

                // 步骤 2: 为新表创建索引 (v3 和 v5)
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_accounts_type` ON `accounts_v5_new` (`type`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_accounts_ledgerId` ON `accounts_v5_new` (`ledgerId`)"
                )

                // 步骤 3: 复制数据。
                // (!!! 关键 !!!)
                // 我们将所有 *现有的* (旧的、全局的) 账户
                // 迁移到 "默认账本" (ID 为 1L)。
                // (这是处理旧数据最安全的方式)
                db.execSQL(
                    "INSERT INTO `accounts_v5_new` (id, type, currency, balance, note, ledgerId) " +
                            "SELECT id, type, currency, balance, note, ${LedgerManager.DEFAULT_LEDGER_ID} FROM `accounts`"
                )

                // 步骤 4: 删除 v4旧表
                db.execSQL("DROP TABLE `accounts`")

                // 步骤 5: 重命名 v5新表
                db.execSQL("ALTER TABLE `accounts_v5_new` RENAME TO `accounts`")
            }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poket_money_database"
                )
                    // 3. !!! 修改：添加 MIGRATION_4_5 !!!
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}