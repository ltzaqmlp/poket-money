package com.example.poketmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poketmoney.databinding.ItemCategoryBinding

/**
 * 这是用于类别管理页面的 RecyclerView 适配器。
 * 它使用 ListAdapter，能高效地处理列表数据的变化。
 */
class CategoryAdapter :
    ListAdapter<String, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    /**
     * 点击事件回调 (lambda)。
     * 当用户点击列表中的某一项时，这个回调将被触发。
     * 它传递两个参数：
     * 1. position: 被点击项的索引 (Int)
     * 2. categoryName: 被点击项的类别名称 (String)
     * (我们需要索引来知道要更新哪一项)
     */
    var onItemClick: ((position: Int, categoryName: String) -> Unit)? = null

    /**
     * 创建 ViewHolder。
     * 当 RecyclerView 需要一个新的列表项视图时调用。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // 从 XML 布局文件 'item_category.xml' 创建视图绑定
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    /**
     * 绑定 ViewHolder。
     * 当 RecyclerView 需要在特定位置显示数据时调用。
     */
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // 获取当前位置的数据
        val categoryName = getItem(position)
        // 将数据绑定到 ViewHolder
        holder.bind(categoryName, position)
    }

    /**
     * ViewHolder 类，用于持有列表项的视图引用。
     */
    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * 将数据（类别名称）绑定到视图上，并设置点击事件。
         */
        fun bind(categoryName: String, position: Int) {
            // 1. 将类别名称设置到 TextView 上
            binding.tvCategoryName.text = categoryName

            // 2. 设置整个列表项的点击事件
            itemView.setOnClickListener {
                // 触发在 Activity 中定义的 onItemClick 回调
                onItemClick?.invoke(position, categoryName)
            }
        }
    }
}

/**
 * DiffUtil.ItemCallback
 * 它告诉 ListAdapter 如何比较两个类别字符串，以确定列表是否需要刷新以及如何刷新。
 * 这比 notifyDataSetChanged() 高效得多。
 */
class CategoryDiffCallback : DiffUtil.ItemCallback<String>() {
    /**
     * 检查两个 item 是否是同一个对象（例如，比较 ID）。
     * 在这里，字符串本身就是数据，但如果它们在列表中的位置不变，我们认为它们是同一个 item。
     * （在我们的简单字符串列表中，这个和 areContentsTheSame 效果类似）
     */
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    /**
     * 检查两个 item 的内容是否相同。
     * 如果内容不同（例如，字符串从 "工资" 变为 "薪水"），RecyclerView 会刷新这个视图。
     */
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}