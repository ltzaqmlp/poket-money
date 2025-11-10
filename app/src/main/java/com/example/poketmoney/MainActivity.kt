package com.example.poketmoney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.poketmoney.databinding.ActivityMainBinding
// 导入柱状图（BarChart）相关的类
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
// !!! 新增：导入 DecimalFormat 用于格式化金额 !!!
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    // --- !!! 移除：不再需要 Adapter !!! ---
    // private lateinit var adapter: StatItemAdapter

    // !!! 新增：用于格式化金额的 Helper !!!
    private val decimalFormat = DecimalFormat("0.00")

    // (无变化)
    private var hasCheckedFirstLaunch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // (无变化)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // (!!! 修改：调用 setupBarChart (现在内部已修改) !!!)
        setupBarChart()

<<<<<<< HEAD
        // (无变化)
        adapter = StatItemAdapter()

        // (无变化)
        binding.recyclerViewStats.adapter = adapter
        binding.recyclerViewStats.layoutManager = LinearLayoutManager(this)


        // (无变化)
        setupObservers()


        // (无变化)
=======
        // --- !!! 移除：不再需要 Adapter 和 RecyclerView 设置 !!! ---
        // adapter = StatItemAdapter()
        // binding.recyclerViewStats.adapter = adapter
        // binding.recyclerViewStats.layoutManager = LinearLayoutManager(this)


        // --- (!!! 关键修改：观察 LiveData !!!) ---

        // (!!! 已修改：观察统计项数据 !!!)
        viewModel.statItems.observe(this) { statItems ->
            // (不再提交给 Adapter)
            // adapter.submitList(statItems)

            // !!! 新增：手动更新 5 个视图 !!!
            // (安全检查：statItems 在没有账本时可能为空)
            if (!statItems.isNullOrEmpty()) {
                updateStatViews(statItems)
            }
        }

        // (无变化) 观察 ViewModel 的柱状图数据
        viewModel.barChartData.observe(this) { (barData, labels) ->
            updateBarChart(barData, labels)
        }

        // (无变化) 观察 "当前账本名称"
        viewModel.activeLedgerName.observe(this) { ledgerName ->
            binding.tvTitleLedgerName.text = ledgerName ?: getString(R.string.app_name)
        }

        // (!!! 2. 新增：观察 "跳转到创建账本" 信号 !!!)
        viewModel.navigateToCreateLedger.observe(this) { shouldNavigate ->
            if (shouldNavigate == true) {
                // 启动账本管理页，并附带 "强制创建" 标志
                val intent = Intent(this, LedgerManageActivity::class.java).apply {
                    // 传入 "IS_FIRST_RUN" 标志
                    putExtra("IS_FIRST_RUN", true)
                }
                startActivity(intent)

                // (重要) 重置信号，这样当用户返回时不会再次触发
                viewModel.doneNavigatingToCreateLedger()
            }
        }
        // --- (修改结束) ---


        // --- !!! 移除：Adapter 的点击事件 !!! ---
        // (点击事件将在 updateStatViews() 中单独设置)
        /*
>>>>>>> temp-branch
        adapter.onItemClick = { statItem ->
            val intent = Intent(this, TransactionListActivity::class.java).apply {
                putExtra("PAGE_TITLE", statItem.title)
                putExtra("START_DATE", statItem.startDate)
                putExtra("END_DATE", statItem.endDate)
            }
            startActivity(intent)
        }
        */

        // (无变化)
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // (无变化)
        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java).apply {
                putExtra("IS_INCOME", true)
            }
            startActivity(intent)
        }

        // (无变化)
        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java).apply {
                putExtra("IS_INCOME", false)
            }
            startActivity(intent)
        }

        // (无变化)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
<<<<<<< HEAD
     * (无变化)
     */
    private fun setupObservers() {
        // (无变化)
        viewModel.statItems.observe(this) { statItems ->
            adapter.submitList(statItems)
        }

        // (无变化)
        viewModel.barChartData.observe(this) { (barData, labels) ->
            updateBarChart(barData, labels)
        }

        // (无变化)
        viewModel.activeLedgerName.observe(this) { ledgerName ->
            binding.tvTitleLedgerName.text = ledgerName ?: getString(R.string.app_name)
        }

        // (无变化)
        viewModel.ledgerCount.observe(this) { count ->
            if (hasCheckedFirstLaunch) return@observe

            if (count == 0) {
                hasCheckedFirstLaunch = true
                val intent = Intent(this, LedgerManageActivity::class.java)
                intent.putExtra("IS_FIRST_LAUNCH", true)
                startActivity(intent)

            } else if (count > 0) {
                hasCheckedFirstLaunch = true
                viewModel.loadDataForActiveLedger()
            }
        }
    }

    /**
     * (无变化)
     */
    override fun onResume() {
        super.onResume()
        if (hasCheckedFirstLaunch) {
            viewModel.checkActiveLedger()
        }
    }

    // (!!! 2. 关键修改：setupBarChart() !!!)
=======
     * (!!! 新增函数 !!!)
     * 手动将 ViewModel 中的 List<StatItem> 数据填充到 5 个 <include> 布局中。
     */
    private fun updateStatViews(items: List<StatItem>) {
        // 安全检查：确保我们有 5 个条目
        if (items.size < 5) return

        // 1. 绑定 "今天"
        bindStatItem(
            // 我们通过 binding.statToday.root 访问 <include> 布局的根
            binding.statToday.root,
            // (我们从 item_stat.xml 得知内部 ID)
            binding.statToday.tvIcon,
            binding.statToday.tvTitle,
            binding.statToday.tvSubtitle,
            binding.statToday.tvIncome,
            binding.statToday.tvExpense,
            binding.statToday.tvBalance,
            items[0] // "今天" 的数据
        )

        // 2. 绑定 "本周"
        bindStatItem(
            binding.statWeek.root,
            binding.statWeek.tvIcon,
            binding.statWeek.tvTitle,
            binding.statWeek.tvSubtitle,
            binding.statWeek.tvIncome,
            binding.statWeek.tvExpense,
            binding.statWeek.tvBalance,
            items[1] // "本周" 的数据
        )

        // 3. 绑定 "本月"
        bindStatItem(
            binding.statMonth.root,
            binding.statMonth.tvIcon,
            binding.statMonth.tvTitle,
            binding.statMonth.tvSubtitle,
            binding.statMonth.tvIncome,
            binding.statMonth.tvExpense,
            binding.statMonth.tvBalance,
            items[2] // "本月" 的数据
        )

        // 4. 绑定 "今年"
        bindStatItem(
            binding.statYear.root,
            binding.statYear.tvIcon,
            binding.statYear.tvTitle,
            binding.statYear.tvSubtitle,
            binding.statYear.tvIncome,
            binding.statYear.tvExpense,
            binding.statYear.tvBalance,
            items[3] // "今年" 的数据
        )

        // 5. 绑定 "总计"
        bindStatItem(
            binding.statTotal.root,
            binding.statTotal.tvIcon,
            binding.statTotal.tvTitle,
            binding.statTotal.tvSubtitle,
            binding.statTotal.tvIncome,
            binding.statTotal.tvExpense,
            binding.statTotal.tvBalance,
            items[4] // "总计" 的数据
        )
    }

    /**
     * (!!! 新增函数 !!!)
     * 这是一个辅助函数，用于将单个 StatItem 绑定到 item_stat.xml 的视图上
     * 并设置点击事件。
     */
    private fun bindStatItem(
        rootView: View,
        icon: TextView,
        title: TextView,
        subtitle: TextView,
        income: TextView,
        expense: TextView,
        balance: TextView,
        item: StatItem
    ) {
        // (逻辑与 StatItemAdapter.kt 中的 bind() 几乎一致)
        title.text = item.title
        subtitle.text = item.subtitle
        income.text = "+${decimalFormat.format(item.income)}"
        expense.text = "-${decimalFormat.format(item.expense)}"
        balance.text = decimalFormat.format(item.balance)

        // 设置颜色
        income.setTextColor(ContextCompat.getColor(this, R.color.income_color))
        expense.setTextColor(ContextCompat.getColor(this, R.color.expense_color))
        balance.setTextColor(ContextCompat.getColor(this, R.color.balance_color))

        // (我们需要根据标题来区分图标)
        when (item.title) {
            "今天" -> icon.text = "1"
            "本周" -> icon.text = "7"
            "本月" -> icon.text = "31"
            "今年" -> icon.text = "365"
            "总计" -> icon.text = "Σ"
        }

        // !!! 新增：为每个根视图 (rootView) 设置点击事件 !!!
        rootView.setOnClickListener {
            val intent = Intent(this, TransactionListActivity::class.java).apply {
                putExtra("PAGE_TITLE", item.title)
                putExtra("START_DATE", item.startDate)
                putExtra("END_DATE", item.endDate)
            }
            startActivity(intent)
        }
    }


    /**
     * (!!! 3. 关键修改：onResume() !!!)
     */
    override fun onResume() {
        super.onResume()
        // (旧: viewModel.checkActiveLedger())
        // 新: 调用 ViewModel 的新版数据刷新/检查函数
        // 1. 如果没有账本，它会触发导航
        // 2. 如果有账本 (或刚创建完返回)，它会加载数据
        viewModel.refreshData()
    }

    /**
     * (无变化) 初始化柱状图样式
     */
>>>>>>> temp-branch
    private fun setupBarChart() {
        binding.chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            legend.textColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            setDrawValueAboveBar(false)
            setDrawGridBackground(false)

            // (!!! 修改：使用动态颜色 !!!)
            val primaryTextColor = ContextCompat.getColor(this@MainActivity, R.color.text_primary_color)
            val secondaryTextColor = ContextCompat.getColor(this@MainActivity, R.color.text_secondary_color)

            legend.textColor = primaryTextColor // (新增)

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = primaryTextColor // (修改)

            axisLeft.setDrawGridLines(true)
            axisLeft.textColor = secondaryTextColor // (修改)
            axisLeft.axisMinimum = 0f

            axisRight.isEnabled = false

            isDoubleTapToZoomEnabled = false
            setPinchZoom(false)
        }
    }

<<<<<<< HEAD
    // (无变化)
=======
    /**
     * (无变化) 更新柱状图数据
     */
>>>>>>> temp-branch
    private fun updateBarChart(data: BarData?, labels: List<String>?) {
        if (data == null || labels == null) {
            binding.chart.clear()
            binding.chart.invalidate()
            return
        }

        binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chart.xAxis.labelCount = labels.size

        val groupSpace = 0.1f
        val barSpace = 0.05f
        data.barWidth = 0.4f

        binding.chart.data = data
        binding.chart.groupBars(0f, groupSpace, barSpace)
        binding.chart.invalidate()
    }
}