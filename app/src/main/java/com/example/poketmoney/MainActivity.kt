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

        // (无变化)
        adapter = StatItemAdapter()

        // (无变化)
        binding.recyclerViewStats.adapter = adapter
        binding.recyclerViewStats.layoutManager = LinearLayoutManager(this)


        // (无变化)
        setupObservers()


        // (无变化)
        adapter.onItemClick = { statItem ->
            val intent = Intent(this, TransactionListActivity::class.java).apply {
                putExtra("PAGE_TITLE", statItem.title)
                putExtra("START_DATE", statItem.startDate)
                putExtra("END_DATE", statItem.endDate)
            }
            startActivity(intent)
        }

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
    private fun setupBarChart() {
        binding.chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
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

    // (无变化)
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