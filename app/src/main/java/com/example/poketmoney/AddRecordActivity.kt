package com.example.poketmoney

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.poketmoney.databinding.ActivityAddRecordBinding
// 导入 (无变化)
import com.example.poketmoney.databinding.DialogAddEditAccountBinding
import com.example.poketmoney.databinding.DialogSelectAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import com.example.poketmoney.CategoryManager
import com.example.poketmoney.LedgerManager

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var viewModel: AddRecordViewModel
    private var isIncome: Boolean = true

    // (无变化) 分类 Spinner
    private val currentCategoryList = mutableListOf<String>()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    // (无变化)
    private var selectedAccount: Account? = null

    // (无变化)
    private val currencies = listOf(
        "CNY (人民币)", "HKD (港币)", "MOP (澳币)", "USD (美元)",
        "EUR (欧元)", "KRW (韩元)", "JPY (日元)", "GBP (英镑)", "TWD (台币)"
    )
    private lateinit var currencyAdapter: ArrayAdapter<String>

    // !!! 1. 新增：获取当前激活的账本 ID
    /**
     * 当前激活的账本 ID。
     * 用于在 "新增账户" 弹窗中创建账户时使用。
     */
    private var activeLedgerId: Long = 1L

    // (无变化)
    private var currentTransactionId: Long = 0L
    private var currentTransaction: Transaction? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val otherCategoryString = "其他"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]
        currentTransactionId = intent.getLongExtra("TRANSACTION_ID", 0L)

        // !!! 2. 新增：在 OnCreate 时获取 activeLedgerId
        activeLedgerId = LedgerManager.getActiveLedgerId(this)

        // (无变化)
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentCategoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // (无变化)
        currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // (无变化)
        binding.etAccount.setOnClickListener {
            showSelectAccountDialog()
        }

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
            // (无变化)
            viewModel.allAccounts.observe(this) { accounts ->
                if (selectedAccount == null && accounts.isNotEmpty()) {
                    setSelectedAccount(accounts[0])
                }
            }

        } else {
            // --- 编辑模式 ---
            binding.tvTitle.text = getString(R.string.edit_record)
            binding.btnDelete.visibility = View.VISIBLE
            viewModel.loadTransaction(currentTransactionId)
        }
    }

    /**
     * (无变化) 设置分类 Spinner
     */
    private fun setupCategorySpinner(isIncomeMode: Boolean, categoryToSelect: String? = null) {
        // ... (无变化)
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
    private fun setSelectedAccount(account: Account?) {
        // ... (无变化)
        this.selectedAccount = account
        if (account != null) {
            binding.etAccount.setText("${account.type} (${account.currency})")
        } else {
            binding.etAccount.setText("")
        }
    }

    /**
     * (无变化) observeViewModel()
     * (ViewModel 已经在内部按 ledgerId 过滤了 allAccounts)
     */
    private fun observeViewModel() {
        // ... (无变化)
        viewModel.transactionToEdit.observe(this) { transaction ->
            if (transaction != null) {
                currentTransaction = transaction
                isIncome = transaction.type == "income"
                binding.etAmount.setText(transaction.amount.toString())
                binding.etNote.setText(transaction.note ?: "")
                binding.etDate.setText(dateFormat.format(Date(transaction.date)))

                setupCategorySpinner(isIncome, transaction.category)

                viewModel.allAccounts.observe(this) { accounts ->
                    if (selectedAccount == null) {
                        val accountToSelect = accounts.find { it.id == transaction.accountId }
                        setSelectedAccount(accountToSelect)
                    }
                }
            }
        }

        viewModel.shouldCloseActivity.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
                viewModel.doneClosingActivity()
            }
        }

        viewModel.newlyCreatedAccount.observe(this) { newAccount ->
            if (newAccount != null) {
                setSelectedAccount(newAccount)
                viewModel.doneSelectingNewAccount()
            }
        }
    }

    // (无变化)
    private fun setupDatePicker() {
        // ... (无变化)
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
        // ... (无变化)
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

    // (无变化)
    private fun showSelectAccountDialog() {
        // ... (无变化)
        // (此函数依赖 viewModel.allAccounts，而 VM 已经过滤了)
        val dialogBinding = DialogSelectAccountBinding.inflate(LayoutInflater.from(this))
        val accountSelectAdapter = AccountSelectAdapter()
        dialogBinding.recyclerViewAccounts.adapter = accountSelectAdapter

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .show()

        accountSelectAdapter.onItemClick = { account ->
            setSelectedAccount(account)
            dialog.dismiss()
        }

        dialogBinding.btnAddAccount.setOnClickListener {
            dialog.dismiss()
            showAddAccountDialog() // (!!! 此函数已被修改 !!!)
        }

        val currentAccounts = viewModel.allAccounts.value ?: emptyList()
        accountSelectAdapter.submitList(currentAccounts)
    }

    // !!! 3. 修改：显示 "新增账户" 对话框
    /**
     * 当用户在 "选择账户" 弹窗中点击 "新增" 时调用。
     */
    private fun showAddAccountDialog() {
        // (无变化)
        val dialogBinding = DialogAddEditAccountBinding.inflate(LayoutInflater.from(this))
        dialogBinding.spinnerCurrency.adapter = currencyAdapter
        dialogBinding.tvDialogTitle.text = getString(R.string.add_account_title)
        dialogBinding.etBalance.setText("0.0")

        // (无变化)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()

        // (!!! 修改：设置 "保存" 按钮 !!!)
        dialogBinding.btnDialogSave.setOnClickListener {
            // (无变化)
            val accountType = dialogBinding.etAccountType.text.toString().trim()
            val currency = dialogBinding.spinnerCurrency.selectedItem.toString()
            val balanceStr = dialogBinding.etBalance.text.toString().trim()
            val note = dialogBinding.etNote.text.toString().trim()
            val noteValue = if (note.isEmpty()) null else note

            // (无变化) 验证
            if (accountType.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(this, R.string.error_account_validation_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val balance = try {
                balanceStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.error_account_validation_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (!!! 关键修改 !!!)
            // 调用 ViewModel 的 insertAccount
            val newAccount = Account(
                type = accountType,
                currency = currency,
                balance = balance,
                note = noteValue,
                ledgerId = activeLedgerId // (!!! 存入当前账本 ID !!!)
            )
            viewModel.insertAccount(newAccount)

            dialog.dismiss()
        }

        // (无变化)
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }


    /**
     * (无变化) saveRecord()
     * (此函数依赖 selectedAccount，它是正确的，所以无需修改)
     */
    private fun saveRecord() {
        // ... (无变化)
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val dateStr = binding.etDate.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (selectedAccount == null) {
            Toast.makeText(this, "请选择一个账户", Toast.LENGTH_SHORT).show()
            binding.etAccount.performClick()
            return
        }
        val selectedAccountId = selectedAccount!!.id

        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "金额和日期不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "金额格式无效", Toast.LENGTH_SHORT).show()
            return
        }
        val dateMillis = try {
            dateFormat.parse(dateStr)?.time ?: return
        } catch (e: Exception) {
            return
        }

        val type = if (isIncome) "income" else "expense"
        val categoryValue = if (category == otherCategoryString) null else category
        val noteValue = if (note.isEmpty()) null else note

        if (currentTransactionId == 0L) {
            // --- 新增模式 ---
            // (!!! activeLedgerId 是在 onCreate 获取的，是正确的 !!!)
            val transaction = Transaction(
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue,
                ledgerId = activeLedgerId,
                accountId = selectedAccountId
            )
            viewModel.insertTransaction(transaction)

        } else {
            // --- 编辑模式 ---
            val originalLedgerId = currentTransaction?.ledgerId ?: activeLedgerId
            val updatedTransaction = Transaction(
                id = currentTransactionId,
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue,
                ledgerId = originalLedgerId,
                accountId = selectedAccountId
            )
            viewModel.updateTransaction(updatedTransaction)
        }
    }
}