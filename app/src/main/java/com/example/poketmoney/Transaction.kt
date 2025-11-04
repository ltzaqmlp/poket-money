package com.example.poketmoney

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction (交易) 实体类
 *
 * !!! 这是数据库升级的核心 !!!
 * 我们正在修改这个表结构。
 *
 * @param id 交易的唯一ID。
 * @param amount 金额。
 * @param type "income" (收入) 或 "expense" (支出)。
 * @param category 类别 (例如 "工资", "餐饮")。
 * @param date 时间戳 (毫秒)。
 * @param note 备注 (可选)。
 * @param ledgerId (!!! 新增字段 !!!)
 * 这个字段是一个外键 (ForeignKey)，
 * 它关联到 Ledger (账本) 表的 'id' 列。
 * 它告诉我们这笔交易属于哪个账本。
 */
@Entity(
    tableName = "transactions",
    // 1. 定义外键约束
    foreignKeys = [
        ForeignKey(
            entity = Ledger::class,       // 关联到 Ledger 实体类
            parentColumns = ["id"],     // 关联到 Ledger 表的 "id" 列
            childColumns = ["ledgerId"],  // 关联到本表 (transactions) 的 "ledgerId" 列
            onDelete = ForeignKey.CASCADE // 级联删除：如果一个账本被删除了，
            // 那么属于该账本的所有交易记录也会被自动删除。
            // (这符合我们的业务逻辑)
        )
    ],
    // 2. 为新列 ledgerId 创建索引，这可以加快按账本ID查询的速度
    indices = [Index(value = ["ledgerId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" 或 "expense"
    val category: String?,
    val date: Long, // 时间戳 (毫秒)
    val note: String?,

    // !!! 新增的字段 !!!
    // 我们必须给它一个默认值 (e.g., 0L)，
    // 但在实际的数据库迁移 (Migration) 中我们会处理旧数据的赋值。
    val ledgerId: Long
)