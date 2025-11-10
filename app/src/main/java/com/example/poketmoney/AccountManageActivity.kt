package com.example.poketmoney

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ActivityAccountManageBinding
// 1. !!! 新增：导入新弹窗的 Binding !!!
import com.example.poketmoney.databinding.DialogAddEditAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.DecimalFormat

/**
 * 账户管理页面
 *
 * (!!! 已更新：现在根据 'activeLedgerId' 过滤账户 !!!)
 */
class AccountManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountManageBinding
    private lateinit var adapter: AccountAdapter

    // 2. !!! 新增：数据库和协程 !!!
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val accountDao by lazy { db.accountDao() }

    // 3. !!! 新增：币种列表 (从已删除的 AddAccountActivity 移入) !!!
    private val currencies = listOf(
        "CNY (人民币)",
        "HKD (港币)",
        "MOP (澳币)",
        "USD (美元)",
        "EUR (欧元)",
        "KRW (韩元)",
        "JPY (日元)",
        "GBP (英镑)",
        "TWD (台币)"
    )
    private lateinit var currencyAdapter: ArrayAdapter<String>

    // 4. !!! 新增：滑动删除所需变量 !!!
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable
    private var currentAccountList = listOf<Account>() // 用于检查 "删除最后一个"

    // 5. !!! (新增) 跟踪当前激活的账本ID !!!
    private var activeLedgerId: Long = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAccountManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 6. !!! (新增) 获取当前激活的账本ID !!!
        activeLedgerId = LedgerManager.getActiveLedgerId(this)

        // 7. !!! 新增：初始化币种 Adapter !!!
        currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 8. !!! 新增：初始化滑动删除的图标和背景 !!!
        swipeBackground = ColorDrawable(Color.RED)
        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!

        // 9. 返回按钮 (无变化)
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 10. !!! 修改："新增账户" 按钮 !!!
        //    (不再启动 Activity，而是显示弹窗)
        binding.fabAddAccount.setOnClickListener {
            // 传入 null 表示是 "新增模式"
            showAccountDialog(null)
        }

        // 11. !!! 新增：初始化 RecyclerView !!!
        setupRecyclerView()

        // 12. !!! 新增：设置滑动删除 !!!
        setupSwipeToDelete()

        // 13. !!! 新增：开始观察数据库 !!!
        observeAccountList()

        // 14. 窗口边距 (无变化)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * (!!! 新增 !!!) 初始化 RecyclerView 和 Adapter
     */
    private fun setupRecyclerView() {
        adapter = AccountAdapter()
        binding.recyclerViewAccounts.adapter = adapter
        binding.recyclerViewAccounts.layoutManager = LinearLayoutManager(this)

        // (未来) 账户点击事件
        adapter.onItemClick = { account ->
            // TODO: (未来) 点击账户可以设为 "默认" 或查看详情
        }

        // "笔" 图标点击事件
        adapter.onEditClick = { account ->
            // 传入账户对象，表示是 "修改模式"
            showAccountDialog(account)
        }
    }

    /**
     * (!!! 关键修改：观察数据库 Account 表 (按 ledgerId 过滤) !!!)
     */
    private fun observeAccountList() {
        // (!!! 修改：调用 getAllAccountsByLedger 并传入 activeLedgerId !!!)
        accountDao.getAllAccountsByLedger(activeLedgerId).observe(this) { accountsList ->
            currentAccountList = accountsList // 保存副本
            adapter.submitList(accountsList)
            checkEmptyState(accountsList)
        }
    }

    /**
     * (!!! 新增 !!!) 检查列表是否为空，并切换提示视图
     */
    private fun checkEmptyState(accountsList: List<Account>) {
        if (accountsList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewAccounts.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewAccounts.visibility = View.VISIBLE
        }
    }

    /**
     * (!!! 关键修改：新增账户时必须存入 ledgerId !!!)
     * 显示“新增”或“修改”账户的对话框
     * @param account 如果为 null，则是 "新增模式"；否则为 "修改模式"
     */
    private fun showAccountDialog(account: Account?) {
        val isEditMode = account != null

        // 1. 加载弹窗布局 (dialog_add_edit_account.xml)
        val dialogBinding = DialogAddEditAccountBinding.inflate(LayoutInflater.from(this))

        // 2. 将币种 Adapter 设置到弹窗的 Spinner
        dialogBinding.spinnerCurrency.adapter = currencyAdapter

        // 3. 设置弹窗标题和预填文本
        if (isEditMode) {
            // --- 修改模式 ---
            dialogBinding.tvDialogTitle.text = getString(R.string.edit_account_title)
            dialogBinding.etAccountType.setText(account!!.type)
            dialogBinding.etBalance.setText(account.balance.toString())
            dialogBinding.etNote.setText(account.note ?: "")
            // 自动选中该账户的币种
            val currencyPosition = currencies.indexOf(account.currency)
            if (currencyPosition >= 0) {
                dialogBinding.spinnerCurrency.setSelection(currencyPosition)
            }
        } else {
            // --- 新增模式 ---
            dialogBinding.tvDialogTitle.text = getString(R.string.add_account_title)
            // (余额默认为 0.0)
            dialogBinding.etBalance.setText("0.0")
        }

        // 4. 创建并显示 AlertDialog
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
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
                Toast.makeText(this, R.string.error_account_validation_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 5.3. 在后台保存到数据库
            lifecycleScope.launch {
                if (isEditMode) {
                    // --- 修改 ---
                    // (ledgerId 在 copy() 中被保留，无需改动)
                    val updatedAccount = account!!.copy(
                        type = accountType,
                        currency = currency,
                        balance = balance,
                        note = noteValue
                    )
                    accountDao.update(updatedAccount)
                } else {
                    // --- 新增 ---
                    // (!!! 关键：存入 activeLedgerId !!!)
                    val newAccount = Account(
                        type = accountType,
                        currency = currency,
                        balance = balance,
                        note = noteValue,
                        ledgerId = activeLedgerId // 关联到当前账本
                    )
                    accountDao.insert(newAccount)
                }
            }

            dialog.dismiss()
        }

        // 6. 设置“取消”按钮
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    /**
     * (!!! 新增 !!!) 设置 RecyclerView 的滑动删除功能
     */
    private fun setupSwipeToDelete() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0, // 不支持拖拽
            ItemTouchHelper.LEFT // 只支持向左滑动
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // 不处理拖拽
            }

            // onSwiped 在用户滑动列表项时触发
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val accountToDelete = adapter.currentList[position]

                // 显示删除确认对话框
                showDeleteConfirmationDialog(accountToDelete, position)
            }

            // onChildDraw 用于在滑动时绘制自定义背景（红色）和图标
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX < 0) { // 向左滑动
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    swipeBackground.draw(c)

                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewAccounts)
    }

    /**
     * (!!! 新增 !!!) 显示“删除”二次确认对话框
     */
    private fun showDeleteConfirmationDialog(accountToDelete: Account, position: Int) {

        // 规则 1：不能删除最后一个账户
        // (!!! 修改：现在是检查 *当前账本* 的最后一个账户 !!!)
        if (currentAccountList.size <= 1) {
            Toast.makeText(this, R.string.error_account_delete_last, Toast.LENGTH_LONG).show()
            adapter.notifyItemChanged(position) // 撤销滑动
            return
        }

        // 格式化 HTML 消息
        val message = Html.fromHtml(getString(R.string.delete_account_dialog_message, accountToDelete.type), Html.FROM_HTML_MODE_LEGACY)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_account_dialog_title)
            .setMessage(message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                adapter.notifyItemChanged(position) // 撤销滑动
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                // 在后台删除
                lifecycleScope.launch {
                    accountDao.delete(accountToDelete)

                    // (注意：由于 Transaction.accountId 设置了 'ON DELETE SET NULL',
                    //  删除账户时，关联的交易记录的 accountId 会自动变 null,
                    //  这符合我们的需求——历史记录不丢失。)
                }
                dialog.dismiss()
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position) // 撤销滑动
            }
            .show()
    }
}