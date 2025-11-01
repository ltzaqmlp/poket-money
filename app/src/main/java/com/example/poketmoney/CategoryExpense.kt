package com.example.poketmoney

/**
 * 一个数据类，用于承载 Room 查询按分类统计的总支出。
 * @param category 类别名称 (可能为 null，我们会处理为 "其他")
 * @param totalAmount 该类别的总金额
 */
data class CategoryExpense(
    val category: String?,
    val totalAmount: Double
)

