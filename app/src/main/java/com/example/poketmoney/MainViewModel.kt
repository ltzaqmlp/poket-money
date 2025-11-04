package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val ledgerDao = db.ledgerDao()

    private val _statItems = MutableLiveData<List<StatItem>>()
    val statItems: LiveData<List<StatItem>> = _statItems

    private val _barChartData = MutableLiveData<Pair<BarData, List<String>>>()
    val barChartData: LiveData<Pair<BarData, List<String>>> = _barChartData

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    // (无变化) 存储当前激活的账本ID
    private val _activeLedgerId = MutableLiveData<Long>()

    // (无变化) 暴露 "当前激活账本的名称"
    val activeLedgerName: LiveData<String?> = _activeLedgerId.switchMap { id ->
        ledgerDao.getLedgerById(id).switchMap { ledger ->
            MutableLiveData(ledger?.name)
        }
    }

    // (无变化) 初始化
    init {
        viewModelScope.launch {
            var currentId = LedgerManager.getActiveLedgerId(application)

            val firstLedger = ledgerDao.getFirstLedger()
            if (firstLedger == null) {
                currentId = 1L
            } else {
                currentId = LedgerManager.getActiveLedgerId(application)
            }

            _activeLedgerId.value = currentId
            loadStats()
        }
    }

    /**
     * 4. !!! BUG 修复：移除了重复的函数 !!!
     * 这是 checkActiveLedger() 唯一的、正确的版本。
     */
    fun checkActiveLedger() {
        val newLedgerId = LedgerManager.getActiveLedgerId(getApplication())
        if (newLedgerId != _activeLedgerId.value) {
            // 账本 ID 已经改变了！ (例如从 "账本管理页" 返回)
            _activeLedgerId.value = newLedgerId
            loadStats() // 重新加载新账本的数据
        } else {
            // 账本 ID 没有变，但我们可能从 "新增记录页" 返回了
            // 仍然需要刷新数据
            loadStats()
        }
    }

    // 5. (!!! 已移除 !!!) 这里是之前那个重复的、错误的函数

    // (无变化) loadStats()
    fun loadStats() {
        viewModelScope.launch {
            try {
                val ledgerId = _activeLedgerId.value ?: return@launch

                val calendar = Calendar.getInstance()

                // (无变化) 计算今天
                val todayStart = getStartOfDay(calendar.timeInMillis)
                val todayEnd = getEndOfDay(calendar.timeInMillis)
                val todayIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, todayStart, todayEnd) ?: 0.0
                val todayExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, todayStart, todayEnd) ?: 0.0
                val todayItem = StatItem("今天", dateFormat.format(Date(todayStart)), todayIncome, todayExpense, todayIncome - todayExpense, todayStart, todayEnd)

                // (无变化) 计算本周
                val (weekStart, weekEnd) = getCurrentWeekRange(calendar)
                val weekIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, weekStart, weekEnd) ?: 0.0
                val weekExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, weekStart, weekEnd) ?: 0.0
                val weekItem = StatItem("本周", getWeekSubtitle(weekStart, weekEnd), weekIncome, weekExpense, weekIncome - weekExpense, weekStart, weekEnd)

                // (无变化) 计算本月
                val (monthStart, monthEnd) = getCurrentMonthRange(calendar)
                val monthIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, monthStart, monthEnd) ?: 0.0
                val monthExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, monthStart, monthEnd) ?: 0.0
                val monthItem = StatItem("本月", getMonthSubtitle(monthStart, monthEnd), monthIncome, monthExpense, monthIncome - monthExpense, monthStart, monthEnd)

                // (无变化) 计算今年
                val (yearStart, yearEnd) = getCurrentYearRange(calendar)
                val yearIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, yearStart, yearEnd) ?: 0.0
                val yearExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, yearStart, yearEnd) ?: 0.0
                val yearItem = StatItem("今年", getYearSubtitle(yearStart, yearEnd), yearIncome, yearExpense, yearIncome - yearExpense, yearStart, yearEnd)

                // (无变化) 计算总计
                val totalIncome = transactionDao.getTotalIncome(ledgerId) ?: 0.0
                val totalExpense = transactionDao.getTotalExpense(ledgerId) ?: 0.0
                val totalItem = StatItem("总计", "所有记录", totalIncome, totalExpense, totalIncome - totalExpense, 0L, 0L)

                _statItems.value = listOf(todayItem, weekItem, monthItem, yearItem, totalItem)

                // (无变化)
                loadBarChartData()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // (无变化) loadBarChartData()
    private fun loadBarChartData() {
        viewModelScope.launch {
            try {
                val ledgerId = _activeLedgerId.value ?: return@launch

                val calendar = Calendar.getInstance()
                val endDate = getEndOfDay(calendar.timeInMillis)
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                val startDate = getStartOfDay(calendar.timeInMillis)

                val labels = mutableListOf<String>()
                val tempCal = calendar.clone() as Calendar
                for (i in 0 until 7) {
                    labels.add(dayFormat.format(tempCal.time))
                    tempCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val incomeSummaries = transactionDao.getDailyIncomeSummary(ledgerId, startDate, endDate)
                val expenseSummaries = transactionDao.getDailyExpenseSummary(ledgerId, startDate, endDate)

                val incomeMap = incomeSummaries.associateBy({ it.dayLabel }, { it.totalAmount })
                val expenseMap = expenseSummaries.associateBy({ it.dayLabel }, { it.totalAmount })

                val incomeEntries = mutableListOf<BarEntry>()
                val expenseEntries = mutableListOf<BarEntry>()

                labels.forEachIndexed { index, dayLabel ->
                    val income = incomeMap[dayLabel] ?: 0.0
                    val expense = expenseMap[dayLabel] ?: 0.0
                    incomeEntries.add(BarEntry(index.toFloat(), income.toFloat()))
                    expenseEntries.add(BarEntry(index.toFloat(), expense.toFloat()))
                }

                val context = getApplication<Application>().applicationContext
                val incomeColorInt = ContextCompat.getColor(context, R.color.income_color)
                val expenseColorInt = ContextCompat.getColor(context, R.color.expense_color)

                val incomeDataSet = BarDataSet(incomeEntries, "收入")
                incomeDataSet.color = incomeColorInt
                incomeDataSet.setDrawValues(false)

                val expenseDataSet = BarDataSet(expenseEntries, "支出")
                expenseDataSet.color = expenseColorInt
                expenseDataSet.setDrawValues(false)

                val barData = BarData(incomeDataSet, expenseDataSet)
                barData.barWidth = 0.4f

                _barChartData.value = Pair(barData, labels)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // --- Helper functions (无变化) ---
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
        cal.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek + 7) % 7
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
        val start = getStartOfDay(cal.timeInMillis)

        cal.add(Calendar.DAY_OF_MONTH, 6)
        val end = getEndOfDay(cal.timeInMillis)

        return Pair(start, end)
    }

    private fun getCurrentMonthRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = getStartOfDay(cal.timeInMillis)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = getEndOfDay(cal.timeInMillis)

        return Pair(start, end)
    }

    private fun getCurrentYearRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_YEAR, 1)
        val start = getStartOfDay(cal.timeInMillis)

        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
        val end = getEndOfDay(cal.timeInMillis)

        return Pair(start, end)
    }

    // (无变化)
    private fun getWeekSubtitle(start: Long, end: Long): String {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = end

        val startMonth = startCal.get(Calendar.MONTH) + 1
        val startDay = startCal.get(Calendar.DAY_OF_MONTH)
        val endMonth = endCal.get(Calendar.MONTH) + 1
        val endDay = endCal.get(Calendar.DAY_OF_MONTH)

        return "${startMonth}月${startDay}日 -- ${endMonth}月${endDay}日"
    }

    // (无变化)
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
            "${startYear}年${startMonth}月"
        } else {
            "${startYear}年${startMonth}月 -- ${endYear}年${endMonth}月"
        }
    }

    // (无变化)
    private fun getYearSubtitle(start: Long, end: Long): String {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = end

        val startYear = startCal.get(Calendar.YEAR)
        val endYear = endCal.get(Calendar.YEAR)

        return if (startYear == endYear) {
            "${startYear}年"
        } else {
            "${startYear}年 -- ${endYear}年"
        }
    }
}