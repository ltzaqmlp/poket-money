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
        enableEdgeToEdge()
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
            val intent = Intent(this, TransactionListActivity::class.java).apply {
                putExtra("PAGE_TITLE", statItem.title)
                putExtra("START_DATE", statItem.startDate)
                putExtra("END_DATE", statItem.endDate)
            }
            startActivity(intent)
        }

        // 设置按钮点击事件
        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java).apply {
                putExtra("IS_INCOME", true)
            }
            startActivity(intent)
        }

        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java).apply {
                putExtra("IS_INCOME", false)
            }
            startActivity(intent)
        }

        // 为根视图设置边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次 Activity 恢复时，都调用 ViewModel 的方法来刷新数据
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
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false) // 不绘制 X 轴网格线
            xAxis.granularity = 1f // X 轴最小间隔
            xAxis.textColor = ContextCompat.getColor(this@MainActivity, R.color.white) // 假设你有 R.color.white

            // Y 轴（左侧）
            axisLeft.setDrawGridLines(true) // 绘制 Y 轴网格线
            axisLeft.textColor = ContextCompat.getColor(this@MainActivity, R.color.grey_400) // 假设你有 R.color.grey_400
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
            binding.chart.clear()
            binding.chart.invalidate()
            return
        }

        // 关键：设置 X 轴的标签
        binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chart.xAxis.labelCount = labels.size

        // 组合柱状图的设置
        val groupSpace = 0.1f
        val barSpace = 0.05f // (0.4f * 2) + 0.05f * 2 + 0.1f = 1.0f
        data.barWidth = 0.4f

        binding.chart.data = data
        binding.chart.groupBars(0f, groupSpace, barSpace) // 从 0 开始组合
        binding.chart.invalidate() // 刷新图表
    }
}