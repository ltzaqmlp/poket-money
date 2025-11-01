package com.example.poketmoney

/**
 * 这是一个数据类，用于承载 Room 数据库按天统计收入或支出的查询结果
 * Room 2.5.0+ 支持 strftime, 我们可以按 'MM-dd' 格式分组
 */
data class DailySummary(
    val dayLabel: String,  // 日期标签, e.g., "10-31"
    val totalAmount: Double // 当天的总金额
)
