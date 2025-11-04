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

        // 设置返回按钮的点击事件
        binding.btnBack.setOnClickListener {
            finish() // 关闭当前 Activity
        }

        // --- 为 "收入类别" 按钮设置点击事件 ---
        binding.btnIncomeCategory.setOnClickListener {
            // 1. 创建一个 Intent 跳转到 CategoryManageActivity
            val intent = Intent(this, CategoryManageActivity::class.java)

            // 2. 传入一个 "extra"，告诉 CategoryManageActivity 我们要管理的是 "income" (收入)
            intent.putExtra("CATEGORY_TYPE", "income")

            // 3. 启动 Activity
            startActivity(intent)
        }

        // --- 为 "支出类别" 按钮设置点击事件 ---
        binding.btnExpenseCategory.setOnClickListener {
            val intent = Intent(this, CategoryManageActivity::class.java)
            // 告诉 CategoryManageActivity 我们要管理的是 "expense" (支出)
            intent.putExtra("CATEGORY_TYPE", "expense")
            startActivity(intent)
        }

        // --- !!! 新增代码：为 "账本管理" 按钮设置点击事件 !!! ---
        binding.btnLedgerManagement.setOnClickListener {
            // 1. 创建一个 Intent 跳转到 LedgerManageActivity
            val intent = Intent(this, LedgerManageActivity::class.java)
            // 2. 启动 Activity
            startActivity(intent)
        }
        // --- 新增代码结束 ---


        // (为保持一致性) 处理窗口边距，确保布局不会与系统栏重叠
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}