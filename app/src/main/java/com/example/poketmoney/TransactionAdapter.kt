package com.example.poketmoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemTransactionBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    // 日期格式化
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // 金额格式化
    private val decimalFormat = DecimalFormat("0.00")

    // (为步骤 3 准备) 点击事件回调
    var onItemClick: ((Transaction) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                // 1. 设置类别和图标
                val categoryName = transaction.category ?: "其他"
                tvCategoryName.text = categoryName
                tvCategoryIcon.text = categoryName.firstOrNull()?.toString() ?: "?"

                // 2. 设置备注 (如果备注不为空则显示)
                if (transaction.note.isNullOrEmpty()) {
                    tvNote.visibility = View.GONE
                } else {
                    tvNote.text = transaction.note
                    tvNote.visibility = View.VISIBLE
                }

                // 3. 设置日期
                tvDate.text = dateFormat.format(Date(transaction.date))

                // 4. 设置金额和颜色
                when (transaction.type) {
                    "income" -> {
                        tvAmount.text = "+${decimalFormat.format(transaction.amount)}"
                        tvAmount.setTextColor(itemView.context.getColor(R.color.income_color))
                    }
                    "expense" -> {
                        tvAmount.text = "-${decimalFormat.format(transaction.amount)}"
                        tvAmount.setTextColor(itemView.context.getColor(R.color.expense_color))
                    }
                    else -> {
                        tvAmount.text = decimalFormat.format(transaction.amount)
                        tvAmount.setTextColor(itemView.context.getColor(R.color.balance_color))
                    }
                }

                // (为步骤 3 准备) 设置点击事件
                itemView.setOnClickListener {
                    onItemClick?.invoke(transaction)
                }
            }
        }
    }
}

// DiffUtil 用于 ListAdapter 高效刷新列表
class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}
