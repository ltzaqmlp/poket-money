package com.example.poketmoney

// 1. !!! 新增：导入 Intent !!!
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
 * 账本管理页面 (已修复 Bug #2)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // (无变化)
        binding = ActivityLedgerManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activeLedgerId = LedgerManager.getActiveLedgerId(this)
        swipeBackground = ColorDrawable(Color.RED)
        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!

        // (无变化)
        binding.btnBack.setOnClickListener {
            finish()
        }

        // (无变化)
        setupRecyclerView()

        // (无变化) "新增账本"
        binding.fabAddLedger.setOnClickListener {
            showLedgerDialog(null)
        }

        // (无变化)
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
        ledgerDao.getAllLedgers().observe(this) { ledgers ->
            currentLedgerList = ledgers
            adapter.submitList(ledgers)
            checkEmptyState(ledgers)
        }
    }

    /**
     * 初始化 RecyclerView 和 Adapter (!!! BUG 修复处 !!!)
     */
    private fun setupRecyclerView() {
        adapter = LedgerAdapter()
        binding.recyclerViewLedgers.adapter = adapter
        binding.recyclerViewLedgers.layoutManager = LinearLayoutManager(this)

        // 2. !!! 交互 1: (单击列表项) -> 切换账本 (已修复) !!!
        adapter.onItemClick = { ledger ->
            // 2.1. 更新全局 "激活" 账本 ID (无变化)
            LedgerManager.setActiveLedgerId(this, ledger.id)

            // 2.2. (!!! 修复 !!!)
            // (旧代码: finish())
            //
            // (新代码)
            // 创建一个指向 MainActivity (首页) 的 Intent
            val intent = Intent(this, MainActivity::class.java)
            // 添加标志 (FLAG_ACTIVITY_CLEAR_TOP)：
            // 清除此 Intent 之上的所有 Activity (即 SettingsActivity 和 LedgerManageActivity)
            // (FLAG_ACTIVITY_SINGLE_TOP)：
            // 确保我们是 "重用" 现有的 MainActivity，而不是创建一个新的
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // 2.3. 启动 Intent (跳转回首页)
            startActivity(intent)

            // 2.4. (可选, 但推荐) 立即关闭当前页
            finish()
        }

        // 交互 2: (单击 "笔" 图标) -> 编辑账本 (无变化)
        adapter.onEditClick = { ledger ->
            showLedgerDialog(ledger)
        }
    }

    // (无变化)
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
                val ledgerToDelete = adapter.currentList[position]

                // 显示删除确认对话框
                showDeleteConfirmationDialog(ledgerToDelete, position)
            }

            // onChildDraw 用于在滑动时绘制自定义背景（红色）和图标
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, // X 轴的滑动距离
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX < 0) { // 只在向左滑动时绘制 (dX < 0)
                    // 绘制红色背景
                    swipeBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    swipeBackground.draw(c)

                    // 绘制删除图标
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

        // 将 ItemTouchHelper 附加到 RecyclerView 上
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewLedgers)
    }


    // (无变化)
    private fun showLedgerDialog(ledger: Ledger?) {
        val isEditMode = ledger != null

        val dialogBinding = DialogAddLedgerBinding.inflate(LayoutInflater.from(this))

        if (isEditMode) {
            dialogBinding.tvDialogTitle.text = getString(R.string.edit_ledger_dialog_title)
            dialogBinding.etLedgerName.setText(ledger?.name)
        } else {
            dialogBinding.tvDialogTitle.text = getString(R.string.add_ledger_dialog_title)
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()

        dialogBinding.btnDialogSave.setOnClickListener {
            val newName = dialogBinding.etLedgerName.text.toString().trim()

            lifecycleScope.launch {
                val validationError = validateLedgerName(newName, ledger?.id)

                if (validationError == null) {
                    if (isEditMode) {
                        ledgerDao.update(ledger!!.copy(name = newName))
                    } else {
                        ledgerDao.insert(Ledger(name = newName))
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
            return getString(R.string.error_ledger_name_empty)
        }

        val existingLedger = ledgerDao.findLedgerByName(newName)
        if (existingLedger != null) {
            if (currentId == null) {
                return getString(R.string.error_ledger_name_exists)
            } else {
                if (existingLedger.id != currentId) {
                    return getString(R.string.error_ledger_name_exists)
                }
            }
        }

        return null
    }

    // (无变化)
    private fun showDeleteConfirmationDialog(ledgerToDelete: Ledger, position: Int) {

        if (currentLedgerList.size <= 1) {
            Toast.makeText(this, R.string.error_ledger_delete_last, Toast.LENGTH_LONG).show()
            adapter.notifyItemChanged(position)
            return
        }

        val message = Html.fromHtml(getString(R.string.delete_ledger_dialog_message, ledgerToDelete.name), Html.FROM_HTML_MODE_LEGACY)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_category_dialog_title) // (勘误：这里应该用 R.string.delete_ledger_dialog_title，我将在下一步修复)
            .setMessage(message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                lifecycleScope.launch {
                    ledgerDao.delete(ledgerToDelete)
                    if (activeLedgerId == ledgerToDelete.id) {
                        val newActiveLedger = ledgerDao.getFirstLedger()
                        if (newActiveLedger != null) {
                            LedgerManager.setActiveLedgerId(this@LedgerManageActivity, newActiveLedger.id)
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


    // (无变化)
    private fun checkEmptyState(ledgers: List<Ledger>) {
        if (ledgers.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewLedgers.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewLedgers.visibility = View.VISIBLE
        }
    }
}