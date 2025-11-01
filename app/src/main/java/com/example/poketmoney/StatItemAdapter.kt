package com.example.poketmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemStatBinding
import java.text.DecimalFormat

// 步骤 4 修改：继承自 ListAdapter 而不是 RecyclerView.Adapter
class StatItemAdapter :
    ListAdapter<StatItem, StatItemAdapter.StatItemViewHolder>(StatItemDiffCallback()) {

    // private val _items = mutableListOf<StatItem>() // 步骤 4 移除：ListAdapter 会管理列表
    private val decimalFormat = DecimalFormat("0.00")

    // (步骤 3 已添加) 点击回调
    var onItemClick: ((StatItem) -> Unit)? = null

    /* 步骤 4 移除：不再需要 updateItems
    fun updateItems(newItems: List<StatItem>) {
        _items.clear()
        _items.addAll(newItems)
        notifyDataSetChanged()
    }
    */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatItemViewHolder {
        val binding = ItemStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatItemViewHolder, position: Int) {
        // 步骤 4 修改：使用 ListAdapter 的 getItem 方法
        holder.bind(getItem(position))
    }

    // 步骤 4 移除：ListAdapter 会自动处理
    // override fun getItemCount(): Int = _items.size

    inner class StatItemViewHolder(private val binding: ItemStatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StatItem) {
            binding.apply {
                tvTitle.text = item.title
                tvSubtitle.text = item.subtitle
                tvIncome.text = "+${decimalFormat.format(item.income)}"
                tvExpense.text = "-${decimalFormat.format(item.expense)}"
                tvBalance.text = decimalFormat.format(item.balance)

                // 设置颜色
                tvIncome.setTextColor(itemView.context.getColor(R.color.income_color))
                tvExpense.setTextColor(itemView.context.getColor(R.color.expense_color))
                tvBalance.setTextColor(itemView.context.getColor(R.color.balance_color))

                // (步骤 3 已添加) 点击事件
                itemView.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }
}

// 步骤 4 新增：DiffUtil.ItemCallback
// 它告诉 ListAdapter 如何比较两个 StatItem 对象
class StatItemDiffCallback : DiffUtil.ItemCallback<StatItem>() {
    override fun areItemsTheSame(oldItem: StatItem, newItem: StatItem): Boolean {
        // 通常我们比较唯一的 ID，但这里 "title" (今天, 本周) 是唯一的
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: StatItem, newItem: StatItem): Boolean {
        // 检查所有内容是否相同 (Kotlin data class 会自动实现 ==)
        return oldItem == newItem
    }
}