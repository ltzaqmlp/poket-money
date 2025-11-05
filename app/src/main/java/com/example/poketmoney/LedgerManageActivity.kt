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
import com.example.poketmoney.databinding.ActivityLedgerManageBinding
import com.example.poketmoney.databinding.DialogAddLedgerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * 账本管理页面
 *
 * (!!! 已更新：支持 "首次启动" 强制模式 !!!)
 */
class LedgerManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLedgerManageBinding
    private lateinit var adapter: LedgerAdapter

    // (无变化)
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val ledgerDao by lazy { db.ledgerDao() }
    private var activeLedgerId: Long = 1L
    private var currentLedgerList = listOf<Ledger>()
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable

    // 1. !!! 新增：用于判断是否为 "强制模式" !!!
    private var isFirstLaunchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLedgerManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. !!! 新增：检查是否为 "强制模式" !!!
        // (我们稍后会在 MainActivity 中传入这个 "IS_FIRST_LAUNCH" extra)
        isFirstLaunchMode = intent.getBooleanExtra("IS_FIRST_LAUNCH", false)

        // (无变化)
        activeLedgerId = LedgerManager.getActiveLedgerId(this)
        swipeBackground = ColorDrawable(Color.RED)
        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!

        // 3. !!! 修改：返回按钮逻辑 !!!
        if (isFirstLaunchMode) {
            // 3.1. "强制模式"：隐藏返回按钮
            binding.btnBack.visibility = View.GONE
        } else {
            // 3.2. "普通模式"：设置点击事件
            binding.btnBack.visibility = View.VISIBLE
            binding.btnBack.setOnClickListener {
                finish()
            }
        }

        // (无变化) "新增账本"
        binding.fabAddLedger.setOnClickListener {
            showLedgerDialog(null)
        }

        // (无变化)
        setupRecyclerView()
        setupSwipeToDelete()
        observeLedgers()

        // (无变化)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // (无变化)
    private fun observeLedgers() {
        //
        ledgerDao.getAllLedgers().observe(this) { ledgers ->
            currentLedgerList = ledgers
            adapter.submitList(ledgers)

            // 4. !!! 修改：空状态检查 !!!
            checkEmptyState(ledgers)

            // 5. !!! 新增：如果在 "强制模式" 下，账本不再为空
            //    (即用户刚创建了第一个账本)，
            //    我们自动将这个新账本设为激活，并返回首页。
            if (isFirstLaunchMode && ledgers.isNotEmpty()) {
                // 5.1. 获取刚创建的账本 (它在列表顶部，因为我们按 ID DESC 排序)
                val firstLedger = ledgers[0]

                // 5.2. 切换账本并返回首页
                // (调用我们之前在 "Bug 修复" 中创建的切换逻辑)
                switchLedgerAndGoToMain(firstLedger)
            }
        }
    }

    /**
     * (!!! 修改 !!!)
     * 初始化 RecyclerView 和 Adapter
     */
    private fun setupRecyclerView() {
        adapter = LedgerAdapter()
        binding.recyclerViewLedgers.adapter = adapter
        binding.recyclerViewLedgers.layoutManager = LinearLayoutManager(this)

        // 交互 1: (单击列表项) -> 切换账本
        adapter.onItemClick = { ledger ->
            // (!!! 修改 !!!)
            // 只有在 "非强制模式" 下才允许自由切换
            if (!isFirstLaunchMode) {
                switchLedgerAndGoToMain(ledger)
            } else {
                // 在 "强制模式" 下，如果点击了（唯一的）账本，
                // 也应该触发 "编辑"
                showLedgerDialog(ledger)
            }
        }

        // 交互 2: (单击 "笔" 图标) -> 编辑账本 (无变化)
        adapter.onEditClick = { ledger ->
            showLedgerDialog(ledger)
        }
    }

    /**
     * (无变化)
     * 封装“切换账本并跳转首页”的逻辑
     *
     */
    private fun switchLedgerAndGoToMain(ledger: Ledger) {
        LedgerManager.setActiveLedgerId(this, ledger.id) //
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    // (无变化)
    private fun setupSwipeToDelete() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val ledgerToDelete = adapter.currentList[position]
                showDeleteConfirmationDialog(ledgerToDelete, position)
            }
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                if (dX < 0) {
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
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewLedgers)
    }


    /**
     * (!!! 已修改 !!!)
     * 修复了 'spinnerCurrency' 的 Bug
     *
     */
    private fun showLedgerDialog(ledger: Ledger?) {
        val isEditMode = ledger != null

        val dialogBinding = DialogAddLedgerBinding.inflate(LayoutInflater.from(this)) //

        // (!!! 新增 !!!) "强制模式" 且 "新增" 时，隐藏取消按钮
        if (isFirstLaunchMode && !isEditMode) {
            dialogBinding.btnDialogCancel.visibility = View.GONE
        } else {
            dialogBinding.btnDialogCancel.visibility = View.VISIBLE
        }

        // (已修复) 移除了对 'spinnerCurrency' 的错误引用

        if (isEditMode) {
            dialogBinding.tvDialogTitle.text = getString(R.string.edit_ledger_dialog_title) //
            dialogBinding.etLedgerName.setText(ledger?.name)
        } else {
            dialogBinding.tvDialogTitle.text = getString(R.string.add_ledger_dialog_title) //
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            // (!!! 新增 !!!) "强制模式" 下，禁止点击外部取消
            .setCancelable(!isFirstLaunchMode)
            .show()

        dialogBinding.btnDialogSave.setOnClickListener {
            val newName = dialogBinding.etLedgerName.text.toString().trim()

            lifecycleScope.launch {
                val validationError = validateLedgerName(newName, ledger?.id)

                if (validationError == null) {
                    if (isEditMode) {
                        ledgerDao.update(ledger!!.copy(name = newName)) //
                    } else {
                        ledgerDao.insert(Ledger(name = newName)) //
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@LedgerManageActivity, validationError, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    // (无变化)
    private suspend fun validateLedgerName(newName: String, currentId: Long?): String? {
        if (newName.isEmpty()) {
            return getString(R.string.error_ledger_name_empty) //
        }
        val existingLedger = ledgerDao.findLedgerByName(newName) //
        if (existingLedger != null) {
            if (currentId == null) {
                return getString(R.string.error_ledger_name_exists) //
            } else {
                if (existingLedger.id != currentId) {
                    return getString(R.string.error_ledger_name_exists) //
                }
            }
        }
        return null
    }

    // (!!! 已修改 !!!)
    private fun showDeleteConfirmationDialog(ledgerToDelete: Ledger, position: Int) {

        // (!!! 关键：确保 "强制模式" 下不能删除 !!!)
        // (你的需求是 "不允许账本全部删除")
        if (currentLedgerList.size <= 1) {
            val message = if (isFirstLaunchMode) {
                "请先完成第一个账本的创建"
            } else {
                getString(R.string.error_ledger_delete_last) //
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            adapter.notifyItemChanged(position)
            return
        }

        val message = Html.fromHtml(getString(R.string.delete_ledger_dialog_message, ledgerToDelete.name), Html.FROM_HTML_MODE_LEGACY) //

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_ledger_dialog_title) //
            .setMessage(message)
            .setNegativeButton(R.string.cancel) { dialog, _ -> //
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ -> //
                lifecycleScope.launch {
                    ledgerDao.delete(ledgerToDelete) //
                    if (activeLedgerId == ledgerToDelete.id) {
                        val newActiveLedger = ledgerDao.getFirstLedger() //
                        if (newActiveLedger != null) {
                            LedgerManager.setActiveLedgerId(this@LedgerManageActivity, newActiveLedger.id) //
                        }
                    }
                }
                dialog.dismiss()
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }


    /**
     * (!!! 修复 Bug !!!)
     * 检查列表是否为空，并切换提示视图
     * (修复了 List<Account> 的 copy-paste 错误)
     */
    private fun checkEmptyState(ledgersList: List<Ledger>) { // (!!! 修复：List<Ledger> !!!)
        if (ledgersList.isEmpty()) {
            // 列表为空

            // (!!! 新增 !!!)
            // 检查是否为 "强制模式"
            if (isFirstLaunchMode) {
                // 1. "强制模式"：显示 "欢迎提示"
                binding.tvEmptyState.text = getString(R.string.ledger_manage_welcome_prompt) //
            } else {
                // 2. "普通模式"：显示 "没有账本"
                binding.tvEmptyState.text = getString(R.string.no_ledgers_found) //
            }

            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewLedgers.visibility = View.GONE
        } else {
            // 列表不为空
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewLedgers.visibility = View.VISIBLE
        }
    }
}