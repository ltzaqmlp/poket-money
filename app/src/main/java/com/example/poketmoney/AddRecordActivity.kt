package com.example.poketmoney

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.poketmoney.databinding.ActivityAddRecordBinding
import java.text.SimpleDateFormat
import java.util.*

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var viewModel: AddRecordViewModel
    private var isIncome: Boolean = true // 默认为收入

    // 示例分类列表
    // 步骤 3 修改：将 "其他" 移到末尾，以便逻辑统一
    private val categories = listOf("工资", "奖金", "理财", "餐饮", "交通", "购物", "娱乐", "医疗", "其他")
    private lateinit var categoryAdapter: ArrayAdapter<String>

    // 步骤 3 新增：用于判断是 "新增" 还是 "编辑"
    private var currentTransactionId: Long = 0L
    private var currentTransaction: Transaction? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 步骤 3 修改：初始化 ViewModel
        viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]

        // 1. 获取传入的参数
        // 尝试获取 TRANSACTION_ID，如果为 0，则为 "新增模式"
        currentTransactionId = intent.getLongExtra("TRANSACTION_ID", 0L)

        // 2. 设置分类 Spinner
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // 3. 设置日期点击事件 (无变化)
        setupDatePicker()

        // 4. 设置按钮点击事件
        setupButtons()

        // 5. 观察 ViewModel
        observeViewModel()

        // 6. 根据模式（新增/编辑）初始化页面
        if (currentTransactionId == 0L) {
            // --- 新增模式 ---
            isIncome = intent.getBooleanExtra("IS_INCOME", true)
            binding.tvTitle.text = if (isIncome) getString(R.string.add_income) else getString(R.string.add_expense)
            binding.btnDelete.visibility = View.GONE
        } else {
            // --- 编辑模式 ---
            binding.tvTitle.text = getString(R.string.edit_record)
            binding.btnDelete.visibility = View.VISIBLE
            // 加载要编辑的数据
            viewModel.loadTransaction(currentTransactionId)
        }
    }

    private fun observeViewModel() {
        // 观察要编辑的交易数据
        viewModel.transactionToEdit.observe(this) { transaction ->
            if (transaction != null) {
                currentTransaction = transaction
                // 填充 UI
                binding.etAmount.setText(transaction.amount.toString())
                binding.etNote.setText(transaction.note ?: "")
                binding.etDate.setText(dateFormat.format(Date(transaction.date)))

                // 设置 Spinner 选中项
                val categoryPosition = categoryAdapter.getPosition(transaction.category ?: "其他")
                binding.spinnerCategory.setSelection(categoryPosition)

                // 更新 isIncome 状态
                isIncome = transaction.type == "income"
            }
        }

        // 观察是否应关闭页面
        viewModel.shouldCloseActivity.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
                viewModel.doneClosingActivity() // 重置信号
            }
        }
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            // 如果是编辑模式，使用记录的日期
            currentTransaction?.let {
                calendar.timeInMillis = it.date
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    binding.etDate.setText(dateFormat.format(selectedDate.time))
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            saveRecord()
        }
        binding.btnDelete.setOnClickListener {
            // 删除当前记录
            currentTransaction?.let {
                viewModel.deleteTransaction(it)
            }
        }
    }

    private fun saveRecord() {
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val dateStr = binding.etDate.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            // 可以在此添加 Toast 提示
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            // 处理数字格式异常
            return
        }

        val dateMillis = try {
            dateFormat.parse(dateStr)?.time ?: return
        } catch (e: Exception) {
            // 处理日期解析异常
            return
        }

        val type = if (isIncome) "income" else "expense"
        val categoryValue = if (category == "其他") null else category
        val noteValue = if (note.isEmpty()) null else note

        // 步骤 3 修改：根据模式调用 ViewModel
        if (currentTransactionId == 0L) {
            // --- 新增模式 ---
            val transaction = Transaction(
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue
            )
            viewModel.insertTransaction(transaction)
        } else {
            // --- 编辑模式 ---
            val updatedTransaction = Transaction(
                id = currentTransactionId, // 保持 ID 不变
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue
            )
            viewModel.updateTransaction(updatedTransaction)
        }
    }
}
