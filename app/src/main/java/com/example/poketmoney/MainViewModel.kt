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

    // (无变化)
    private val _statItems = MutableLiveData<List<StatItem>>()
    val statItems: LiveData<List<StatItem>> = _statItems

    // (无变化)
    private val _barChartData = MutableLiveData<Pair<BarData, List<String>>>()
    val barChartData: LiveData<Pair<BarData, List<String>>> = _barChartData

    // (无变化)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    // (无变化)
    private val _activeLedgerId = MutableLiveData<Long>()

    // (无变化)
    val activeLedgerName: LiveData<String?> = _activeLedgerId.switchMap { id ->
        ledgerDao.getLedgerById(id).switchMap { ledger ->
            MutableLiveData(ledger?.name)
        }
    }

<<<<<<< HEAD
    // (无变化)
    val ledgerCount: LiveData<Int> = ledgerDao.getLedgerCount()


    /**
     * (无变化)
     */
    init {
        // (空)
    }

    /**
     * (无变化)
     */
    fun loadDataForActiveLedger() {
        viewModelScope.launch {
            val currentId = LedgerManager.getActiveLedgerId(getApplication())

            // (安全检查：修复当 ID 不存在时的逻辑)
            // (我们不能在协程中直接检查 LiveData.value)
            val firstLedger = ledgerDao.getFirstLedger() // 尝试获取第一个账本

            if (firstLedger == null) {
                // (数据库中一个账本都没有，ledgerCount 应该为 0，MainActivity 会处理)
                return@launch
            }

            // 检查当前 ID 是否有效
            val currentLedger = ledgerDao.getLedgerById(currentId).value
            if (currentLedger == null) {
                // 如果 ID 无效 (例如被删除了)，则切换到第一个账本
                LedgerManager.setActiveLedgerId(getApplication(), firstLedger.id)
                _activeLedgerId.value = firstLedger.id
            } else {
                // ID 有效
                _activeLedgerId.value = currentId
            }

            loadStats()
        }
=======
    // --- (!!! 1. 新增 LiveData !!!) ---
    /**
     * 用于通知 MainActivity 是否需要跳转到 "强制创建账本" 页。
     */
    private val _navigateToCreateLedger = MutableLiveData<Boolean>()
    val navigateToCreateLedger: LiveData<Boolean> = _navigateToCreateLedger

    /**
     * (!!! 2. 修改：更新初始化逻辑 !!!)
     * ViewModel 初始化时，立即调用数据刷新/检查逻辑
     */
    init {
        refreshData()
>>>>>>> temp-branch
    }


    /**
<<<<<<< HEAD
     * (无变化)
     */
    fun checkActiveLedger() {
        val newLedgerId = LedgerManager.getActiveLedgerId(getApplication())
        if (newLedgerId != _activeLedgerId.value) {
            _activeLedgerId.value = newLedgerId
            loadStats()
        } else {
            loadStats()
        }
    }

    // (!!! 1. 关键修改：loadStats() !!!)
    fun loadStats() {
        viewModelScope.launch {
            try {
                // (无变化)
                val ledgerId = _activeLedgerId.value ?: run {
                    val currentId = LedgerManager.getActiveLedgerId(getApplication())
                    _activeLedgerId.value = currentId
                    currentId
                }
=======
     * (!!! 3. 新增函数 !!!)
     * 重置导航信号
     */
    fun doneNavigatingToCreateLedger() {
        _navigateToCreateLedger.value = false
    }

    /**
     * (!!! 4. 关键修改：重构 checkActiveLedger() !!!)
     *
     * 这个函数现在是数据加载的核心。
     * 它在 App 启动 (init) 和返回首页 (onResume) 时被调用。
     *
     * 职责：
     * 1. 检查数据库中是否 *至少有一个* 账本。
     * 2. 如果没有 -> 发送导航信号 (navigateToCreateLedger = true)。
     * 3. 如果有 -> 确保 "激活ID" 有效，然后加载统计数据 (loadStats)。
     */
    fun refreshData() {
        viewModelScope.launch {
            val application = getApplication<Application>()
            // 检查数据库中是否存在 *任何* 账本
            val firstLedger = ledgerDao.getFirstLedger()

            if (firstLedger == null) {
                // --- Case 1: 数据库中没有账本 ---
                // 发送信号，通知 MainActivity 跳转
                _navigateToCreateLedger.postValue(true)
                // (此时 _activeLedgerId 保持 null, loadStats() 不会执行)

            } else {
                // --- Case 2: 数据库中 *有* 账本 ---
                var activeLedgerId = LedgerManager.getActiveLedgerId(application)

                // 检查 SharedPreferences 中存的 ID 是否真的有效
                val activeLedger = ledgerDao.getLedgerByIdSuspend(activeLedgerId)

                if (activeLedger == null) {
                    // ID 无效 (例如，该账本被删除了)
                    // 自动切换到数据库中的第一个账本作为 "激活" 账本
                    activeLedgerId = firstLedger.id
                    LedgerManager.setActiveLedgerId(application, activeLedgerId)
                }

                // 确保 LiveData 更新
                _activeLedgerId.value = activeLedgerId

                // (重要) 只有在确认有账本后，才加载统计数据
                loadStats()
            }
        }
    }

    // (!!! 5. 已移除旧的 checkActiveLedger() !!!)


    /**
     * (!!! 6. 修改：loadStats() 现在依赖 _activeLedgerId !!!)
     * 只有在 _activeLedgerId 被设置 (即确认有账本) 后，此函数才会执行。
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                // (关键检查) 如果 ledgerId 为 null (因为没有账本), 则不执行
                val ledgerId = _activeLedgerId.value ?: return@launch
>>>>>>> temp-branch

                val calendar = Calendar.getInstance()

                // (!!! 2. 新增：获取月份和年份天数 !!!)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toString()
                val daysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR).toString()

                // (!!! 3. 修改：计算今天 !!!)
                val todayStart = getStartOfDay(calendar.timeInMillis)
                val todayEnd = getEndOfDay(calendar.timeInMillis)
                val todayIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, todayStart, todayEnd) ?: 0.0
                val todayExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, todayStart, todayEnd) ?: 0.0
                val todayItem = StatItem(
                    iconText = "1", // (新增)
                    title = "今天",
                    subtitle = dateFormat.format(Date(todayStart)),
                    income = todayIncome,
                    expense = todayExpense,
                    balance = todayIncome - todayExpense,
                    startDate = todayStart,
                    endDate = todayEnd
                )

                // (!!! 4. 修改：计算本周 !!!)
                val (weekStart, weekEnd) = getCurrentWeekRange(calendar)
                val weekIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, weekStart, weekEnd) ?: 0.0
                val weekExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, weekStart, weekEnd) ?: 0.0
                val weekItem = StatItem(
                    iconText = "7", // (新增)
                    title = "本周",
                    subtitle = getWeekSubtitle(weekStart, weekEnd),
                    income = weekIncome,
                    expense = weekExpense,
                    balance = weekIncome - weekExpense,
                    startDate = weekStart,
                    endDate = weekEnd
                )

                // (!!! 5. 修改：计算本月 !!!)
                val (monthStart, monthEnd) = getCurrentMonthRange(calendar)
                val monthIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, monthStart, monthEnd) ?: 0.0
                val monthExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, monthStart, monthEnd) ?: 0.0
                val monthItem = StatItem(
                    iconText = daysInMonth, // (新增)
                    title = "本月",
                    subtitle = getMonthSubtitle(monthStart, monthEnd),
                    income = monthIncome,
                    expense = monthExpense,
                    balance = monthIncome - monthExpense,
                    startDate = monthStart,
                    endDate = monthEnd
                )

                // (!!! 6. 修改：计算今年 !!!)
                val (yearStart, yearEnd) = getCurrentYearRange(calendar)
                val yearIncome = transactionDao.getTotalIncomeByDateRange(ledgerId, yearStart, yearEnd) ?: 0.0
                val yearExpense = transactionDao.getTotalExpenseByDateRange(ledgerId, yearStart, yearEnd) ?: 0.0
                val yearItem = StatItem(
                    iconText = daysInYear, // (新增)
                    title = "今年",
                    subtitle = getYearSubtitle(yearStart, yearEnd),
                    income = yearIncome,
                    expense = yearExpense,
                    balance = yearIncome - yearExpense,
                    startDate = yearStart,
                    endDate = yearEnd
                )

                // (!!! 7. 修改：计算总计 !!!)
                val totalIncome = transactionDao.getTotalIncome(ledgerId) ?: 0.0
                val totalExpense = transactionDao.getTotalExpense(ledgerId) ?: 0.0
                val totalItem = StatItem(
                    iconText = "∑", // (新增)
                    title = "总计",
                    subtitle = "所有记录",
                    income = totalIncome,
                    expense = totalExpense,
                    balance = totalIncome - totalExpense,
                    startDate = 0L,
                    endDate = 0L
                )

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