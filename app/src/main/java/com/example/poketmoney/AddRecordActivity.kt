package com.example.poketmoney

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
<<<<<<< HEAD
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
=======
// import android.widget.Spinner // Spinner 是 binding 的一部分，不需要单独导入
import android.widget.Toast
>>>>>>> temp-branch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
// import androidx.lifecycle.lifecycleScope // (未使用的导入 - 已移除)
import com.example.poketmoney.databinding.ActivityAddRecordBinding
// 导入 (无变化)
import com.example.poketmoney.databinding.DialogAddEditAccountBinding
import com.example.poketmoney.databinding.DialogSelectAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import com.example.poketmoney.CategoryManager
import com.example.poketmoney.LedgerManager
// (!!! 1. 新增：导入弹窗所需的 Binding !!!)
import com.example.poketmoney.databinding.DialogAddEditAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
// import kotlinx.coroutines.launch // (未使用的导入 - 已移除)

/**
 * (!!! 已重构：全面支持 "账户" 和 "余额同步" 逻辑 !!!)
 * (!!! 已更新：支持在下拉列表中 "新增账户" !!!)
 * (!!! Bug 修复：修复了 "新增" 按钮二次点击无效的问题 !!!)
 * (!!! 优化 #2：实现 "自动弹窗" 和 "条件提示符" !!!)
 * (!!! Bug 修复：修复了编译错误 !!!)
 */
class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var viewModel: AddRecordViewModel
    private var isIncome: Boolean = true

<<<<<<< HEAD
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
=======
    // (无变化) "分类" 下拉列表
    private val currentCategoryList = mutableListOf<String>()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    // (!!! 3. "账户" 下拉列表 (已修改) !!!)
    private val currentAccountList = mutableListOf<Account>() // 真实的 Account 对象
    private lateinit var accountAdapter: ArrayAdapter<String>
    private val accountDisplayList = mutableListOf<String>() // 用于在 Spinner 中显示的字符串
    private lateinit var addNewAccountString: String // “+ 新增账户”
    private lateinit var selectAccountPromptString: String // “--- 请选择一个账户 ---”
    private var lastAccountSpinnerPosition = 0

    // (!!! 20. 新增：用于 "自动弹窗" 的标志 !!!)
    private var hasAutoShownAccountDialog = false

    // (!!! 21. 新增：用于 "索引计算" 的辅助标志 !!!)
    /**
     * `true`  = accountDisplayList[0] 是 "--- 请选择 ---"
     * `false` = accountDisplayList[0] 是第一个真实账户
     */
    private var hasPrompt: Boolean = false


    // (!!! 4. 关键：获取当前账本 ID !!!)
    private var activeLedgerId: Long = 1L

    // (!!! 5. 关键：在 "编辑模式" 下，必须持有原始交易 !!!)
    private var currentTransactionId: Long = 0L
    private var currentTransaction: Transaction? = null // (在 "编辑模式" 下，这将持有原始交易)

    // (无变化)
>>>>>>> temp-branch
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val otherCategoryString = "其他"

    // (!!! 15. 新增：用于 "新增账户" 弹窗的币种列表 !!!)
    private val currencies = listOf(
        "CNY (人民币)", "HKD (港币)", "MOP (澳币)", "USD (美元)",
        "EUR (欧元)", "KRW (韩元)", "JPY (日元)", "GBP (英镑)", "TWD (台币)"
    )
    private lateinit var currencyAdapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]
        currentTransactionId = intent.getLongExtra("TRANSACTION_ID", 0L)

<<<<<<< HEAD
        // !!! 2. 新增：在 OnCreate 时获取 activeLedgerId
        activeLedgerId = LedgerManager.getActiveLedgerId(this)

        // (无变化)
=======
        // (!!! 6. 关键：获取当前账本ID 和 字符串 !!!)
        activeLedgerId = LedgerManager.getActiveLedgerId(this)
        addNewAccountString = getString(R.string.add_new_account) // “+ 新增账户”
        selectAccountPromptString = getString(R.string.select_account_prompt) // “--- 请选择 ---”

        // (!!! 7. 设置 "分类" Spinner !!!) (!!! 错误修复 !!!)
>>>>>>> temp-branch
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentCategoryList)
        // (!!! 修复：使用 android.R.layout !!!)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // (!!! 8. 新增：设置 "账户" Spinner !!!) (!!! 错误修复 !!!)
        accountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountDisplayList)
        // (!!! 修复：使用 android.R.layout !!!)
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAccount.adapter = accountAdapter

        // (!!! 16. 新增：初始化 "新增账户" 弹窗所需的币种 Adapter !!!) (!!! 错误修复 !!!)
        currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        // (!!! 修复：使用 android.R.layout !!!)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // (!!! 17. 新增：设置 "账户" 下拉框的点击事件 !!!)
        setupAccountSpinnerListener()

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

            // (!!! 9. 新增：加载账户列表 !!!)
            viewModel.loadAccounts(activeLedgerId)

        } else {
            // --- 编辑模式 ---
            binding.tvTitle.text = getString(R.string.edit_record)
            binding.btnDelete.visibility = View.VISIBLE
            // (!!! 10. 修改：加载交易记录 和 账户列表 !!!)
            viewModel.loadTransaction(currentTransactionId)
            viewModel.loadAccounts(activeLedgerId)
        }
    }

    /**
     * (!!! 11. 关键修改：观察 ViewModel !!!)
     */
    private fun observeViewModel() {

        // (!!! 11.1 关键修改：观察账户列表, 实现 "条件提示符" 和 "自动弹窗" !!!)
        viewModel.accounts.observe(this) { accounts ->
            currentAccountList.clear()
            currentAccountList.addAll(accounts) // 真实数据 (已按 ID 倒序)

            accountDisplayList.clear()

            // (!!! 优化 #1：条件提示符 !!!)
            if (accounts.isEmpty()) {
                // --- 列表为空 ---
                hasPrompt = true
                accountDisplayList.add(selectAccountPromptString) // [0] = "--- 请选择 ---"

                // (!!! 优化 #2：自动弹窗 !!!)
                // (并且不是在 "编辑" 模式下)
                if (currentTransactionId == 0L && !hasAutoShownAccountDialog) {
                    showAccountDialog()
                    hasAutoShownAccountDialog = true // 确保只自动弹出一次
                }

            } else {
                // --- 列表不为空 ---
                hasPrompt = false
                // (不添加 "请选择" 提示符)
            }

            // (添加真实账户)
            accountDisplayList.addAll(accounts.map {
                "${it.type} (${it.currency} ${"%.2f".format(it.balance)})"
            })

            // (在列表末尾添加 "+ 新增账户")
            accountDisplayList.add(addNewAccountString)

            accountAdapter.notifyDataSetChanged()

            // (重要) 如果是 "编辑模式"，在账户列表加载后，尝试选中 "原始账户"
            if (currentTransactionId != 0L) {
                currentTransaction?.let {
                    selectAccountInSpinner(it.accountId)
                }
            }
        }

        // (!!! 11.2 修改：观察待编辑的交易 !!!)
        viewModel.transactionToEdit.observe(this) { transaction ->
            if (transaction != null) {
                currentTransaction = transaction
                isIncome = transaction.type == "income"
                binding.etAmount.setText(transaction.amount.toString())
                binding.etNote.setText(transaction.note ?: "")
                binding.etDate.setText(dateFormat.format(Date(transaction.date)))
                setupCategorySpinner(isIncome, transaction.category)

                // (如果 currentAccountList 已经加载了)
                if (accountDisplayList.isNotEmpty()) {
                    selectAccountInSpinner(transaction.accountId)
                }
            }
        }

        // (!!! 11.3 关键修改：观察 "新创建的账户" 信号 !!!)
        viewModel.newlyCreatedAccountId.observe(this) { newAccountId ->
            if (newAccountId != null) {
                // 新账户已创建！
                // (此时 viewModel.accounts 观察者会重新触发,
                //  列表会刷新, hasPrompt 会变为 false)

                // 1. 在 (已刷新的) 真实列表中找到它的索引
                //    (因为已按 ID 倒序，新账户总是在 [0])
                val newIndex = currentAccountList.indexOfFirst { it.id == newAccountId }

                if (newIndex >= 0) {
                    // 2. 自动选中它
                    // (!!! 关键：索引计算 !!!)
                    // (因为 hasPrompt 此时为 false, 索引是 1:1 对应的)
                    val uiIndex = newIndex
                    binding.spinnerAccount.setSelection(uiIndex)
                    lastAccountSpinnerPosition = uiIndex
                }
                // 3. 重置信号
                viewModel.doneNavigatingToNewAccount()
            }
        }

        // (无变化)
        viewModel.shouldCloseActivity.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
                viewModel.doneClosingActivity()
            }
        }
    }

    /**
     * (!!! 12. 关键修改：辅助函数 - 选中 Spinner 中的账户 !!!)
     */
    private fun selectAccountInSpinner(accountIdToSelect: Long?) {
        if (accountIdToSelect == null) return

        // 在我们本地的 "Account" 列表中找到该 ID 的索引
        val index = currentAccountList.indexOfFirst { it.id == accountIdToSelect }

        if (index >= 0) {
            // (!!! 关键：索引计算 !!!)
            // 如果列表有提示符 (hasPrompt=true), UI 索引 = 真实索引 + 1
            // 如果列表无提示符 (hasPrompt=false), UI 索引 = 真实索引
            val uiIndex = if (hasPrompt) index + 1 else index

            // (安全检查，防止索引越界)
            if (uiIndex < binding.spinnerAccount.adapter.count) {
                binding.spinnerAccount.setSelection(uiIndex)
                lastAccountSpinnerPosition = uiIndex
            }
        }
    }

    /**
     * (!!! 18. 关键修改：设置 "账户" 下拉框的点击事件 !!!)
     */
    private fun setupAccountSpinnerListener() {
        binding.spinnerAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                // 检查是否点击了最后一项 (即 "+ 新增账户")
                if (position == accountDisplayList.size - 1) {
                    // 是 "+ 新增账户"

                    // 1. 立即弹出 "新增账户" 对话框
                    showAccountDialog()

                    // 2. (重要) 将下拉框的选择恢复到 "点击前" 的位置
                    //    (防止 spinner 停留在 "+ 新增" 选项上)
                    binding.spinnerAccount.setSelection(lastAccountSpinnerPosition)

                } else {
                    // 选择了真实的账户 (或 "--- 请选择 ---")
                    // 1. 记录这个新位置
                    lastAccountSpinnerPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 默认选择 0
                lastAccountSpinnerPosition = 0
            }
        }
    }

    /**
     * (!!! 19. 新增：显示 "新增账户" 弹窗 !!!)
     * (此逻辑从 AccountManageActivity 复制并适配)
     */
    private fun showAccountDialog() {
        // 1. 加载弹窗布局 (dialog_add_edit_account.xml)
        val dialogBinding = DialogAddEditAccountBinding.inflate(LayoutInflater.from(this))

        // 2. 将币种 Adapter 设置到弹窗的 Spinner
        dialogBinding.spinnerCurrency.adapter = currencyAdapter

        // 3. 设置弹窗标题 (强制 "新增" 模式)
        dialogBinding.tvDialogTitle.text = getString(R.string.add_account_title)
        dialogBinding.etBalance.setText("0.0")

        // 4. 创建并显示 AlertDialog
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false) // 不可取消
            .show()

        // 5. 设置“保存”按钮的点击事件
        dialogBinding.btnDialogSave.setOnClickListener {
            // 5.1. 获取输入
            val accountType = dialogBinding.etAccountType.text.toString().trim()
            val currency = dialogBinding.spinnerCurrency.selectedItem.toString()
            val balanceStr = dialogBinding.etBalance.text.toString().trim()
            val note = dialogBinding.etNote.text.toString().trim()
            val noteValue = if (note.isEmpty()) null else note

            // 5.2. 验证
            if (accountType.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(this, R.string.error_account_validation_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val balance = try {
                balanceStr.toDouble()
            } catch (e: NumberFormatException) {
                // (修复警告：使用 'e')
                Toast.makeText(this, "无效的余额: ${e.message}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 5.3. (!!! 关键：调用 ViewModel 的 insertAccount !!!)
            val newAccount = Account(
                type = accountType,
                currency = currency,
                balance = balance,
                note = noteValue,
                ledgerId = activeLedgerId // 关联到当前账本
            )
            viewModel.insertAccount(newAccount)

            // (ViewModel 会自动刷新列表, 且 11.3 中的观察者会自动选中新账户)
            dialog.dismiss()
        }

        // 6. 设置“取消”按钮
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }


    /**
     * (!!! 13. 关键修改：saveRecord() !!!)
     * (!!! 已更新：索引计算已更新 !!!)
     */
    private fun saveRecord() {
        // --- 1. 获取所有输入 ---
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val dateStr = binding.etDate.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        // (!!! 关键检查：获取选中的账户 !!!)
        val selectedAccountIndex = binding.spinnerAccount.selectedItemPosition

        // (!!! 检查 1：是否选中的是 "+ 新增" (最后一项) !!!)
        if (selectedAccountIndex == accountDisplayList.size - 1) {
            Toast.makeText(this, "请选择一个有效账户", Toast.LENGTH_SHORT).show()
            return
        }

        // (!!! 检查 2：如果列表有提示符, 检查是否选中的是 "提示" (位置 0) !!!)
        if (hasPrompt && selectedAccountIndex == 0) {
            Toast.makeText(this, "请选择一个账户", Toast.LENGTH_SHORT).show()
            return
        }

        // (!!! 检查 3：如果列表为空 (除了提示符和+新增), 阻止保存 !!!)
        if (currentAccountList.isEmpty()) {
            Toast.makeText(this, "请先创建或选择一个账户", Toast.LENGTH_SHORT).show()
            return
        }

        // (!!! 关键：获取真实的 Account 对象 (计算索引偏移) !!!)
        // 如果 hasPrompt=true, 真实索引 = UI 索引 - 1
        // 如果 hasPrompt=false, 真实索引 = UI 索引
        val realIndex = if (hasPrompt) selectedAccountIndex - 1 else selectedAccountIndex
        val selectedAccount = currentAccountList[realIndex]

        // --- 2. (无变化) 验证 ---
        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "金额和日期不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            // (修复警告：使用 'e')
            Toast.makeText(this, "无效的金额: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }
        val dateMillis = try {
            dateFormat.parse(dateStr)?.time ?: return
        } catch (e: Exception) {
            // (修复警告：使用 'e')
            Toast.makeText(this, "无效的日期: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }
        val type = if (isIncome) "income" else "expense"
        val categoryValue = if (category == otherCategoryString) null else category
        val noteValue = if (note.isEmpty()) null else note


        // --- 3. (!!! 关键：调用 ViewModel !!!) ---
        if (currentTransactionId == 0L) {
            // --- A. 新增模式 ---

            val transaction = Transaction(
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue,
                ledgerId = activeLedgerId, // (!!! 存入账本ID !!!)
                accountId = selectedAccount.id // (!!! 存入账户ID !!!)
            )
            viewModel.insertTransaction(transaction)

        } else {
            // --- B. 编辑模式 ---

            val oldTransaction = currentTransaction
            if (oldTransaction == null) {
                Toast.makeText(this, "无法加载原始交易，更新失败", Toast.LENGTH_SHORT).show()
                return
            }

            val updatedTransaction = Transaction(
                id = currentTransactionId, // (保持 ID 不变)
                amount = amount,
                type = type,
                category = categoryValue,
                date = dateMillis,
                note = noteValue,
                ledgerId = activeLedgerId, // (保持原始账本ID)
                accountId = selectedAccount.id // (!!! 存入 *新* 选中的账户ID !!!)
            )

            viewModel.updateTransaction(oldTransaction, updatedTransaction)
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

<<<<<<< HEAD
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
=======
    // (!!! 错误修复 !!!)
>>>>>>> temp-branch
    private fun setupDatePicker() {
        // ... (无变化)
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            currentTransaction?.let {
                calendar.timeInMillis = it.date
            }
            // (!!! 修复：移除 '::' !!!)
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

    /**
     * (!!! 14. 关键修改：设置 "删除" 按钮的逻辑 !!!)
     */
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

        // (!!! 修改：调用 ViewModel 的新 deleteTransaction 方法 !!!)
        binding.btnDelete.setOnClickListener {
            currentTransaction?.let {
                // (调用新方法，它会自动同步余额)
                viewModel.deleteTransaction(it)
            }
        }
    }
<<<<<<< HEAD

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
=======
>>>>>>> temp-branch
}