package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    // 用于通知 Activity 关闭的 LiveData
    private val _shouldCloseActivity = MutableLiveData<Boolean>()
    val shouldCloseActivity: LiveData<Boolean> = _shouldCloseActivity

    // 用于存放从数据库加载的、待编辑的交易
    private val _transactionToEdit = MutableLiveData<Transaction?>()
    val transactionToEdit: LiveData<Transaction?> = _transactionToEdit

    /**
     * 根据 ID 加载单条交易记录
     */
    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionDao.getTransactionById(id)
            _transactionToEdit.postValue(transaction)
        }
    }

    /**
     * 插入一条新交易
     */
    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
            _shouldCloseActivity.postValue(true) // 通知 Activity 关闭
        }
    }

    /**
     * 更新一条现有交易
     */
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction)
            _shouldCloseActivity.postValue(true) // 通知 Activity 关闭
        }
    }

    /**
     * 删除一条交易
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
            _shouldCloseActivity.postValue(true) // 通知 Activity 关闭
        }
    }

    /**
     * 重置关闭信号
     */
    fun doneClosingActivity() {
        _shouldCloseActivity.value = false
    }
}
