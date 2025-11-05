package com.example.poketmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemStatBinding
import java.text.DecimalFormat

// (无变化)
class StatItemAdapter :
    ListAdapter<StatItem, StatItemAdapter.StatItemViewHolder>(StatItemDiffCallback()) {

    // (无变化)
    private val decimalFormat = DecimalFormat("0.00")
    var onItemClick: ((StatItem) -> Unit)? = null

    // (无变化)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatItemViewHolder {
        val binding = ItemStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatItemViewHolder(binding)
    }

    // (无变化)
    override fun onBindViewHolder(holder: StatItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StatItemViewHolder(private val binding: ItemStatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * !!! 1. 关键修改：bind() !!!
         */
        fun bind(item: StatItem) {
            binding.apply {
                // !!! 修改：设置图标文本 !!!
                // (旧: tvIcon.text = "1" -- 这是 item_stat.xml 中的 tools:text)
                // (新: 读取 item.iconText)
                tvIcon.text = item.iconText

                // (无变化)
                tvTitle.text = item.title
                tvSubtitle.text = item.subtitle
                tvIncome.text = "+${decimalFormat.format(item.income)}"
                tvExpense.text = "-${decimalFormat.format(item.expense)}"
                tvBalance.text = decimalFormat.format(item.balance)

                // (无变化)
                tvIncome.setTextColor(itemView.context.getColor(R.color.income_color))
                tvExpense.setTextColor(itemView.context.getColor(R.color.expense_color))
                tvBalance.setTextColor(itemView.context.getColor(R.color.balance_color))

                // (无变化)
                itemView.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }
}

// (无变化)
class StatItemDiffCallback : DiffUtil.ItemCallback<StatItem>() {
    override fun areItemsTheSame(oldItem: StatItem, newItem: StatItem): Boolean {
        // (!!! 修改：我们必须使用 StatItem 中唯一的字段 !!!)
        // (在 Step 1 中，我们将 iconText 添加到了 StatItem 的构造函数中)
        // (但 title 仍然是唯一的)
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: StatItem, newItem: StatItem): Boolean {
        // (无变化) data class 会比较所有字段，包括新的 iconText
        return oldItem == newItem
    }
}