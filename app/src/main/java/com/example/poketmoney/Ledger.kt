package com.example.poketmoney

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ledger (账本) 实体类
 *
 * 这将代表数据库中的 "ledgers" (账本) 表。
 *
 * @param id 账本的唯一ID，自动增长。
 * @param name 账本的名称 (例如 "生活", "旅游")。
 */
@Entity(
    tableName = "ledgers",
    // 我们为 "name" 列添加一个唯一索引 (indices)，
    // 这可以防止用户创建两个同名的账本 (例如，两个 "生活")，
    // 确保数据的完整性。
    indices = [Index(value = ["name"], unique = true)]
)
data class Ledger(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)