package com.example.poketmoney

data class StatItem(
    // !!! 1. 新增字段 !!!
    val iconText: String,   // 如 "1", "7", "31", "365", "∑"

    val title: String,      // 如 "今天", "本周"
    val subtitle: String,   // 如 "2025年10月29日", "10月, 27 -- 11月, 02"
    val income: Double,     // 收入金额
    val expense: Double,    // 支出金额
    val balance: Double,    // 结余 (收入 - 支出)

    // (无变化)
    val startDate: Long,
    val endDate: Long
)