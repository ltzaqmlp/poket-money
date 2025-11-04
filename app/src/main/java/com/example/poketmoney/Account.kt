package com.example.poketmoney

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Account (账户) 实体类
 *
 * 这将代表数据库中的 "accounts" (账户) 表。
 *
 * @param id 账户的唯一ID，自动增长。
 * @param type 账户类别 (例如: "现金", "储蓄卡", "信用卡")。
 * @param currency 币种 (例如: "CNY (人民币)")。
 * @param balance 初始余额。
 * @param note 备注 (可选)。
 */
@Entity(
    tableName = "accounts",
    // 我们为 "type" (类别) 列添加一个索引，
    // 因为我们未来可能会按类别筛选账户
    indices = [Index(value = ["type"])]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val currency: String,
    val balance: Double,
    val note: String?
)