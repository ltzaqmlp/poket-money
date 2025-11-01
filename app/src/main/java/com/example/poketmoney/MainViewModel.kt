package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
// 步骤 6 新增：导入柱状图相关的类
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
// 修复 Bug：导入 ContextCompat
import androidx.core.content.ContextCompat

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    private val _statItems = MutableLiveData<List<StatItem>>()
    val statItems: LiveData<List<StatItem>> = _statItems

    // 步骤 6 修改：为柱状图创建 LiveData。
    // Pair<BarData, List<String>> -> 第一个是图表数据, 第二个是 X 轴的标签
    private val _barChartData = MutableLiveData<Pair<BarData, List<String>>>()
    val barChartData: LiveData<Pair<BarData, List<String>>> = _barChartData

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // 步骤 6 新增：用于图表 X 轴的日期格式
    private val dayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    init {
        loadStats() // 初始化时加载统计数据
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()

                // 1. 计算今天
                val todayStart = getStartOfDay(calendar.timeInMillis)
                val todayEnd = getEndOfDay(calendar.timeInMillis)
                val todayIncome = transactionDao.getTotalIncomeByDateRange(todayStart, todayEnd) ?: 0.0
                val todayExpense = transactionDao.getTotalExpenseByDateRange(todayStart, todayEnd) ?: 0.0
                val todayItem = StatItem(
                    title = "今天",
                    subtitle = dateFormat.format(Date(todayStart)),
                    income = todayIncome,
                    expense = todayExpense,
                    balance = todayIncome - todayExpense,
                    startDate = todayStart,
                    endDate = todayEnd
                )

                // 2. 计算本周 (假设周一是第一天)
                val (weekStart, weekEnd) = getCurrentWeekRange(calendar)
                val weekIncome = transactionDao.getTotalIncomeByDateRange(weekStart, weekEnd) ?: 0.0
                val weekExpense = transactionDao.getTotalExpenseByDateRange(weekStart, weekEnd) ?: 0.0
                val weekItem = StatItem(
                    title = "本周",
                    subtitle = getWeekSubtitle(weekStart, weekEnd),
                    income = weekIncome,
                    expense = weekExpense,
                    balance = weekIncome - weekExpense,
                    startDate = weekStart,
                    endDate = weekEnd
                )

                // 3. 计算本月
                val (monthStart, monthEnd) = getCurrentMonthRange(calendar)
                val monthIncome = transactionDao.getTotalIncomeByDateRange(monthStart, monthEnd) ?: 0.0
                val monthExpense = transactionDao.getTotalExpenseByDateRange(monthStart, monthEnd) ?: 0.0
                val monthItem = StatItem(
                    title = "本月",
                    subtitle = getMonthSubtitle(monthStart, monthEnd),
                    income = monthIncome,
                    expense = monthExpense,
                    balance = monthIncome - monthExpense,
                    startDate = monthStart,
                    endDate = monthEnd
                )

                // 4. 计算今年
                val (yearStart, yearEnd) = getCurrentYearRange(calendar)
                val yearIncome = transactionDao.getTotalIncomeByDateRange(yearStart, yearEnd) ?: 0.0
                val yearExpense = transactionDao.getTotalExpenseByDateRange(yearStart, yearEnd) ?: 0.0
                val yearItem = StatItem(
                    title = "今年",
                    subtitle = getYearSubtitle(yearStart, yearEnd),
                    income = yearIncome,
                    expense = yearExpense,
                    balance = yearIncome - yearExpense,
                    startDate = yearStart,
                    endDate = yearEnd
                )

                // 5. 计算总计
                val totalIncome = transactionDao.getTotalIncome() ?: 0.0
                val totalExpense = transactionDao.getTotalExpense() ?: 0.0
                val totalItem = StatItem(
                    title = "总计",
                    subtitle = "所有记录",
                    income = totalIncome,
                    expense = totalExpense,
                    balance = totalIncome - totalExpense,
                    startDate = 0L,
                    endDate = Long.MAX_VALUE
                )

                _statItems.value = listOf(todayItem, weekItem, monthItem, yearItem, totalItem)

                // 步骤 6 修改：加载完统计后，加载柱状图数据
                loadBarChartData()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 步骤 6 新增：加载柱状图数据 (最近 7 天)
    private fun loadBarChartData() {
        viewModelScope.launch {
            try {
                // 1. 确定日期范围 (最近 7 天，包含今天)
                val calendar = Calendar.getInstance()
                val endDate = getEndOfDay(calendar.timeInMillis) // 今天结束
                calendar.add(Calendar.DAY_OF_YEAR, -6) // 倒退 6 天 (总共 7 天)
                val startDate = getStartOfDay(calendar.timeInMillis) // 7 天前的开始

                // 2. 生成 X 轴的 7 个日期标签
                val labels = mutableListOf<String>()
                val tempCal = calendar.clone() as Calendar
                for (i in 0 until 7) {
                    labels.add(dayFormat.format(tempCal.time))
                    tempCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                // 3. 从数据库查询数据
                val incomeSummaries = transactionDao.getDailyIncomeSummary(startDate, endDate)
                val expenseSummaries = transactionDao.getDailyExpenseSummary(startDate, endDate)

                // 4. 将查询结果转换为 Map<String, Double> 以便快速查找
                val incomeMap = incomeSummaries.associateBy({ it.dayLabel }, { it.totalAmount })
                val expenseMap = expenseSummaries.associateBy({ it.dayLabel }, { it.totalAmount })

                // 5. 创建图表数据 (BarEntry)
                val incomeEntries = mutableListOf<BarEntry>()
                val expenseEntries = mutableListOf<BarEntry>()

                // 遍历 7 天的标签，填充数据 (没有数据的天补 0)
                labels.forEachIndexed { index, dayLabel ->
                    val income = incomeMap[dayLabel] ?: 0.0
                    val expense = expenseMap[dayLabel] ?: 0.0
                    incomeEntries.add(BarEntry(index.toFloat(), income.toFloat()))
                    expenseEntries.add(BarEntry(index.toFloat(), expense.toFloat()))
                }

                // 6. 创建数据集 (DataSet)

                // !!! 修复 Bug：从 Context 加载颜色 !!!
                val context = getApplication<Application>().applicationContext
                val incomeColorInt = ContextCompat.getColor(context, R.color.income_color)
                val expenseColorInt = ContextCompat.getColor(context, R.color.expense_color)

                val incomeDataSet = BarDataSet(incomeEntries, "收入")
                incomeDataSet.color = incomeColorInt // 设为整数颜色值
                incomeDataSet.setDrawValues(false) // 不在柱子顶部显示数值

                val expenseDataSet = BarDataSet(expenseEntries, "支出")
                expenseDataSet.color = expenseColorInt // 设为整数颜色值
                expenseDataSet.setDrawValues(false) // 不在柱子顶部显示数值

                // 7. 创建 BarData
                val barData = BarData(incomeDataSet, expenseDataSet)
                barData.barWidth = 0.4f // 设置柱子的宽度

                // 8. 更新 LiveData
                _barChartData.value = Pair(barData, labels)

            } catch (e: Exception) {
                e.printStackTrace()
                // 可以考虑发送一个空状态
            }
        }
    }


    // Helper functions to calculate date ranges
    private fun getStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun getCurrentWeekRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY // 设置周一为一周的开始
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek + 7) % 7
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
        val start = getStartOfDay(cal.timeInMillis)

        cal.add(Calendar.DAY_OF_MONTH, 6) // 加6天到周日
        val end = getEndOfDay(cal.timeInMillis)

        return Pair(start, end)
    }

    private fun getCurrentMonthRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1) // 设置为月初
        val start = getStartOfDay(cal.timeInMillis)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)) // 设置为月末
        val end = getEndOfDay(cal.timeInMillis)

        return Pair(start, end)
    }

    private fun getCurrentYearRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_YEAR, 1) // 设置为年初
        val start = getStartOfDay(cal.timeInMillis)

        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR)) // 设置为年末
        val end = getEndOfDay(cal.timeInMillis)

        return Pair(start, end)
    }

    // Helper functions to format subtitles
    private fun getWeekSubtitle(start: Long, end: Long): String {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = end

        val startMonth = startCal.get(Calendar.MONTH) + 1
        val startDay = startCal.get(Calendar.DAY_OF_MONTH)
        val endMonth = endCal.get(Calendar.MONTH) + 1
        val endDay = endCal.get(Calendar.DAY_OF_MONTH)

        // 这里的花括号是必须的，因为变量名后紧跟了 "月"
        return "${startMonth}月${startDay}日 -- ${endMonth}月${endDay}日"
    }

    private fun getMonthSubtitle(start: Long, end: Long): String {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = end

        val startYear = startCal.get(Calendar.YEAR)
        val startMonth = startCal.get(Calendar.MONTH) + 1
        val endYear = endCal.get(Calendar.YEAR)
        val endMonth = endCal.get(Calendar.MONTH) + 1

        return if (startYear == endYear) {
            // 修正：将 $startYear年$startMonth月 改为 ${startYear}年${startMonth}月
            "${startYear}年${startMonth}月"
        } else {
            // 修正：修正所有变量
            "${startYear}年${startMonth}月 -- ${endYear}年${endMonth}月"
        }
    }

    private fun getYearSubtitle(start: Long, end: Long): String {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = end

        val startYear = startCal.get(Calendar.YEAR)
        val endYear = endCal.get(Calendar.YEAR)

        return if (startYear == endYear) {
            // 修正：将 $startYear年 改为 ${startYear}年
            "${startYear}年"
        } else {
            // 修正：修正两个变量
            "${startYear}年 -- ${endYear}年"
        }
    }
}
