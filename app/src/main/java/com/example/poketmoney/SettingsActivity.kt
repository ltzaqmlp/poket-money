package com.example.poketmoney

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.poketmoney.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 启用边缘到边缘显示
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置返回按钮的点击事件 (无变化)
        binding.btnBack.setOnClickListener {
            finish() // 关闭当前 Activity
        }

        // --- 为 "收入类别" 按钮设置点击事件 (无变化) ---
        binding.btnIncomeCategory.setOnClickListener {
            val intent = Intent(this, CategoryManageActivity::class.java)
            intent.putExtra("CATEGORY_TYPE", "income")
            startActivity(intent)
        }

        // --- 为 "支出类别" 按钮设置点击事件 (无变化) ---
        binding.btnExpenseCategory.setOnClickListener {
            val intent = Intent(this, CategoryManageActivity::class.java)
            intent.putExtra("CATEGORY_TYPE", "expense")
            startActivity(intent)
        }

        // --- 为 "账本管理" 按钮设置点击事件 (无变化) ---
        binding.btnLedgerManagement.setOnClickListener {
            val intent = Intent(this, LedgerManageActivity::class.java)
            startActivity(intent)
        }

        // --- !!! 新增代码：为 "账户管理" 按钮设置点击事件 !!! ---
        binding.btnAccountManagement.setOnClickListener {
            // 1. 创建一个 Intent 跳转到 AccountManageActivity
            val intent = Intent(this, AccountManageActivity::class.java)
            // 2. 启动 Activity
            startActivity(intent)
        }
        // --- 新增代码结束 ---


        // (为保持一致性) 处理窗口边距 (无变化)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}