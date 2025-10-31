package com.example.poketmoney

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.poketmoney.databinding.ActivityAddRecordBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var db: AppDatabase
    private var isIncome: Boolean = true // 默认为收入

    // 示例分类列表
    private val categories = listOf("工资", "奖金", "理财", "其他", "餐饮", "交通", "购物", "娱乐", "医疗", "其他")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // 获取传入的参数，判断是新增收入还是支出
        isIncome = intent.getBooleanExtra("IS_INCOME", true)

        // 设置标题
        binding.tvTitle.text = if (isIncome) getString(R.string.add_income) else getString(R.string.add_expense)

        // 设置分类 Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // 设置日期点击事件
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.etDate.setText(dateFormat.format(selectedDate.time))
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // 设置返回按钮点击事件
        binding.btnBack.setOnClickListener {
            finish() // 关闭当前 Activity
        }

        // 设置取消按钮点击事件
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // 设置保存按钮点击事件
        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun saveRecord() {
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val dateStr = binding.etDate.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            // 可以添加 Toast 提示
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            // 处理数字格式异常
            return
        }

        // 将日期字符串转换为毫秒时间戳
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateMillis = try {
            dateFormat.parse(dateStr)?.time ?: return
        } catch (e: Exception) {
            // 处理日期解析异常
            return
        }

        val type = if (isIncome) "income" else "expense"

        // 创建 Transaction 对象
        val transaction = Transaction(
            amount = amount,
            type = type,
            category = if (category == "其他") null else category, // 如果选择“其他”，则存为 null
            date = dateMillis,
            note = if (note.isEmpty()) null else note // 如果备注为空，则存为 null
        )

        // 启动协程插入数据库
        lifecycleScope.launch {
            db.transactionDao().insert(transaction)
            finish() // 保存成功后关闭 Activity
        }
    }
}