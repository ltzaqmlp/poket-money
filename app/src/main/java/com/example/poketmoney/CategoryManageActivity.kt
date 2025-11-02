package com.example.poketmoney

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ActivityCategoryManageBinding
import com.example.poketmoney.databinding.DialogAddCategoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoryManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryManageBinding
    private lateinit var adapter: CategoryAdapter

    // 用于存储当前列表的类别数据
    private val categoryList = mutableListOf<String>()

    // 标记是 "income" (收入) 还是 "expense" (支出)
    private var categoryType: String = "income"

    // 用于滑动删除的背景和图标
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 启用边缘到边缘显示
        binding = ActivityCategoryManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 获取从 SettingsActivity 传来的类型
        // 默认为 "income"
        categoryType = intent.getStringExtra("CATEGORY_TYPE") ?: "income"

        // 2. 初始化滑动删除的图标和背景
        swipeBackground = ColorDrawable(Color.RED)
        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!

        // 3. 设置顶部栏
        setupTopBar()

        // 4. 设置 RecyclerView
        setupRecyclerView()

        // 5. 设置 FAB (浮动操作按钮) 的点击事件
        binding.fabAddCategory.setOnClickListener {
            // 调用弹窗函数，传入 null 表示是 "新增模式"
            showCategoryDialog(null, -1)
        }

        // 6. 加载数据
        loadCategories()

        // 7. 设置滑动删除
        setupSwipeToDelete()

        // 8. (为保持一致性) 处理窗口边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * 设置顶部栏的标题和返回按钮
     */
    private fun setupTopBar() {
        // 根据是 "income" 还是 "expense" 设置不同的标题
        binding.tvTitle.text = if (categoryType == "income") {
            getString(R.string.category_manage_title_income)
        } else {
            getString(R.string.category_manage_title_expense)
        }

        // 设置返回按钮
        binding.btnBack.setOnClickListener {
            finish() // 关闭当前页面
        }
    }

    /**
     * 初始化 RecyclerView 和 Adapter
     */
    private fun setupRecyclerView() {
        adapter = CategoryAdapter()
        binding.recyclerViewCategories.adapter = adapter
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(this)

        // 设置列表项的点击事件 (用于"修改")
        adapter.onItemClick = { position, categoryName ->
            // 调用弹窗函数，传入类别名称和位置，表示是 "修改模式"
            showCategoryDialog(categoryName, position)
        }
    }

    /**
     * 从 CategoryManager 加载类别数据
     */
    private fun loadCategories() {
        // 1. 根据类型从 CategoryManager 获取数据
        val loadedList = if (categoryType == "income") {
            CategoryManager.getIncomeCategories(this)
        } else {
            CategoryManager.getExpenseCategories(this)
        }

        // 2. 更新本地列表
        categoryList.clear()
        categoryList.addAll(loadedList)

        // 3. 将新列表提交给 Adapter (ListAdapter 会自动计算差异并刷新)
        adapter.submitList(categoryList.toList()) // 提交一个不可变副本

        // 4. 检查是否显示"空状态"
        checkEmptyState()
    }

    /**
     * 保存当前列表数据到 CategoryManager
     */
    private fun saveCategories() {
        // 根据类型保存到 CategoryManager
        if (categoryType == "income") {
            CategoryManager.saveIncomeCategories(this, categoryList)
        } else {
            CategoryManager.saveExpenseCategories(this, categoryList)
        }
        // 重新加载数据以确保排序和一致性
        loadCategories()
    }

    /**
     * 检查列表是否为空，并切换提示视图
     */
    private fun checkEmptyState() {
        if (categoryList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewCategories.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewCategories.visibility = View.VISIBLE
        }
    }

    /**
     * 显示“新增”或“修改”类别的对话框
     * @param categoryName 如果为 null，则是 "新增模式"；否则为 "修改模式"
     * @param position 被修改项的索引；"新增模式" 时为 -1
     */
    private fun showCategoryDialog(categoryName: String?, position: Int) {
        // 1. 确定模式 (新增/修改)
        val isEditMode = categoryName != null

        // 2. 加载弹窗布局
        val dialogBinding = DialogAddCategoryBinding.inflate(LayoutInflater.from(this))

        // 3. 设置弹窗标题
        dialogBinding.tvDialogTitle.text = if (isEditMode) {
            getString(R.string.edit_category_dialog_title)
        } else {
            getString(R.string.add_category_dialog_title)
        }

        // 4. 如果是 "修改模式"，在输入框中预填入旧名称
        if (isEditMode) {
            dialogBinding.etCategoryName.setText(categoryName)
        }

        // 5. 创建并显示 AlertDialog
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false) // 点击外部不关闭
            .show()

        // 6. 设置“保存”按钮的点击事件
        dialogBinding.btnDialogSave.setOnClickListener {
            val newName = dialogBinding.etCategoryName.text.toString().trim()

            // 验证输入
            if (validateInput(newName, categoryName)) {
                if (isEditMode) {
                    // --- 修改模式 ---
                    categoryList[position] = newName
                } else {
                    // --- 新增模式 ---
                    categoryList.add(newName)
                }

                // 保存数据
                saveCategories()
                dialog.dismiss()
            }
        }

        // 7. 设置“取消”按钮的点击事件
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    /**
     * 验证用户输入的类别名称
     * @param newName 用户输入的新名称
     * @param oldName "修改模式" 下的旧名称，"新增模式" 时为 null
     * @return true 如果验证通过，false 否则
     */
    private fun validateInput(newName: String, oldName: String?): Boolean {
        // 规则 1：不能为空
        if (newName.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_category_name_empty), Toast.LENGTH_SHORT).show()
            return false
        }

        // 规则 2：不能重复
        // (如果是在 "修改模式" 下，且新旧名称相同，则不算重复)
        if (newName != oldName && categoryList.any { it.equals(newName, ignoreCase = true) }) {
            Toast.makeText(this, getString(R.string.error_category_name_exists), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * 设置 RecyclerView 的滑动删除功能
     */
    private fun setupSwipeToDelete() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0, // 0 表示不支持拖拽 (drag)
            ItemTouchHelper.LEFT // 只支持向左滑动
        ) {
            // onMove 用于拖拽排序，我们暂时不需要
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // onSwiped 在用户滑动列表项时触发
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val categoryToDelete = adapter.currentList[position]

                // 显示删除确认对话框
                showDeleteConfirmationDialog(categoryToDelete, position)
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
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewCategories)
    }

    /**
     * 显示“删除”二次确认对话框
     * @param categoryName 要删除的类别名称
     * @param position 要删除的类别索引
     */
    private fun showDeleteConfirmationDialog(categoryName: String, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_category_dialog_title)
            // 格式化字符串，将 %s 替换为类别名称
            .setMessage(getString(R.string.delete_category_dialog_message, categoryName))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                // 如果用户点击 "取消"，滑动操作被撤销
                // 我们必须通知 Adapter 刷新这一行，使其滑回去
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                // 如果用户点击 "删除"，则从数据列表中移除
                categoryList.removeAt(position)
                // 保存更改
                saveCategories()
                dialog.dismiss()
            }
            .setOnCancelListener {
                // 如果用户点击对话框外部（如果 enabled）
                // 同样需要撤销滑动
                adapter.notifyItemChanged(position)
            }
            .show()
    }
}