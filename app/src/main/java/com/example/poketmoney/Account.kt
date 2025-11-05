package com.example.poketmoney

import androidx.room.Entity
import androidx.room.ForeignKey // !!! 1. 新增导入
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Account (账户) 实体类
 *
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
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val currency: String,
    val balance: Double,
    val note: String?,

    // !!! 4. 新增字段 !!!
    // 它不是可空的 (Long)，因为一个账户 *必须* 属于一个账本
    val ledgerId: Long
)