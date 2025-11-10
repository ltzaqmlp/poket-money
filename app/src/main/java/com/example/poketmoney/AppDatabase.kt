package com.example.poketmoney

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * !!! 数据库升级 (已更新 v3 -> v5) !!!
 */
@Database(
    entities = [Transaction::class, Ledger::class, Account::class],
    version = 5, // (!!! 1. 升级到 v5 !!!)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * (无变化) 数据库从版本 1 到 2 的迁移
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- 账本 (Ledger) 表 (无变化) ---
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ledgers` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_ledgers_name` ON `ledgers` (`name`)"
                )
                db.execSQL(
                    "INSERT INTO `ledgers` (name) VALUES ('默认账本')" // ID 自动为 1
                )

                // --- 交易 (Transaction) 表 (无变化) ---
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
                db.execSQL(
                    "INSERT INTO `transactions_new` (id, amount, type, category, date, note, ledgerId) " +
                            "SELECT id, amount, type, category, date, note, 1 FROM `transactions`"
                )
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
            }
        }

        /**
         * (无变化) 数据库从版本 2 到 3 的迁移
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 步骤 1: 创建 "accounts" (账户) 表
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `accounts` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`currency` TEXT NOT NULL, " +
                            "`balance` REAL NOT NULL, " +
                            "`note` TEXT)"
                )
                // 步骤 2: 为 "type" 列创建索引
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_accounts_type` ON `accounts` (`type`)"
                )
            }
        }

        /**
         * (!!! 2. 新增：v3 -> v4 迁移 !!!)
         * 目标：将 `ledgerId` (外键) 添加到 `accounts` 表
         * 策略：重建表 (这是添加外键的最安全方式)
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 步骤 1: 创建新表 (必须与 Account.kt v4 结构一致)
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `accounts_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`currency` TEXT NOT NULL, " +
                            "`balance` REAL NOT NULL, " +
                            "`note` TEXT, " +
                            "`ledgerId` INTEGER NOT NULL, " + // 新增列
                            "FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE)" // 新增外键
                )

                // 步骤 2: 将数据从旧表 `accounts` 复制到 `accounts_new`
                //         (重要) 为所有旧账户的 `ledgerId` 赋值为 1 (指向 "默认账本")
                db.execSQL(
                    "INSERT INTO `accounts_new` (id, type, currency, balance, note, ledgerId) " +
                            "SELECT id, type, currency, balance, note, 1 FROM `accounts`"
                )

                // 步骤 3: 删除旧的 `accounts` 表
                db.execSQL("DROP TABLE `accounts`")

                // 步骤 4: 将 `accounts_new` 重命名为 `accounts`
                db.execSQL("ALTER TABLE `accounts_new` RENAME TO `accounts`")

                // 步骤 5: 重新创建索引 (旧索引 `index_accounts_type` 和新索引 `index_accounts_ledgerId`)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_type` ON `accounts` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_ledgerId` ON `accounts` (`ledgerId`)")
            }
        }

        /**
         * (!!! 3. 新增：v4 -> v5 迁移 !!!)
         * 目标：将 `accountId` (可空, 外键) 添加到 `transactions` 表
         * 策略：重建表
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 步骤 1: 创建新表 (必须与 Transaction.kt v5 结构一致)
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `transactions_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`amount` REAL NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`category` TEXT, " +
                            "`date` INTEGER NOT NULL, " +
                            "`note` TEXT, " +
                            "`ledgerId` INTEGER NOT NULL, " +
                            "`accountId` INTEGER, " + // 新增 (可空) 列
                            "FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE, " + // 原有外键
                            "FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON DELETE SET NULL)" // 新增外键
                )

                // 步骤 2: 将数据从旧表 `transactions` 复制到 `transactions_new`
                //         (重要) 为所有旧交易的 `accountId` 赋值为 NULL
                db.execSQL(
                    "INSERT INTO `transactions_new` (id, amount, type, category, date, note, ledgerId, accountId) " +
                            "SELECT id, amount, type, category, date, note, ledgerId, NULL FROM `transactions`"
                )

                // 步骤 3: 删除旧的 `transactions` 表
                db.execSQL("DROP TABLE `transactions`")

                // 步骤 4: 将 `transactions_new` 重命名为 `transactions`
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

                // 步骤 5: 重新创建索引
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_ledgerId` ON `transactions` (`ledgerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions` (`accountId`)")
            }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poket_money_database"
                )
                    // 5. (!!! 4. 将 *全部* 迁移脚本添加到构建器中 !!!)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}