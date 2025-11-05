package com.example.poketmoney

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.poketmoney.databinding.ActivityBackgroundSettingsBinding

/**
 * (!!! 已修改：添加了主题切换逻辑 !!!)
 */
class BackgroundSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackgroundSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBackgroundSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置返回按钮的点击事件 (无变化)
        binding.btnBack.setOnClickListener {
            finish() // 关闭当前 Activity
        }

        // !!! 1. 新增：加载当前主题并选中 RadioButton
        loadCurrentTheme()

        // !!! 2. 新增：设置 RadioButton 的点击监听
        setupThemeListener()

        // 处理窗口边距 (无变化)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * !!! 3. 新增：加载当前保存的主题
     */
    private fun loadCurrentTheme() {
        // 从 ThemeManager 获取保存的设置
        val currentTheme = ThemeManager.getTheme(this)

        // 根据保存的设置，勾选对应的 RadioButton
        when (currentTheme) {
            ThemeManager.THEME_LIGHT -> binding.radioGroupTheme.check(R.id.rbLight)
            ThemeManager.THEME_DARK -> binding.radioGroupTheme.check(R.id.rbDark)
            else -> binding.radioGroupTheme.check(R.id.rbSystem)
        }
    }

    /**
     * !!! 4. 新增：设置监听器
     */
    private fun setupThemeListener() {
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                // 当用户点击 "浅色"
                R.id.rbLight -> {
                    // 保存并应用 "浅色"
                    ThemeManager.setTheme(this, ThemeManager.THEME_LIGHT)
                }
                // 当用户点击 "深色"
                R.id.rbDark -> {
                    // 保存并应用 "深色"
                    ThemeManager.setTheme(this, ThemeManager.THEME_DARK)
                }
                // 当用户点击 "跟随系统"
                R.id.rbSystem -> {
                    // 保存并应用 "跟随系统"
                    ThemeManager.setTheme(this, ThemeManager.THEME_SYSTEM)
                }
            }
        }
    }
}