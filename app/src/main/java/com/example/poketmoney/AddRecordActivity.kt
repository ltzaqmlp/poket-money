package com.example.poketmoney

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
// import android.widget.Spinner // Spinner 是 binding 的一部分，不需要单独导入
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.poketmoney.databinding.ActivityAddRecordBinding
import java.text.SimpleDateFormat
import java.util.*
// 1. 导入我们创建的 CategoryManager
import com.example.poketmoney.CategoryManager

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var viewModel: AddRecordViewModel
    private var isIncome: Boolean = true // 默认为收入

    // 2. 移除旧的硬编码列表
    // private val categories = listOf("工资", "奖金", "理财", "餐饮", "交通", "购物", "娱乐", "医疗", "其他")

    // 3. 准备一个新的空列表，用于填充 Spinner
    private val currentCategoryList = mutableListOf<String>()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    // (无变化) 用于判断是 "新增" 还是 "编辑"
    private var currentTransactionId: Long = 0L
    private var currentTransaction: Transaction? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // (无变化) 特殊类别 "其他" (保持与 TransactionAdapter.kt 一致)
    // 我们将动态地把它添加到列表末尾
    private val otherCategoryString = "其他"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 ViewModel (无变化)
        viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]

        // 1. 获取传入的参数 (无变化)
        currentTransactionId = intent.getLongExtra("TRANSACTION_ID", 0L)

        // 2. 设置分类 Spinner
        // 4. 修改 Spinner 初始化：
        //    我们用 'currentCategoryList' (目前是空的) 来初始化 Adapter
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentCategoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // 3. 设置日期点击事件 (无变化)
        setupDatePicker()

        // 4. 设置按钮点击事件 (无变化)
        setupButtons()

        // 5. 观察 ViewModel (内部逻辑有修改)
        observeViewModel()

        // 6. 根据模式（新增/编辑）初始化页面
        if (currentTransactionId == 0L) {
            // --- 新增模式 ---
            isIncome = intent.getBooleanExtra("IS_INCOME", true)
            binding.tvTitle.text = if (isIncome) getString(R.string.add_income) else getString(R.string.add_expense)
            binding.btnDelete.visibility = View.GONE

            // 5. 修改：为 "新增模式" 加载动态类别
            setupCategorySpinner(isIncome)

        } else {
            // --- 编辑模式 ---
            binding.tvTitle.text = getString(R.string.edit_record)
            binding.btnDelete.visibility = View.VISIBLE
            // 加载要编辑的数据 (observeViewModel 将处理 Spinner 的加载)
            viewModel.loadTransaction(currentTransactionId)
        }
    }

    /**
     * 6. 新增辅助函数：
     * 根据 "收入" 或 "支出" 模式，从 CategoryManager 加载类别并设置 Spinner
     *
     * @param isIncomeMode true 表示加载收入类别, false 表示加载支出类别
     * @param categoryToSelect (可选) 加载后需要默认选中的类别 (用于"编辑模式")
     */
    private fun setupCategorySpinner(isIncomeMode: Boolean, categoryToSelect: String? = null) {
        // 1. 从 CategoryManager 获取动态列表
        val loadedCategories = if (isIncomeMode) {
            CategoryManager.getIncomeCategories(this)
        } else {
            CategoryManager.getExpenseCategories(this)
        }

        // 2. 更新 Spinner 的数据列表
        currentCategoryList.clear()
        currentCategoryList.addAll(loadedCategories)

        // 3. 关键：将 "其他" 添加到列表末尾
        //    (这个 "其他" 类别是固定的，不由用户管理)
        currentCategoryList.add(otherCategoryString)

        // 4. 通知 Adapter 数据已更新
        categoryAdapter.notifyDataSetChanged()

        // 5. (用于"编辑模式")
        //    如果提供了 categoryToSelect，则自动选中它
        //    ( ?: otherCategoryString 确保了 null 也会选中 "其他")
        val categoryToFind = categoryToSelect ?: otherCategoryString
        val position = currentCategoryList.indexOf(categoryToFind)

        if (position >= 0) {
            binding.spinnerCategory.setSelection(position)
        } else {
            // 安全回退：如果找不到（例如类别被删除了），也选中 "其他"
            binding.spinnerCategory.setSelection(currentCategoryList.size - 1)
        }
    }

    private fun observeViewModel() {
        // 观察要编辑的交易数据
        viewModel.transactionToEdit.observe(this) { transaction ->
            if (transaction != null) {
                currentTransaction = transaction

                // (必须在 setupCategorySpinner 之前更新 isIncome)
                isIncome = transaction.type == "income"

                // 填充 UI
                binding.etAmount.setText(transaction.amount.toString())
                binding.etNote.setText(transaction.note ?: "")
                binding.etDate.setText(dateFormat.format(Date(transaction.date)))

                // 7. 修改：为 "编辑模式" 加载动态类别
                //    (transaction.category 可能为 null，null 时我们让它选中 "其他")
                setupCategorySpinner(isIncome, transaction.category)
            }
        }

        // 观察是否应关闭页面 (无变化)
        viewModel.shouldCloseActivity.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
                viewModel.doneClosingActivity() // 重置信号
            }
        }
    }

    private fun setupDatePicker() {
        // (无变化)
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
        // (无变化)
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
        // (无变化)
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

        // 8. 修改：确保保存逻辑与 "其他" 字符串匹配
        //    (如果用户选择了 "其他"，我们存入 null)
        val categoryValue = if (category == otherCategoryString) null else category
        val noteValue = if (note.isEmpty()) null else note

        // (无变化)
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