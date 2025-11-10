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
// 2. !!! 新增：导入 OnBackPressedCallback !!!
import androidx.activity.OnBackPressedCallback
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
<<<<<<< HEAD
 * 账本管理页面
 *
 * (!!! 已更新：支持 "首次启动" 强制模式 !!!)
=======
 * 账本管理页面 (已修复 Bug #2, 已新增 "强制创建" 逻辑)
>>>>>>> temp-branch
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

<<<<<<< HEAD
    // 1. !!! 新增：用于判断是否为 "强制模式" !!!
    private var isFirstLaunchMode = false
=======
    // 3. !!! 新增：用于 "强制创建" 模式的标志 !!!
    private var isFirstRun: Boolean = false
>>>>>>> temp-branch

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
        // (修正警告：使用 ContextCompat.getDrawable，并且 ID 是 android.R.drawable.ic_menu_delete)
        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!

<<<<<<< HEAD
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
=======
        // 4. !!! 新增：检查 Intent 标志 !!!
        isFirstRun = intent.getBooleanExtra("IS_FIRST_RUN", false)

        // (无变化)
        binding.btnBack.setOnClickListener {
            finish()
>>>>>>> temp-branch
        }

        // (无变化) "新增账本"
        binding.fabAddLedger.setOnClickListener {
            showLedgerDialog(null)
        }

        // (无变化)
        setupRecyclerView()
        setupSwipeToDelete()
        observeLedgers()

        // 5. !!! 新增：处理 "强制创建" 模式的 UI !!!
        if (isFirstRun) {
            // 隐藏返回按钮
            binding.btnBack.visibility = View.GONE

            // 立即弹出创建对话框
            showLedgerDialog(null)

            // (重要) 拦截物理返回键
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 如果是 "首次运行" 模式，禁止用户通过返回键退出
                    // (什么也不做)
                }
            })
        }


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
<<<<<<< HEAD
     * (!!! 修改 !!!)
     * 初始化 RecyclerView 和 Adapter
=======
     * 初始化 RecyclerView 和 Adapter (!!! 已修改 !!!)
>>>>>>> temp-branch
     */
    private fun setupRecyclerView() {
        adapter = LedgerAdapter()
        binding.recyclerViewLedgers.adapter = adapter
        binding.recyclerViewLedgers.layoutManager = LinearLayoutManager(this)

<<<<<<< HEAD
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
=======
        // 2. !!! 交互 1: (单击列表项) -> 切换账本 (已修改) !!!
        adapter.onItemClick = { ledger ->
            // 2.1. 更新全局 "激活" 账本 ID (无变化)
            LedgerManager.setActiveLedgerId(this, ledger.id)

            // 2.2. (!!! 修改 !!!)
            // 无论是 "首次运行" 还是 "正常切换"，我们都希望返回 MainActivity

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
>>>>>>> temp-branch
        }

        // 交互 2: (单击 "笔" 图标) -> 编辑账本 (!!! 错误修复 !!!)
        adapter.onEditClick = editClick@{ ledger ->
            // (在 "首次运行" 模式下，用户不应该能点到这个，但为了安全起见)
            // (!!! 修复：使用 return@editClick 标签 !!!)
            if (isFirstRun) return@editClick

            showLedgerDialog(ledger)
        }
    }

    /**
<<<<<<< HEAD
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
=======
     * (!!! 已修改：增加 isFirstRun 检查 !!!)
     */
>>>>>>> temp-branch
    private fun setupSwipeToDelete() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // (!!! 新增检查 !!!)
                if (isFirstRun) {
                    // 强制创建模式下，不允许删除
                    adapter.notifyItemChanged(position)
                    return
                }

                val ledgerToDelete = adapter.currentList[position]
                showDeleteConfirmationDialog(ledgerToDelete, position)
            }
<<<<<<< HEAD
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
=======

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
                // (!!! 新增检查 !!!)
                if (isFirstRun) {
                    // 强制创建模式下，不允许滑动
                    super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, false)
                    return
                }

>>>>>>> temp-branch
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
<<<<<<< HEAD
     * (!!! 已修改 !!!)
     * 修复了 'spinnerCurrency' 的 Bug
     *
=======
     * (!!! 已修改：处理 isFirstRun 模式下的 UI 和逻辑 !!!)
>>>>>>> temp-branch
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
<<<<<<< HEAD
            dialogBinding.tvDialogTitle.text = getString(R.string.edit_ledger_dialog_title) //
            dialogBinding.etLedgerName.setText(ledger?.name)
=======
            dialogBinding.tvDialogTitle.text = getString(R.string.edit_ledger_dialog_title)
            // (!!! 修复警告：移除不必要的 '?.' !!!)
            dialogBinding.etLedgerName.setText(ledger.name)
>>>>>>> temp-branch
        } else {
            dialogBinding.tvDialogTitle.text = getString(R.string.add_ledger_dialog_title) //
        }

        // (!!! 新增：处理 isFirstRun !!!)
        if (isFirstRun) {
            // 隐藏 "取消" 按钮
            dialogBinding.btnDialogCancel.visibility = View.GONE
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
<<<<<<< HEAD
            // (!!! 新增 !!!) "强制模式" 下，禁止点击外部取消
            .setCancelable(!isFirstLaunchMode)
=======
            // (!!! 修改：isFirstRun 模式下不可取消 !!!)
            .setCancelable(if (isFirstRun) false else true)
>>>>>>> temp-branch
            .show()

        dialogBinding.btnDialogSave.setOnClickListener {
            val newName = dialogBinding.etLedgerName.text.toString().trim()

            lifecycleScope.launch {
                val validationError = validateLedgerName(newName, ledger?.id)

                if (validationError == null) {
                    if (isEditMode) {
<<<<<<< HEAD
                        ledgerDao.update(ledger!!.copy(name = newName)) //
                    } else {
                        ledgerDao.insert(Ledger(name = newName)) //
=======
                        // (正常编辑模式)
                        // (!!! 修复警告：移除不必要的 '!!' !!!)
                        ledgerDao.update(ledger.copy(name = newName))
                    } else {
                        // (新增模式)
                        val newLedger = Ledger(name = newName)
                        // (!!! 修正：使用 LedgerDao.insert 返回的 ID !!!)
                        val newLedgerId = ledgerDao.insert(newLedger)

                        // (!!! 新增：如果是首次运行，自动激活并返回 !!!)
                        if (isFirstRun) {
                            // 1. 将新创建的账本设为 "激活" 账本
                            // (注意：Room insert 返回的是 rowId，在自增主键下就是新 ID)
                            LedgerManager.setActiveLedgerId(this@LedgerManageActivity, newLedgerId)

                            // 2. 关闭当前 Activity，返回 MainActivity
                            finish()
                        }
>>>>>>> temp-branch
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@LedgerManageActivity, validationError, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogBinding.btnDialogCancel.setOnClickListener {
            // (isFirstRun 模式下此按钮不可见，所以无需检查)
            dialog.dismiss()
        }
    }

    /**
     * (!!! 已修改：优化逻辑以移除 "Redundant if" 警告 !!!)
     */
    private suspend fun validateLedgerName(newName: String, currentId: Long?): String? {
        if (newName.isEmpty()) {
            return getString(R.string.error_ledger_name_empty) //
        }
<<<<<<< HEAD
        val existingLedger = ledgerDao.findLedgerByName(newName) //
        if (existingLedger != null) {
            if (currentId == null) {
                return getString(R.string.error_ledger_name_exists) //
            } else {
                if (existingLedger.id != currentId) {
                    return getString(R.string.error_ledger_name_exists) //
                }
            }
=======

        val existingLedger = ledgerDao.findLedgerByName(newName)

        // (!!! 优化后的逻辑 !!!)
        // 检查：
        // 1. 账本是否已存在
        // 2. 并且 ( (这是 "新增" 模式) OR (这是 "编辑" 模式且 ID 不匹配) )
        if (existingLedger != null && (currentId == null || existingLedger.id != currentId)) {
            return getString(R.string.error_ledger_name_exists)
>>>>>>> temp-branch
        }
        return null
    }

    // (!!! 已修改 !!!)
    private fun showDeleteConfirmationDialog(ledgerToDelete: Ledger, position: Int) {

<<<<<<< HEAD
        // (!!! 关键：确保 "强制模式" 下不能删除 !!!)
        // (你的需求是 "不允许账本全部删除")
=======
        // (此函数在 isFirstRun=true 时不会被调用，无需改动)

>>>>>>> temp-branch
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
<<<<<<< HEAD
            .setTitle(R.string.delete_ledger_dialog_title) //
=======
            .setTitle(R.string.delete_ledger_dialog_title) // (!!! 已修复：使用了正确的 Title !!!)
>>>>>>> temp-branch
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
<<<<<<< HEAD
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

=======
     * (!!! 已修改：增加 isFirstRun 检查 !!!)
     */
    private fun checkEmptyState(ledgers: List<Ledger>) {
        // (!!! 新增：在 "首次运行" 模式下，我们 *不* 显示空状态，而是等待对话框)
        if (isFirstRun) {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewLedgers.visibility = View.GONE
            return
        }

        if (ledgers.isEmpty()) {
>>>>>>> temp-branch
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewLedgers.visibility = View.GONE
        } else {
            // 列表不为空
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewLedgers.visibility = View.VISIBLE
        }
    }
}