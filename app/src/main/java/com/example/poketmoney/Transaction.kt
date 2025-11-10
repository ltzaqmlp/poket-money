package com.example.poketmoney

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction (交易) 实体类
 *
<<<<<<< HEAD
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
=======
 * !!! 这是数据库升级的核心 !!!
 * 我们正在修改这个表结构，以添加 "accountId" 关联。
 *
 * @param id 交易的唯一ID。
 * @param amount 金额。
 * @param type "income" (收入) 或 "expense" (支出)。
 * @param category 类别 (例如 "工资", "餐饮")。
 * @param date 时间戳 (毫秒)。
 * @param note 备注 (可选)。
 * @param ledgerId (!!! 已有字段 !!!)
 * 它告诉我们这笔交易属于哪个账本。
 * @param accountId (!!! 新增字段 !!!)
 * 这个字段是一个外键 (ForeignKey)，
 * 它关联到 Account (账户) 表的 'id' 列。
 * 它告诉我们这笔交易使用了哪个账户。
 */
@Entity(
    tableName = "transactions",
    // 1. (!!! 已修改：在 ForeignKeys 数组中新增 Account 关联 !!!)
>>>>>>> temp-branch
    foreignKeys = [
        // (无变化) 账本外键
        ForeignKey(
<<<<<<< HEAD
            entity = Ledger::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE // (删除账本，会删除所有交易)
        ),
        // (!!! 新增 !!!) 账户外键
=======
            entity = Ledger::class,       // (原有) 关联到 Ledger 实体类
            parentColumns = ["id"],     // 关联到 Ledger 表的 "id" 列
            childColumns = ["ledgerId"],  // 关联到本表 (transactions) 的 "ledgerId" 列
            onDelete = ForeignKey.CASCADE // 级联删除：如果一个账本被删除了，
            // 那么属于该账本的所有交易记录也会被自动删除。
            // (这符合我们的业务逻辑)
        ),
        // (!!! 新增外键 !!!)
>>>>>>> temp-branch
        ForeignKey(
            entity = Account::class,      // 关联到 Account 实体类
            parentColumns = ["id"],     // 关联到 Account 表的 "id" 列
            childColumns = ["accountId"], // 关联到本表 (transactions) 的 "accountId" 列
<<<<<<< HEAD
            onDelete = ForeignKey.SET_NULL // (安全选项) 删除账户，
            // 交易记录的 accountId 会被设为 null，
            // 但交易记录本身 *不会* 被删除。
        )
    ],
    // 2. !!! 修改：添加了第二个索引 !!!
    indices = [
        Index(value = ["ledgerId"]),   // (旧索引)
        Index(value = ["accountId"])  // (!!! 新增索引 !!!)
=======
            onDelete = ForeignKey.SET_NULL // (!!! 重要 !!!)
            // 级联置空：如果一个账户被删除了，
            // 相关的交易记录 *不会* 被删除，
            // 只是它们的 accountId 会被设为 NULL。
            // (这可以防止用户丢失历史交易记录)
        )
    ],
    // 2. (!!! 已修改：在 Indices 数组中新增 Account 索引 !!!)
    //    (为新列 accountId 创建索引，可以加快按账户ID查询的速度)
    indices = [
        Index(value = ["ledgerId"]),  // (原有)
        Index(value = ["accountId"]) // (!!! 新增索引 !!!)
>>>>>>> temp-branch
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

<<<<<<< HEAD
    // 3. !!! 新增的字段 !!!
    // 它必须是可为空的 (Long?)，
    // 这样 v2->v3 迁移才能成功 (旧交易没有 accountId)，
    // 并且 onDelete = SET_NULL 才能工作。
=======
    // (原有字段)
    val ledgerId: Long,

    // 3. (!!! 新增的字段 !!!)
    // 这个字段是可空的 (Long?)，
    // 因为旧的交易记录在迁移时可能没有账户。
>>>>>>> temp-branch
    val accountId: Long?
)