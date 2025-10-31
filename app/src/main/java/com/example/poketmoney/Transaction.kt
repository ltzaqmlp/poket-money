package com.example.poketmoney

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" 或 "expense"
    val category: String?,
    val date: Long, // 时间戳 (毫秒)
    val note: String?
)