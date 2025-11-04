package com.example.poketmoney

import android.content.Context

/**
 * LedgerManager 是一个单例对象 (object)，
 * 负责在本地持久化存储 "当前激活的账本ID"。
 *
 * 它使用 SharedPreferences，这是一个轻量级的键值对存储。
 */
object LedgerManager {

    // SharedPreferences 文件的名称
    private const val PREFS_NAME = "poket_money_ledger_prefs"

    // 存储 "当前激活账本ID" 的 Key
    private const val KEY_ACTIVE_LEDGER_ID = "active_ledger_id"

    /**
     * 默认账本ID。
     * 在数据库迁移 (MIGRATION_1_2) 中，我们插入的第一个账本 ("默认账本")
     * 的自增 ID 几乎可以肯定是 1。
     * 我们使用 1L 作为 App 首次运行的默认值。
     */
    private const val DEFAULT_LEDGER_ID = 1L

    /**
     * 保存 "当前激活的账本ID"。
     * 当用户在 LedgerManageActivity 中点击一个账本时，我们将调用此方法。
     *
     * @param context Context 对象，用于访问 SharedPreferences。
     * @param ledgerId 要设为激活状态的账本 ID。
     */
    fun setActiveLedgerId(context: Context, ledgerId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_ACTIVE_LEDGER_ID, ledgerId).apply()
    }

    /**
     * 获取 "当前激活的账本ID"。
     * MainActivity 和 AddRecordActivity 将调用此方法来确定当前账本。
     *
     * @param context Context 对象。
     * @return 返回当前激活的账本 ID。
     * 如果是首次运行（即 SharedPreferences 中没有值），
     * 则返回 [DEFAULT_LEDGER_ID] (1L)。
     */
    fun getActiveLedgerId(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_ACTIVE_LEDGER_ID, DEFAULT_LEDGER_ID)
    }
}