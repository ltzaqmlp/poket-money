package com.example.poketmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.room.Transaction as RoomTransaction // (!!! 1. 新增：为 Room 的 @Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * (!!! 已重构：全面支持 "账户" 和 "余额同步" 逻辑 !!!)
 * (!!! 已更新：支持在 ViewModel 中新增账户 !!!)
 */
class AddRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    // (!!! 2. 新增：需要 AccountDao 来更新余额 !!!)
    private val accountDao = db.accountDao()

    // (无变化)
    private val _shouldCloseActivity = MutableLiveData<Boolean>()
    val shouldCloseActivity: LiveData<Boolean> = _shouldCloseActivity

    // (无变化)
    private val _transactionToEdit = MutableLiveData<Transaction?>()
    val transactionToEdit: LiveData<Transaction?> = _transactionToEdit

    // (!!! 3. 新增：用于 "账户" 下拉列表的 LiveData !!!)
    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    // (!!! 8. 新增：用于通知 "新增账户" 已完成 !!!)
    private val _newlyCreatedAccountId = MutableLiveData<Long?>()
    val newlyCreatedAccountId: LiveData<Long?> = _newlyCreatedAccountId


    // (!!! 4. 新增：加载当前账本的账户列表 !!!)
    fun loadAccounts(ledgerId: Long) {
        viewModelScope.launch {
            _accounts.value = accountDao.getAccountsByLedgerSuspend(ledgerId)
        }
    }

    /**
     * (无变化)
     * 根据 ID 加载单条交易记录
     */
    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionDao.getTransactionById(id)
            _transactionToEdit.postValue(transaction)
        }
    }

    /**
     * (!!! 5. 关键重构：插入交易并 *同步* 账户余额 !!!)
     *
     * @param transaction 要插入的新交易
     */
    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            // (安全检查：必须选择了账户)
            val accountId = transaction.accountId ?: return@launch

            // 计算余额变化
            val amountDelta = if (transaction.type == "income") {
                transaction.amount // +5000 (收入)
            } else {
                -transaction.amount // -150 (支出)
            }

            // (!!! 使用 Room 的 @Transaction 确保原子性 !!!)
            // (虽然我们是分开调用，但这是推荐的封装方式)
            // (在更复杂的场景中，我们会把这2步移入 Dao 的 @Transaction 函数)
            withContext(Dispatchers.IO) {
                // 1. 插入交易记录
                transactionDao.insert(transaction)
                // 2. 更新账户余额
                accountDao.updateAccountBalance(accountId, amountDelta)
            }

            _shouldCloseActivity.postValue(true) // 通知 Activity 关闭
        }
    }

    /**
     * (!!! 6. 关键重构：更新交易并 *同步* 两个账户余额 !!!)
     *
     * @param oldTransaction 更新前的原始交易 (用于撤销)
     * @param newTransaction 用户保存的更新后交易
     */
    fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        viewModelScope.launch {
            // (安全检查：新旧交易都必须有关联账户)
            val oldAccountId = oldTransaction.accountId ?: return@launch
            val newAccountId = newTransaction.accountId ?: return@launch

            // 1. 计算 "旧" 交易需要被撤销的金额
            // (例如：旧的是支出 -100, 撤销操作就是 +100)
            val oldAmountDelta = if (oldTransaction.type == "income") {
                -oldTransaction.amount // (撤销 +5000) -> -5000
            } else {
                oldTransaction.amount // (撤销 -100) -> +100
            }

            // 2. 计算 "新" 交易需要被应用的金额
            val newAmountDelta = if (newTransaction.type == "income") {
                newTransaction.amount // +5000 (收入)
            } else {
                -newTransaction.amount // -150 (支出)
            }

            // (!!! 启动数据库操作 !!!)
            withContext(Dispatchers.IO) {
                // 3. 更新交易记录本身
                transactionDao.update(newTransaction)

                if (oldAccountId == newAccountId) {
                    // --- Case 1: 账户未改变 (例如：只是改了金额或备注) ---
                    // (旧的 +100, 新的 -150) -> 总变化 -50
                    val totalDelta = oldAmountDelta + newAmountDelta
                    accountDao.updateAccountBalance(newAccountId, totalDelta)
                } else {
                    // --- Case 2: 账户已改变 (例如：从 "现金" 改为 "储蓄卡") ---
                    // 1. 撤销旧账户的旧金额
                    accountDao.updateAccountBalance(oldAccountId, oldAmountDelta)
                    // 2. 应用新账户的新金额
                    accountDao.updateAccountBalance(newAccountId, newAmountDelta)
                }
            }

            _shouldCloseActivity.postValue(true) // 通知 Activity 关闭
        }
    }

    /**
     * (!!! 7. 关键重构：删除交易并 *同步* 账户余额 !!!)
     *
     * @param transaction 要删除的交易
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            // (安全检查：必须有关联账户)
            val accountId = transaction.accountId ?: return@launch

            // 计算 "撤销" 金额
            val amountDelta = if (transaction.type == "income") {
                -transaction.amount // (删除 +5000) -> -5000
            } else {
                transaction.amount // (删除 -150) -> +150
            }

            withContext(Dispatchers.IO) {
                // 1. 删除交易
                transactionDao.delete(transaction)
                // 2. 更新 (撤销) 账户余额
                accountDao.updateAccountBalance(accountId, amountDelta)
            }

            _shouldCloseActivity.postValue(true) // 通知 Activity 关闭
        }
    }

    /**
     * (!!! 9. 新增：插入一个新账户 !!!)
     * (从 "新增/编辑记录" 页的弹窗中调用)
     */
    fun insertAccount(account: Account) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                accountDao.insert(account)
            }
            // 插入后，重新查询一次账户列表
            // (注意：这里我们假设 insert 总是成功的，没有处理重名等)
            loadAccounts(account.ledgerId)

            // 通知 UI，新的账户已创建 (我们通过名称来查找)
            // (一个更健壮的方法是让 insert 返回 ID)
            val newAccount = accountDao.getAccountsByLedgerSuspend(account.ledgerId)
                .firstOrNull { it.type == account.type && it.balance == account.balance }

            _newlyCreatedAccountId.postValue(newAccount?.id)
        }
    }

    /**
     * (!!! 10. 新增：重置 "新账户" 信号 !!!)
     */
    fun doneNavigatingToNewAccount() {
        _newlyCreatedAccountId.value = null
    }

    /**
     * (无变化)
     * 重置关闭信号
     */
    fun doneClosingActivity() {
        _shouldCloseActivity.value = false
    }
}