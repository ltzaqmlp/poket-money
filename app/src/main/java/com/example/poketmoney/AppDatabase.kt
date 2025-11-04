package com.example.poketmoney

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * !!! 数据库升级 (已修复) !!!
 */
@Database(
    entities = [Transaction::class, Ledger::class],
    version = 2, // 版本升级为 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun ledgerDao(): LedgerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 4. (!!! 已修复 !!!) 数据库从版本 1 到 2 的迁移 (Migration) 脚本
         *
         * 我们现在执行一个安全的 "表复制" 迁移
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- 账本 (Ledger) 表 (无变化) ---

                // 步骤 1: 创建新的 "ledgers" (账本) 表
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ledgers` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL)"
                )
                // 步骤 2: 为 "name" 列添加唯一索引
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_ledgers_name` ON `ledgers` (`name`)"
                )
                // 步骤 3: 插入 "默认账本"，ID 自动为 1
                db.execSQL(
                    "INSERT INTO `ledgers` (name) VALUES ('默认账本')"
                )

                // --- 交易 (Transaction) 表 (!!! 这是修复的核心 !!!) ---

                // 步骤 4: 创建一个 *新的* 临时表 (`transactions_new`)
                // (这个表结构必须与 Transaction.kt v2 的定义 *完全* 一致，包含外键)
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `transactions_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`amount` REAL NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`category` TEXT, " +
                            "`date` INTEGER NOT NULL, " +
                            "`note` TEXT, " +
                            "`ledgerId` INTEGER NOT NULL, " + // 新列
                            "FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE)" // 新的外键
                )

                // 步骤 5: (可选，但推荐) 为新表的 ledgerId 列创建索引
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_ledgerId` ON `transactions_new` (`ledgerId`)"
                )

                // 步骤 6: 将数据从 *旧表* (`transactions`) 复制到 *新表* (`transactions_new`)
                //         并为所有旧记录的 ledgerId 赋值为 1 (指向 "默认账本")
                db.execSQL(
                    "INSERT INTO `transactions_new` (id, amount, type, category, date, note, ledgerId) " +
                            "SELECT id, amount, type, category, date, note, 1 FROM `transactions`"
                )

                // 步骤 7: 删除 *旧的* `transactions` 表
                db.execSQL("DROP TABLE `transactions`")

                // 步骤 8: 将 *新表* (`transactions_new`) 重命名为 `transactions`
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poket_money_database"
                )
                    // 5. 将 *已修复* 的迁移脚本添加到数据库构建器中
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}