package com.example.poketmoney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: StatItemAdapter

    // 1. !!! 新增：用于防止重复检查的标志位 !!!
    /**
     * 这是一个标志位，用于确保 "首次启动" 检查 (observeLedgerCount)
     * 只在 Activity 创建时执行一次，
     * 防止在 (例如) 旋转屏幕后再次触发强制跳转。
     */
    private var hasCheckedFirstLaunch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // (无变化) 初始化柱状图样式
        setupBarChart()

        // (无变化) 初始化 Adapter
        adapter = StatItemAdapter()

        // (无变化) 设置 RecyclerView
        binding.recyclerViewStats.adapter = adapter
        binding.recyclerViewStats.layoutManager = LinearLayoutManager(this)


        // --- (!!! 关键修改：重构所有 Observers !!!) ---

        // 2. !!! 新增：调用新的 Observers 设置函数 !!!
        setupObservers()

        // --- (修改结束) ---


        // (无变化) 设置 Adapter 的点击事件
        adapter.onItemClick = { statItem ->
            val intent = Intent(this, TransactionListActivity::class.java).apply {
                putExtra("PAGE_TITLE", statItem.title)
                putExtra("START_DATE", statItem.startDate)
                putExtra("END_DATE", statItem.endDate)
            }
            startActivity(intent)
        }

        // (无变化) 设置菜单按钮
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // (无变化) 设置按钮点击事件
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

        // (无变化) 为根视图设置边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * 3. !!! 新增：设置所有 LiveData 观察者 !!!
     * 我们将所有 Observers 集中到这里
     */
    private fun setupObservers() {
        // (无变化) 观察统计项
        viewModel.statItems.observe(this) { statItems ->
            adapter.submitList(statItems)
        }

        // (无变化) 观察柱状图
        viewModel.barChartData.observe(this) { (barData, labels) ->
            updateBarChart(barData, labels)
        }

        // (无变化) 观察账本名称 (用于标题)
        //
        viewModel.activeLedgerName.observe(this) { ledgerName ->
            binding.tvTitleLedgerName.text = ledgerName ?: getString(R.string.app_name)
        }

        // 4. !!! 关键：观察 "账本总数" 以实现首次启动逻辑 !!!
        //
        viewModel.ledgerCount.observe(this) { count ->
            // (使用标志位防止重复执行)
            if (hasCheckedFirstLaunch) return@observe

            if (count == 0) {
                // --- 强制模式 (账本数量为 0) ---
                hasCheckedFirstLaunch = true // (标记为已检查)

                // 启动 LedgerManageActivity (账本管理页)
                val intent = Intent(this, LedgerManageActivity::class.java)
                // (传入 "强制模式" 标志)
                intent.putExtra("IS_FIRST_LAUNCH", true)
                startActivity(intent)

            } else if (count > 0) {
                // --- 正常模式 (账本已存在) ---
                hasCheckedFirstLaunch = true // (标记为已检查)

                // (调用我们在 MainViewModel 中创建的数据加载函数)
                viewModel.loadDataForActiveLedger()
            }
            // (如果 count 还在 null 的状态, 我们什么也不做，等待 LiveData 更新)
        }
    }

    /**
     * (!!! 已修改 !!!) onResume()
     */
    override fun onResume() {
        super.onResume()

        // (!!! 修改 !!!)
        // 只有当我们 *不是* 在 "首次启动" 流程中时
        // (即，我们已经有账本了)，
        // 我们才在 onResume 时刷新数据。
        if (hasCheckedFirstLaunch) {
            //
            viewModel.checkActiveLedger()
        }
    }

    // (无变化) 初始化柱状图样式
    private fun setupBarChart() {
        binding.chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawValueAboveBar(false)
            setDrawGridBackground(false)

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = ContextCompat.getColor(this@MainActivity, R.color.white)

            axisLeft.setDrawGridLines(true)
            axisLeft.textColor = ContextCompat.getColor(this@MainActivity, R.color.grey_400)
            axisLeft.axisMinimum = 0f

            axisRight.isEnabled = false

            isDoubleTapToZoomEnabled = false
            setPinchZoom(false)
        }
    }

    // (无变化) 更新柱状图数据
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