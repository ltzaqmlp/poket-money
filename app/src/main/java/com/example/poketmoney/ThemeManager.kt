package com.example.poketmoney

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

/**
 * (!!! 新增文件 !!!)
 *
 * ThemeManager 是一个单例对象 (object)，
 * 负责在本地持久化存储 "当前主题" (浅色/深色/跟随系统)。
 * 它使用 SharedPreferences。
 */
object ThemeManager {

    // SharedPreferences 文件的名称
    private const val PREFS_NAME = "poket_money_theme_prefs"
    // 存储 "主题模式" 的 Key
    private const val KEY_THEME_MODE = "theme_mode"

    // 定义三种模式的常量
    const val THEME_LIGHT = "Light"
    const val THEME_DARK = "Dark"
    const val THEME_SYSTEM = "System"

    /**
     * 保存用户选择的主题模式。
     *
     * @param context Context 对象。
     * @param themeMode 要保存的模式 (使用常量 THEME_LIGHT, THEME_DARK, THEME_SYSTEM)。
     */
    fun setTheme(context: Context, themeMode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, themeMode).apply()
        // 立即应用主题
        applyTheme(themeMode)
    }

    /**
     * 获取当前保存的主题模式。
     *
     * @param context Context 对象。
     * @return 返回当前保存的模式。默认为 "跟随系统" (THEME_SYSTEM)。
     */
    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 默认使用 "跟随系统"
        return prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    /**
     * 应用主题。
     * 这是真正改变 App 外观的核心代码。
     *
     * @param themeMode 要应用的主题模式。
     */
    fun applyTheme(themeMode: String) {
        when (themeMode) {
            THEME_LIGHT -> {
                // 强制应用浅色模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_DARK -> {
                // 强制应用深色模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> { // THEME_SYSTEM
                // 跟随系统设置
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // (Android Q 及以上) 使用系统设置
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    // (Android P 及以下) 使用省电模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }
}