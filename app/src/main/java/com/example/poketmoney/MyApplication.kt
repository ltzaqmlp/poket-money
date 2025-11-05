package com.example.poketmoney

import android.app.Application

/**
 * (!!! 新增文件 !!!)
 *
 * 自定义的 Application 类。
 * 它的 onCreate() 是 App 启动时第一个运行的代码。
 * 我们在这里应用保存的主题，以确保 App 在显示任何 Activity 之前就设置了正确的主题。
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. 获取保存的主题设置
        val theme = ThemeManager.getTheme(this)
        // 2. 立即应用该主题
        ThemeManager.applyTheme(theme)
    }
}