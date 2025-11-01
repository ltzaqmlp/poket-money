package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    // 私有的 MutableLiveData，用于 ViewModel 内部更新
    private val _transactions = MutableLiveData<List<Transaction>>()
    // 公开的 LiveData，供 Activity 观察
    val transactions: LiveData<List<Transaction>> = _transactions

    /**
     * 根据日期范围加载交易记录
     * @param startDate 开始时间戳 (毫秒)
     * @param endDate 结束时间戳 (毫秒)
     */
    fun loadTransactions(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                val result = transactionDao.getTransactionsByDateRange(startDate, endDate)
                _transactions.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _transactions.value = emptyList() // 出错时返回空列表
            }
        }
    }
}
