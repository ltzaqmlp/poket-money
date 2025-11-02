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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 启用边缘到边缘显示
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 步骤 6 新增：初始化柱状图样式
        setupBarChart()

        // 初始化 Adapter
        adapter = StatItemAdapter()

        // 设置 RecyclerView
        binding.recyclerViewStats.adapter = adapter
        binding.recyclerViewStats.layoutManager = LinearLayoutManager(this)


        // 观察 ViewModel 的统计项数据
        viewModel.statItems.observe(this) { statItems ->
            adapter.submitList(statItems)
        }

        // 步骤 6 新增：观察 ViewModel 的柱状图数据
        viewModel.barChartData.observe(this) { (barData, labels) ->
            updateBarChart(barData, labels)
        }

        // 设置 Adapter 的点击事件
        adapter.onItemClick = { statItem ->
            // 当点击 RecyclerView 的某一项时
            val intent = Intent(this, TransactionListActivity::class.java).apply {
                // 传入页面标题和日期范围
                putExtra("PAGE_TITLE", statItem.title)
                putExtra("START_DATE", statItem.startDate)
                putExtra("END_DATE", statItem.endDate)
            }
            startActivity(intent)
        }

        // --- 新增代码：设置菜单按钮（三个点）的点击事件 ---
        binding.btnMenu.setOnClickListener {
            // 创建一个 Intent 来启动 SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            // 启动 Activity
            startActivity(intent)
        }
        // --- 新增代码结束 ---

        // 设置按钮点击事件
        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java).apply {
                putExtra("IS_INCOME", true) // 传入 "是收入" 的标记
            }
            startActivity(intent)
        }

        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java).apply {
                putExtra("IS_INCOME", false) // 传入 "不是收入" (即支出) 的标记
            }
            startActivity(intent)
        }

        // 为根视图设置边距，以避免系统栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次 Activity 恢复时，都调用 ViewModel 的方法来刷新数据
        // 这确保了从其他页面（如添加/编辑页）返回时，首页数据是最新的
        viewModel.loadStats()
    }

    // 步骤 6 新增：初始化柱状图样式
    private fun setupBarChart() {
        // !!! 修复点：使用 binding.chart (BarChart) 而不是 binding.pieChart
        binding.chart.apply {
            description.isEnabled = false // 禁用描述
            legend.isEnabled = true // 启用图例（收入/支出）
            setDrawValueAboveBar(false) // 值在柱子内部绘制
            setDrawGridBackground(false) // 不绘制网格背景

            // X 轴设置
            xAxis.position = XAxis.XAxisPosition.BOTTOM // X轴在底部
            xAxis.setDrawGridLines(false) // 不绘制 X 轴网格线
            xAxis.granularity = 1f // X 轴最小间隔
            xAxis.textColor = ContextCompat.getColor(this@MainActivity, R.color.white) // 设置 X 轴标签颜色

            // Y 轴（左侧）
            axisLeft.setDrawGridLines(true) // 绘制 Y 轴网格线
            axisLeft.textColor = ContextCompat.getColor(this@MainActivity, R.color.grey_400) // 设置 Y 轴标签颜色
            axisLeft.axisMinimum = 0f // Y 轴从 0 开始

            // Y 轴（右侧）
            axisRight.isEnabled = false // 禁用右侧 Y 轴

            // 额外设置
            isDoubleTapToZoomEnabled = false // 禁用双击缩放
            setPinchZoom(false) // 禁用双指缩放
        }
    }

    // 步骤 6 新增：更新柱状图数据
    private fun updateBarChart(data: BarData?, labels: List<String>?) {
        if (data == null || labels == null) {
            // 如果数据为空，清空图表
            binding.chart.clear()
            binding.chart.invalidate()
            return
        }

        // 关键：设置 X 轴的标签
        binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chart.xAxis.labelCount = labels.size

        // 组合柱状图的设置
        val groupSpace = 0.1f // 组间距
        val barSpace = 0.05f // 柱间距 (每组内)
        data.barWidth = 0.4f // 柱子宽度

        binding.chart.data = data
        binding.chart.groupBars(0f, groupSpace, barSpace) // 从 0f 开始组合
        binding.chart.invalidate() // 刷新图表
    }
}