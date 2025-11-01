package com.example.poketmoney

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.poketmoney.databinding.ActivityTransactionListBinding

class TransactionListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionListBinding
    private lateinit var viewModel: TransactionListViewModel
    private lateinit var adapter: TransactionAdapter

    // 从 Intent 获取的参数
    private var pageTitle: String? = null
    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 获取从 MainActivity 传来的数据
        pageTitle = intent.getStringExtra("PAGE_TITLE")
        startDate = intent.getLongExtra("START_DATE", 0)
        endDate = intent.getLongExtra("END_DATE", 0)

        // 2. 初始化 ViewModel
        viewModel = ViewModelProvider(this)[TransactionListViewModel::class.java]

        // 3. 设置顶部栏
        binding.tvTitle.text = pageTitle ?: getString(R.string.transaction_list_title)
        binding.btnBack.setOnClickListener {
            finish() // 点击返回按钮，关闭当前页面
        }

        // 4. 设置 RecyclerView 和 Adapter
        adapter = TransactionAdapter()
        binding.recyclerViewTransactions.adapter = adapter
        binding.recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        // 5. 观察 ViewModel 数据
        viewModel.transactions.observe(this) { transactions ->
            adapter.submitList(transactions) // ListAdapter 使用 submitList
            // 检查空状态
            if (transactions.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.recyclerViewTransactions.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.recyclerViewTransactions.visibility = View.VISIBLE
            }
        }

        // 6. 加载数据
        if (startDate != 0L && endDate != 0L) {
            viewModel.loadTransactions(startDate, endDate)
        }

        // 步骤 3 修改：设置列表项点击事件，跳转到编辑页
        adapter.onItemClick = { transaction ->
            val intent = Intent(this, AddRecordActivity::class.java)
            // 传递 TRANSACTION_ID，让 AddRecordActivity 知道是 "编辑模式"
            intent.putExtra("TRANSACTION_ID", transaction.id)
            startActivity(intent)
        }

        // 处理窗口边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // (为步骤 3 准备) 当从编辑页返回时，刷新列表
    override fun onResume() {
        super.onResume()
        // 确保在 onResume 时也重新加载数据，以便编辑/删除后能刷新
        if (startDate != 0L && endDate != 0L) {
            viewModel.loadTransactions(startDate, endDate)
        }
    }
}

