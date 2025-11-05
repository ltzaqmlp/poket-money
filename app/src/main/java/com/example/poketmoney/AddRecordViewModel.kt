package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
// 导入 (无变化)
import androidx.room.withTransaction
import kotlinx.coroutines.launch

class AddRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val accountDao = db.accountDao()

    // !!! 1. 新增：获取当前激活的账本 ID
    private val activeLedgerId: Long

    // (无变化)
    private val _shouldCloseActivity = MutableLiveData<Boolean>()
    val shouldCloseActivity: LiveData<Boolean> = _shouldCloseActivity

    // (无变化)
    private val _transactionToEdit = MutableLiveData<Transaction?>()
    val transactionToEdit: LiveData<Transaction?> = _transactionToEdit

    // !!! 2. 修改：allAccounts 现在依赖 activeLedgerId
    /**
     * (修改) 暴露 *当前账本* 的所有账户列表
     */
    val allAccounts: LiveData<List<Account>>

    // (无变化)
    private val _newlyCreatedAccount = MutableLiveData<Account?>()
    val newlyCreatedAccount: LiveData<Account?> = _newlyCreatedAccount

    // !!! 3. 新增：init 块
    init {
        // (新增) 在 ViewModel 初始化时，获取一次账本 ID
        activeLedgerId = LedgerManager.getActiveLedgerId(application)

        // (修改) 使用获取到的 ID 来初始化账户列表
        allAccounts = accountDao.getAllAccounts(activeLedgerId)
    }


    /**
     * (无变化) 根据 ID 加载单条交易记录
     */
    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionDao.getTransactionById(id)
            _transactionToEdit.postValue(transaction)
        }
    }

    // --- 交易操作 (无变化) ---
    // (这些函数依赖的是 accountId，它是全局唯一的，
    //  并且余额计算逻辑是正确的，所以不需要修改)

    /**
     * (无变化) 插入一条新交易 (带余额计算)
     */
    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            db.withTransaction {
                // ... (无变化)
                transactionDao.insert(transaction)
                transaction.accountId?.let { accId ->
                    val account = accountDao.getAccountById(accId)
                    if (account != null) {
                        val newBalance = if (transaction.type == "income") {
                            account.balance + transaction.amount
                        } else {
                            account.balance - transaction.amount
                        }
                        accountDao.update(account.copy(balance = newBalance))
                    }
                }
            }
            _shouldCloseActivity.postValue(true)
        }
    }

    /**
     * (无变化) 更新一条现有交易 (带余额计算)
     */
    fun updateTransaction(updatedTransaction: Transaction) {
        viewModelScope.launch {
            val originalTransaction = _transactionToEdit.value
            if (originalTransaction == null) {
                transactionDao.update(updatedTransaction)
                _shouldCloseActivity.postValue(true)
                return@launch
            }

            db.withTransaction {
                // ... (无变化)
                transactionDao.update(updatedTransaction)
                originalTransaction.accountId?.let { oldAccId ->
                    val oldAccount = accountDao.getAccountById(oldAccId)
                    if (oldAccount != null) {
                        val revertedBalance = if (originalTransaction.type == "income") {
                            oldAccount.balance - originalTransaction.amount
                        } else {
                            oldAccount.balance + originalTransaction.amount
                        }
                        accountDao.update(oldAccount.copy(balance = revertedBalance))
                    }
                }
                updatedTransaction.accountId?.let { newAccId ->
                    val newAccount = accountDao.getAccountById(newAccId)
                    if (newAccount != null) {
                        val appliedBalance = if (updatedTransaction.type == "income") {
                            newAccount.balance + updatedTransaction.amount
                        } else {
                            newAccount.balance - updatedTransaction.amount
                        }
                        accountDao.update(newAccount.copy(balance = appliedBalance))
                    }
                }
            }
            _shouldCloseActivity.postValue(true)
        }
    }

    /**
     * (无变化) 删除一条交易 (带余额计算)
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            db.withTransaction {
                // ... (无变化)
                transactionDao.delete(transaction)
                transaction.accountId?.let { accId ->
                    val account = accountDao.getAccountById(accId)
                    if (account != null) {
                        val revertedBalance = if (transaction.type == "income") {
                            account.balance - transaction.amount
                        } else {
                            account.balance + transaction.amount
                        }
                        accountDao.update(account.copy(balance = revertedBalance))
                    }
                }
            }
            _shouldCloseActivity.postValue(true)
        }
    }

    // --- 账户操作 ---

    /**
     * (!!! 4. 修改：插入一个新账户 !!!)
     * Activity 会在创建 Account 对象时传入 ledgerId，
     * 所以这个函数 *不需要* 修改。
     * 它接收一个完整的 Account 对象 并插入。
     */
    fun insertAccount(account: Account) {
        viewModelScope.launch {
            // (无变化)
            val newId = accountDao.insert(account)
            _newlyCreatedAccount.postValue(account.copy(id = newId))
        }
    }

    // --- 重置 LiveData 信号 (无变化) ---

    /**
     * (无变化)
     */
    fun doneClosingActivity() {
        _shouldCloseActivity.value = false
    }

    /**
     * (无变化)
     */
    fun doneSelectingNewAccount() {
        _newlyCreatedAccount.value = null
    }
}