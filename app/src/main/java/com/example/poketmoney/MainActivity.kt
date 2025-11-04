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

        // (无变化) 初始化柱状图样式
        setupBarChart()

        // (无变化) 初始化 Adapter
        adapter = StatItemAdapter()

        // (无变化) 设置 RecyclerView
        binding.recyclerViewStats.adapter = adapter
        binding.recyclerViewStats.layoutManager = LinearLayoutManager(this)


        // --- (!!! 关键修改：观察 LiveData !!!) ---

        // (无变化) 观察 ViewModel 的统计项数据
        viewModel.statItems.observe(this) { statItems ->
            adapter.submitList(statItems)
        }

        // (无变化) 观察 ViewModel 的柱状图数据
        viewModel.barChartData.observe(this) { (barData, labels) ->
            updateBarChart(barData, labels)
        }

        // (!!! 1. 新增：观察 "当前账本名称" !!!)
        /**
         * 观察我们在 MainViewModel 中创建的 activeLedgerName
         */
        viewModel.activeLedgerName.observe(this) { ledgerName ->
            // 当 LiveData 变化时，
            // 1. 检查名称是否为空 (虽然理论上不应该为空)
            // 2. 将其设置到我们在 activity_main.xml 中添加的 tvTitleLedgerName
            binding.tvTitleLedgerName.text = ledgerName ?: getString(R.string.app_name)
        }
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
     * (无变化) onResume()
     * 当我们从 LedgerManageActivity 返回时，
     * onResume() 会被调用。
     */
    override fun onResume() {
        super.onResume()

        // (无变化)
        // 1. 调用 checkActiveLedger()
        // 2. 这会触发 MainViewModel 检查 ID
        // 3. 如果 ID 变化了，_activeLedgerId 会更新
        // 4. _activeLedgerId 的更新会 *自动* 触发 activeLedgerName 的更新
        // 5. activeLedgerName 的更新会 *自动* 触发我们在 onCreate 中设置的 observe
        // 6. 最终，标题被更新。
        viewModel.checkActiveLedger()
    }

    // (无变化) 初始化柱状图样式
    private fun setupBarChart() {
        binding.chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawValueAboveBar(false)
            setDrawGridBackground(false)

            // X 轴设置
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = ContextCompat.getColor(this@MainActivity, R.color.white)

            // Y 轴（左侧）
            axisLeft.setDrawGridLines(true)
            axisLeft.textColor = ContextCompat.getColor(this@MainActivity, R.color.grey_400)
            axisLeft.axisMinimum = 0f

            // Y 轴（右侧）
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