package com.example.poketmoney

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction (交易) 实体类
 *
 * !!! 数据库升级 (v3 -> v4) !!!
 *
 * @param ledgerId (无变化) 关联到 Ledger (账本) 表。
 * @param accountId (!!! 新增字段 !!!)
 * 这个字段是一个外键 (ForeignKey)，
 * 它关联到 Account (账户) 表的 'id' 列。
 * 它告诉我们这笔交易是从哪个账户 "支付" 或 "存入" 的。
 */
@Entity(
    tableName = "transactions",
    // 1. !!! 修改：添加了第二个外键 (ForeignKey) !!!
    foreignKeys = [
        // (无变化) 账本外键
        ForeignKey(
            entity = Ledger::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE // (删除账本，会删除所有交易)
        ),
        // (!!! 新增 !!!) 账户外键
        ForeignKey(
            entity = Account::class,      // 关联到 Account 实体类
            parentColumns = ["id"],     // 关联到 Account 表的 "id" 列
            childColumns = ["accountId"], // 关联到本表 (transactions) 的 "accountId" 列
            onDelete = ForeignKey.SET_NULL // (安全选项) 删除账户，
            // 交易记录的 accountId 会被设为 null，
            // 但交易记录本身 *不会* 被删除。
        )
    ],
    // 2. !!! 修改：添加了第二个索引 !!!
    indices = [
        Index(value = ["ledgerId"]),   // (旧索引)
        Index(value = ["accountId"])  // (!!! 新增索引 !!!)
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" 或 "expense"
    val category: String?,
    val date: Long, // 时间戳 (毫秒)
    val note: String?,
    val ledgerId: Long, // (来自 v1->v2 的迁移)

    // 3. !!! 新增的字段 !!!
    // 它必须是可为空的 (Long?)，
    // 这样 v2->v3 迁移才能成功 (旧交易没有 accountId)，
    // 并且 onDelete = SET_NULL 才能工作。
    val accountId: Long?
)