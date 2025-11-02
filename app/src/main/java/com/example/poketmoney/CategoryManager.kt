package com.example.poketmoney

import android.content.Context

/**
 * CategoryManager 是一个单例对象 (object)，
 * 负责在本地持久化存储 "收入类别" 和 "支出类别"。
 *
 * 它使用 SharedPreferences，这是一个轻量级的键值对存储，非常适合存设置类数据。
 *
 * 你的补充需求（如点击修改、滑动删除）的最终数据都会通过这个 Manager 来保存。
 */
object CategoryManager {

    // SharedPreferences 文件的名称
    private const val PREFS_NAME = "poket_money_category_prefs"

    // 存储 "收入类别" 列表的 Key
    private const val KEY_INCOME_CATEGORIES = "income_categories"

    // 存储 "支出类别" 列表的 Key (为未来功能做准备)
    private const val KEY_EXPENSE_CATEGORIES = "expense_categories"

    // 默认的收入类别 (基于 AddRecordActivity 的硬编码)
    // "其他" 是一个特殊的回退选项，我们不在这里管理它
    private val DEFAULT_INCOME = setOf("工资", "奖金", "理财")

    // 默认的支出类别 (基于 AddRecordActivity 的硬编码)
    private val DEFAULT_EXPENSE = setOf("餐饮", "交通", "购物", "娱乐", "医疗")

    /**
     * 获取 "收入类别" 列表。
     * * @param context Context 对象，用于访问 SharedPreferences。
     * @return 返回一个可变的字符串列表 (MutableList<String>)。
     * 如果用户是第一次打开（SharedPreferences 中没有数据），
     * 则返回并保存 [DEFAULT_INCOME] 列表。
     */
    fun getIncomeCategories(context: Context): MutableList<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 尝试从 SharedPreferences 中读取 "KEY_INCOME_CATEGORIES"
        // SharedPreferences 只能存储 Set<String>，不能直接存 List<String>
        val savedSet = prefs.getStringSet(KEY_INCOME_CATEGORIES, null)

        if (savedSet == null) {
            // --- 第一次运行或数据丢失 ---
            // 1. 将默认的 Set 保存到 SharedPreferences
            prefs.edit().putStringSet(KEY_INCOME_CATEGORIES, DEFAULT_INCOME).apply()
            // 2. 将默认的 Set 转换为可变列表并排序后返回
            return DEFAULT_INCOME.toMutableList().sorted().toMutableList()
        } else {
            // --- 已有数据 ---
            // 将保存的 Set 转换为可变列表并排序后返回
            // 排序是为了保证每次显示的顺序一致
            return savedSet.toMutableList().sorted().toMutableList()
        }
    }

    /**
     * 保存 "收入类别" 列表。
     * * @param context Context 对象。
     * @param categories 要保存的类别列表 (List<String>)。
     */
    fun saveIncomeCategories(context: Context, categories: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 将 List 转换为 Set (因为 SharedPreferences 只能存 Set)
        val categorySet = categories.toSet()

        prefs.edit().putStringSet(KEY_INCOME_CATEGORIES, categorySet).apply()
    }

    /**
     * 获取 "支出类别" 列表。
     * (逻辑同 getIncomeCategories，为未来做准备)
     */
    fun getExpenseCategories(context: Context): MutableList<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSet = prefs.getStringSet(KEY_EXPENSE_CATEGORIES, null)

        if (savedSet == null) {
            prefs.edit().putStringSet(KEY_EXPENSE_CATEGORIES, DEFAULT_EXPENSE).apply()
            return DEFAULT_EXPENSE.toMutableList().sorted().toMutableList()
        } else {
            return savedSet.toMutableList().sorted().toMutableList()
        }
    }

    /**
     * 保存 "支出类别" 列表。
     * (逻辑同 saveIncomeCategories，为未来做准备)
     */
    fun saveExpenseCategories(context: Context, categories: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val categorySet = categories.toSet()
        prefs.edit().putStringSet(KEY_EXPENSE_CATEGORIES, categorySet).apply()
    }
}