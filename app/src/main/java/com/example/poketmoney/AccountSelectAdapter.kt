package com.example.poketmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemAccountSelectBinding
import java.text.DecimalFormat

/**
 * 用于 "选择账户" 对话框的 RecyclerView 适配器。
 * (此类为 "第 2 步" 新增)
 */
class AccountSelectAdapter :
    ListAdapter<Account, AccountSelectAdapter.AccountSelectViewHolder>(AccountDiffCallback()) {

    /**
     * 点击事件回调。
     * 当用户点击一个账户时触发。
     */
    var onItemClick: ((Account) -> Unit)? = null

    // 用于格式化余额
    private val decimalFormat = DecimalFormat("#,##0.00")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountSelectViewHolder {
        val binding =
            ItemAccountSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountSelectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountSelectViewHolder(private val binding: ItemAccountSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            binding.apply {
                // 1. 设置账户类别 (例如 "现金")
                tvAccountType.text = account.type

                // 2. 设置余额和币种 (例如 "1,000.00 CNY")
                tvAccountBalance.text = "${decimalFormat.format(account.balance)} ${account.currency}"

                // 3. 设置点击事件
                itemView.setOnClickListener {
                    onItemClick?.invoke(account)
                }
            }
        }
    }
}

// !!! 关键修复 !!!
//
// class AccountDiffCallback : DiffUtil.ItemCallback<Account>() { ... }
//
// (!!! 已删除此处的重复声明 !!!)
// 此 Adapter 将使用在 AccountAdapter.kt 中定义的 AccountDiffCallback。