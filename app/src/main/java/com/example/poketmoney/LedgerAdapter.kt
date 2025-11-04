package com.example.poketmoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemLedgerBinding

/**
 * LedgerAdapter (账本适配器) - (已更新)
 *
 * 负责在 LedgerManageActivity 的 RecyclerView 中显示账本列表。
 *
 * 新的交互逻辑：
 * 1. (单击 item) -> 切换账本 (onItemClick)
 * 2. (单击 笔) -> 修改账本 (onEditClick)
 * 3. (左滑) -> 删除账本 (在 Activity 中通过 ItemTouchHelper 实现)
 */
class LedgerAdapter :
    ListAdapter<Ledger, LedgerAdapter.LedgerViewHolder>(LedgerDiffCallback()) {

    // --- 事件回调 ---

    /**
     * (单击) 点击事件回调。
     * 当用户点击一个账本时 (非 "笔" 图标区域)，
     * 我们将触发这个回调来 "切换" 当前激活的账本。
     */
    var onItemClick: ((Ledger) -> Unit)? = null

    /**
     * (!!! 新增 !!!) 编辑点击事件回调。
     * 当用户点击该项右侧的 "笔" (btnEditLedger) 图标时，
     * 我们将触发这个回调来显示 "修改" 对话框。
     */
    var onEditClick: ((Ledger) -> Unit)? = null

    // (已移除) onIteLongClick (长按) 回调

    // (已移除) "当前激活账本" (currentActiveLedgerId) 相关的逻辑

    /**
     * 创建 ViewHolder (视图持有者)。
     * 当 RecyclerView 需要一个新的 item_ledger 视图时调用。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val binding =
            ItemLedgerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LedgerViewHolder(binding)
    }

    /**
     * 绑定 ViewHolder。
     * 当 RecyclerView 需要在特定位置显示数据时调用。
     */
    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val ledgerItem = getItem(position)
        // 将数据绑定到 ViewHolder
        holder.bind(ledgerItem)
    }

    /**
     * ViewHolder 类，用于持有 item_ledger.xml 布局中的视图引用。
     */
    inner class LedgerViewHolder(private val binding: ItemLedgerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * 将数据（Ledger 对象）绑定到视图上，并设置点击事件。
         *
         * @param ledger 账本数据对象。
         */
        fun bind(ledger: Ledger) {
            // 1. 设置账本名称
            binding.tvLedgerName.text = ledger.name

            // 2. (已移除) 激活状态 (ivLedgerActive) 的逻辑

            // 3. 设置 (单击) "切换账本" 事件
            //    (我们将其设置在 itemView，即整个列表项上)
            itemView.setOnClickListener {
                onItemClick?.invoke(ledger)
            }

            // 4. (!!! 新增 !!!) 设置 "编辑账本" 事件
            //    (我们将其只设置在 "笔" 图标上)
            binding.btnEditLedger.setOnClickListener {
                onEditClick?.invoke(ledger)
            }
        }
    }
}

/**
 * LedgerDiffCallback (无变化)
 * 告诉 ListAdapter 如何比较两个 Ledger (账本) 对象。
 */
class LedgerDiffCallback : DiffUtil.ItemCallback<Ledger>() {
    /**
     * 检查两个 item 是否是同一个对象（比较 ID）。
     */
    override fun areItemsTheSame(oldItem: Ledger, newItem: Ledger): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * 检查两个 item 的内容是否相同（比较 name）。
     */
    override fun areContentsTheSame(oldItem: Ledger, newItem: Ledger): Boolean {
        return oldItem == newItem
    }
}