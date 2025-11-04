package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * !!! 这是一个关键的 Bug 修复 !!!
 * 我们必须更新这个 ViewModel 来处理 ledgerId (账本ID)
 */
class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    // 1. !!! 新增：获取当前激活的账本 ID !!!
    // 我们只在加载时获取一次，因为这个页面的账本上下文是固定的
    private val activeLedgerId: Long = LedgerManager.getActiveLedgerId(application)

    // 用于触发 switchMap 的内部状态 (无变化)
    private val _dateRange = MutableLiveData<Pair<Long, Long>>()

    // 2. !!! 修改：更新 switchMap 中的 DAO 调用 !!!
    val transactions: LiveData<List<Transaction>> = _dateRange.switchMap { (start, end) ->
        if (start == 0L && end == 0L) {
            // “总计”：加载所有交易 (!!! 传入 activeLedgerId !!!)
            transactionDao.getAllTransactions(activeLedgerId)
        } else {
            // 按日期范围查询 (!!! 传入 activeLedgerId !!!)
            transactionDao.getTransactionsByDateRangeLiveData(activeLedgerId, start, end)
        }
    }

    // (无变化)
    fun loadTransactions(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

    // (无变化)
//    /**
//     * 加载指定日期范围内的交易记录。
//     * 如果 startDate 和 endDate 均为 0，则视为“总计”，加载所有记录。
//     */
//    fun loadTransactions(startDate: Long, endDate: Long) {
//        if (startDate == 0L && endDate == 0L) {
//            _dateRange.value = null // 触发 getAllTransactions()
//        } else {
//            _dateRange.value = Pair(startDate, endDate)
//        }
//    }
}