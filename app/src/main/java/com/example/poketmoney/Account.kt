package com.example.poketmoney

import androidx.room.Entity
<<<<<<< HEAD
import androidx.room.ForeignKey // !!! 1. 新增导入
=======
import androidx.room.ForeignKey
>>>>>>> temp-branch
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Account (账户) 实体类
 *
<<<<<<< HEAD
 * (!!! 数据库升级 (v4 -> v5) !!!)
 * (!!! 已添加 ledgerId、外键和索引 !!!)
 *
 * @param ledgerId (!!! 新增字段 !!!) 关联到 Ledger (账本) 表。
 */
@Entity(
    tableName = "accounts",
    // !!! 2. 新增：外键，关联到 Ledger 表
    foreignKeys = [
        ForeignKey(
            entity = Ledger::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE // (删除账本，会删除所有关联账户)
        )
    ],
    // !!! 3. 修改：添加了第二个索引
    indices = [
        Index(value = ["type"]),       // (旧索引)
        Index(value = ["ledgerId"])  // (!!! 新增索引 !!!)
=======
 * (!!! 已更新：添加 ledgerId 来实现 "账本-账户" 关联 !!!)
 *
 * @param id 账户的唯一ID，自动增长。
 * @param type 账户类别 (例如: "现金", "储蓄卡", "信用卡")。
 * @param currency 币种 (例如: "CNY (人民币)")。
 * @param balance 初始余额。
 * @param note 备注 (可选)。
 * @param ledgerId (!!! 新增字段 !!!)
 * 这个字段是一个外键 (ForeignKey)，
 * 它关联到 Ledger (账本) 表的 'id' 列。
 * 它告诉我们这个账户 *属于* 哪个账本。
 */
@Entity(
    tableName = "accounts",
    // 1. 更新索引：保留 "type" 索引，并为 "ledgerId" 添加新索引
    //    (加快按账本ID查询账户的速度)
    indices = [Index(value = ["type"]), Index(value = ["ledgerId"])],

    // 2. 定义外键约束
    foreignKeys = [
        ForeignKey(
            entity = Ledger::class,       // 关联到 Ledger 实体类
            parentColumns = ["id"],     // 关联到 Ledger 表的 "id" 列
            childColumns = ["ledgerId"],  // 关联到本表 (accounts) 的 "ledgerId" 列
            onDelete = ForeignKey.CASCADE // 级联删除：如果一个账本被删除了，
            // 那么属于该账本的所有账户也会被自动删除。
            // (这符合我们的业务逻辑)
        )
>>>>>>> temp-branch
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val currency: String,
    val balance: Double,
    val note: String?,

<<<<<<< HEAD
    // !!! 4. 新增字段 !!!
    // 它不是可空的 (Long)，因为一个账户 *必须* 属于一个账本
=======
    // 3. (!!! 新增字段 !!!)
    // 这是这个账户所属的账本 ID
>>>>>>> temp-branch
    val ledgerId: Long
)