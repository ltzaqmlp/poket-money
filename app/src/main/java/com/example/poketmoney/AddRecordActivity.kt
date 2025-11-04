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
// 2. 导入 LedgerManager
import com.example.poketmoney.LedgerManager

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var viewModel: AddRecordViewModel
    private var isIncome: Boolean = true // 默认为收入

    // (无变化)
    private val currentCategoryList = mutableListOf<String>()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    // (无变化)
    private var currentTransactionId: Long = 0L
    private var currentTransaction: Transaction? = null // (在 "编辑模式" 下，这将持有原始交易)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // (无变化)
    private val otherCategoryString = "其他"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // (无变化)
        viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]
        currentTransactionId = intent.getLongExtra("TRANSACTION_ID", 0L)

        // (无变化) 设置 Spinner
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentCategoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // (无变化)
        setupDatePicker()
        setupButtons()
        observeViewModel()

        // (无变化)
        if (currentTransactionId == 0L) {
            // --- 新增模式 ---
            isIncome = intent.getBooleanExtra("IS_INCOME", true)
            binding.tvTitle.text = if (isIncome) getString(R.string.add_income) else getString(R.string.add_expense)
            binding.btnDelete.visibility = View.GONE
            setupCategorySpinner(isIncome)

        } else {
            // --- 编辑模式 ---
            binding.tvTitle.text = getString(R.string.edit_record)
            binding.btnDelete.visibility = View.VISIBLE
            viewModel.loadTransaction(currentTransactionId)
        }
    }

    // (无变化)
    private fun setupCategorySpinner(isIncomeMode: Boolean, categoryToSelect: String? = null) {
        val loadedCategories = if (isIncomeMode) {
            CategoryManager.getIncomeCategories(this)
        } else {
            CategoryManager.getExpenseCategories(this)
        }
        currentCategoryList.clear()
        currentCategoryList.addAll(loadedCategories)
        currentCategoryList.add(otherCategoryString)
        categoryAdapter.notifyDataSetChanged()

        val categoryToFind = categoryToSelect ?: otherCategoryString
        val position = currentCategoryList.indexOf(categoryToFind)

        if (position >= 0) {
            binding.spinnerCategory.setSelection(position)
        } else {
            binding.spinnerCategory.setSelection(currentCategoryList.size - 1)
        }
    }

    // (无变化)
    private fun observeViewModel() {
        viewModel.transactionToEdit.observe(this) { transaction ->
            if (transaction != null) {
                // (重要) 保存原始交易记录，以便在保存时获取 ledgerId
                currentTransaction = transaction

                isIncome = transaction.type == "income"
                binding.etAmount.setText(transaction.amount.toString())
                binding.etNote.setText(transaction.note ?: "")
                binding.etDate.setText(dateFormat.format(Date(transaction.date)))
                setupCategorySpinner(isIncome, transaction.category)
            }
        }

        viewModel.shouldCloseActivity.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
                viewModel.doneClosingActivity()
            }
        }
    }

    // (无变化)
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
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

    // (无变化)
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
            currentTransaction?.let {
                viewModel.deleteTransaction(it)
            }
        }
    }

    /**
     * !!! 关键修改：saveRecord() !!!
     */
    private fun saveRecord() {
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val dateStr = binding.etDate.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        // (无变化) 验证
        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            return
        }
        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            return
        }
        val dateMillis = try {
            dateFormat.parse(dateStr)?.time ?: return
        } catch (e: Exception) {
            return
        }

        // (无变化)
        val type = if (isIncome) "income" else "expense"
        val categoryValue = if (category == otherCategoryString) null else category
        val noteValue = if (note.isEmpty()) null else note

        // (修改)
        if (currentTransactionId == 0L) {
            // --- 新增模式 ---

            val activeLedgerId = LedgerManager.getActiveLedgerId(this)

            val transaction = Transaction(
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue,
                ledgerId = activeLedgerId // 存入账本ID
            )
            viewModel.insertTransaction(transaction)

        } else {
            // --- 编辑模式 ---

            // 3.3. !!! BUG 修复：从 'val' 修改为 'var' !!!
            var originalLedgerId = currentTransaction?.ledgerId
            if (originalLedgerId == null) {
                // 安全检查：如果原始 ID 为空，使用当前激活的 ID
                originalLedgerId = LedgerManager.getActiveLedgerId(this)
            }

            val updatedTransaction = Transaction(
                id = currentTransactionId, // 保持 ID 不变
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue,
                ledgerId = originalLedgerId // 存入原始账本ID
            )
            viewModel.updateTransaction(updatedTransaction)
        }
    }
}