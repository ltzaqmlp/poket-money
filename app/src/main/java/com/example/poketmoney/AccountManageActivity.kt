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
// 导入 (无变化)
import com.example.poketmoney.databinding.DialogAddEditAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.DecimalFormat

/**
 * 账户管理页面
 *
 * (!!! 已重构：实现了 增/删/改/查 全部功能 !!!)
 * (!!! 已修改：现在所有操作都与 activeLedgerId 关联 !!!)
 */
class AccountManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountManageBinding
    private lateinit var adapter: AccountAdapter

    // (无变化)
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val accountDao by lazy { db.accountDao() }

    // !!! 1. 新增：获取当前激活的账本 ID
    /**
     * 当前激活的账本 ID。
     * 此页面上的所有账户操作都将基于此 ID。
     */
    private var activeLedgerId: Long = 1L

    // (无变化)
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

    // (无变化)
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable
    private var currentAccountList = listOf<Account>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAccountManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // !!! 2. 新增：在 OnCreate 时获取 activeLedgerId
        activeLedgerId = LedgerManager.getActiveLedgerId(this)

        // (无变化)
        currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // (无变化)
        swipeBackground = ColorDrawable(Color.RED)
        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!

        // (无变化)
        binding.btnBack.setOnClickListener {
            finish()
        }

        // (无变化)
        binding.fabAddAccount.setOnClickListener {
            showAccountDialog(null)
        }

        // (无变化)
        setupRecyclerView()
        setupSwipeToDelete()

        // !!! 3. 修改：调用 observeAccountList (现在内部已修改)
        observeAccountList()

        // (无变化)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * (无变化) 初始化 RecyclerView 和 Adapter
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
     * (!!! 4. 修改：观察数据库 Account 表的 LiveData !!!)
     */
    private fun observeAccountList() {
        // (!!! 修改：传入 activeLedgerId !!!)
        accountDao.getAllAccounts(activeLedgerId).observe(this) { accountsList ->
            currentAccountList = accountsList // 保存副本
            adapter.submitList(accountsList)
            checkEmptyState(accountsList)
        }
    }

    /**
     * (无变化) 检查列表是否为空，并切换提示视图
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
     * (!!! 5. 修改：显示“新增”或“修改”账户的对话框 !!!)
     * @param account 如果为 null，则是 "新增模式"；否则为 "修改模式"
     */
    private fun showAccountDialog(account: Account?) {
        val isEditMode = account != null

        val dialogBinding = DialogAddEditAccountBinding.inflate(LayoutInflater.from(this))
        dialogBinding.spinnerCurrency.adapter = currencyAdapter

        if (isEditMode) {
            // --- 修改模式 ---
            dialogBinding.tvDialogTitle.text = getString(R.string.edit_account_title)
            dialogBinding.etAccountType.setText(account!!.type)
            dialogBinding.etBalance.setText(account.balance.toString())
            dialogBinding.etNote.setText(account.note ?: "")
            val currencyPosition = currencies.indexOf(account.currency)
            if (currencyPosition >= 0) {
                dialogBinding.spinnerCurrency.setSelection(currencyPosition)
            }
        } else {
            // --- 新增模式 ---
            dialogBinding.tvDialogTitle.text = getString(R.string.add_account_title)
            dialogBinding.etBalance.setText("0.0")
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()

        dialogBinding.btnDialogSave.setOnClickListener {
            // (无变化) 5.1. 获取输入
            val accountType = dialogBinding.etAccountType.text.toString().trim()
            val currency = dialogBinding.spinnerCurrency.selectedItem.toString()
            val balanceStr = dialogBinding.etBalance.text.toString().trim()
            val note = dialogBinding.etNote.text.toString().trim()
            val noteValue = if (note.isEmpty()) null else note

            // (无变化) 5.2. 验证
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

            // (!!! 修改 !!!) 5.3. 在后台保存到数据库
            lifecycleScope.launch {
                if (isEditMode) {
                    // --- 修改 ---
                    val updatedAccount = account!!.copy(
                        type = accountType,
                        currency = currency,
                        balance = balance,
                        note = noteValue
                        // (!!! ledgerId 保持不变 !!!)
                    )
                    accountDao.update(updatedAccount)
                } else {
                    // --- 新增 ---
                    val newAccount = Account(
                        type = accountType,
                        currency = currency,
                        balance = balance,
                        note = noteValue,
                        ledgerId = activeLedgerId // (!!! 新增：存入当前账本 ID !!!)
                    )
                    accountDao.insert(newAccount)
                }
            }

            dialog.dismiss()
        }

        // (无变化) 6. 设置“取消”按钮
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    /**
     * (无变化) 设置 RecyclerView 的滑动删除功能
     */
    private fun setupSwipeToDelete() {
        // ... (此函数无变化)
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
     * (!!! 6. 修改：删除确认对话框 !!!)
     */
    private fun showDeleteConfirmationDialog(accountToDelete: Account, position: Int) {

        // (!!! 修改：现在可以删除最后一个账户了，因为它是 *当前账本* 的最后一个)
        // (旧规则：if (currentAccountList.size <= 1))

        // (无变化) 格式化 HTML 消息
        val message = Html.fromHtml(getString(R.string.delete_account_dialog_message, accountToDelete.type), Html.FROM_HTML_MODE_LEGACY)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_account_dialog_title)
            .setMessage(message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                adapter.notifyItemChanged(position) // 撤销滑动
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                // (!!! 警告 !!!)
                // (!!! 删除账户会通过 v4->v5 迁移 中的 onDelete = SET_NULL
                // (!!! 设置关联交易的 accountId 为 NULL)
                lifecycleScope.launch {
                    accountDao.delete(accountToDelete)
                }
                dialog.dismiss()
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position) // 撤销滑动
            }
            .show()
    }
}