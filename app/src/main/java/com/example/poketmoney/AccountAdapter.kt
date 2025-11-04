package com.example.poketmoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemAccountBinding
import java.text.DecimalFormat

/**
 * AccountAdapter (账户适配器)
 *
 * (!!! 已更新：支持 "编辑" 按钮点击 !!!)
 *
 * 负责在 AccountManageActivity 的 RecyclerView 中显示账户列表。
 * 它使用 ListAdapter 以实现高效的列表刷新。
 */
class AccountAdapter :
    ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    /**
     * (单击) 点击事件回调。
     * (为未来做准备) 当用户点击列表项主体时触发。
     */
    var onItemClick: ((Account) -> Unit)? = null

    /**
     * (!!! 新增 !!!) "编辑" 点击事件回调。
     * 当用户点击 "笔" 图标时触发。
     */
    var onEditClick: ((Account) -> Unit)? = null

    // 用于格式化余额 (例如: 1000.00)
    private val decimalFormat = DecimalFormat("0.00")

    /**
     * 创建 ViewHolder (视图持有者)。
     * 当 RecyclerView 需要一个新的 item_account 视图时调用。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding =
            ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    /**
     * 绑定 ViewHolder。
     * 当 RecyclerView 需要在特定位置显示数据时调用。
     */
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val accountItem = getItem(position)
        holder.bind(accountItem)
    }

    /**
     * ViewHolder 类，用于持有 item_account.xml 布局中的视图引用。
     */
    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * 将数据（Account 对象）绑定到视图上。
         *
         * @param account 账户数据对象。
         */
        fun bind(account: Account) {
            binding.apply {
                // 1. 设置账户类别 (无变化)
                tvAccountType.text = account.type

                // 2. 设置余额 (无变化)
                tvAccountBalance.text = decimalFormat.format(account.balance)

                // 3. 设置币种 (无变化)
                tvAccountCurrency.text = account.currency

                // 4. 设置备注 (无变化)
                if (account.note.isNullOrEmpty()) {
                    tvAccountNote.visibility = View.GONE
                } else {
                    tvAccountNote.text = account.note
                    tvAccountNote.visibility = View.VISIBLE
                }

                // 5. (单击) 点击事件
                itemView.setOnClickListener {
                    onItemClick?.invoke(account)
                }

                // 6. (!!! 新增 !!!) "编辑" 按钮点击事件
                //    (绑定到 btnEditAccount)
                btnEditAccount.setOnClickListener {
                    onEditClick?.invoke(account)
                }
            }
        }
    }
}

/**
 * AccountDiffCallback (无变化)
 * 告诉 ListAdapter 如何比较两个 Account (账户) 对象。
 */
class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
    /**
     * 检查两个 item 是否是同一个对象（比较 ID）。
     */
    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * 检查两个 item 的内容是否相同。
     */
    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
        return oldItem == newItem
    }
}