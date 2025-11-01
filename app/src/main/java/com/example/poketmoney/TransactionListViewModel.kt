package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    // 用于触发 switchMap 的内部状态
    private val _dateRange = MutableLiveData<Pair<Long, Long>>()

    // 根据 dateRange 是否为 null 决定查询方式
    val transactions: LiveData<List<Transaction>> = _dateRange.switchMap { (start, end) ->
        if (start == 0L && end == 0L) {
            // “总计”：加载所有交易
            transactionDao.getAllTransactions()
        } else {
            // 按日期范围查询
            transactionDao.getTransactionsByDateRangeLiveData(start, end)
        }
    }

    fun loadTransactions(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

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