package com.example.poketmoney

data class StatItem(
    val title: String,      // 如 "今天", "本周"
    val subtitle: String,   // 如 "2025年10月29日", "10月, 27 -- 11月, 02"
    val income: Double,     // 收入金额
    val expense: Double,    // 支出金额
    val balance: Double,    // 结余 (收入 - 支出)

    // 步骤 2 新增：用于传递给详情页的日期范围
    val startDate: Long,
    val endDate: Long
)
