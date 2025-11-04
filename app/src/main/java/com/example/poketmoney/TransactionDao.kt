package com.example.poketmoney

import androidx.room.*
import androidx.lifecycle.LiveData

/**
 * !!! 数据库升级 !!!
 * 这是一个非常重要的修改。
 * 我们需要修改此文件中的 *几乎每一个 @Query*，
 * 为它们添加一个 `ledgerId: Long` 参数，
 * 并在 SQL 语句中添加 `WHERE ledgerId = :ledgerId` 的过滤条件。
 */
@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // (无变化) 按 ID 查询是全局的
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    // --- 以下所有查询都已修改 ---

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT * FROM transactions WHERE ledgerId = :ledgerId ORDER BY date DESC")
    fun getAllTransactions(ledgerId: Long): LiveData<List<Transaction>>

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT * FROM transactions WHERE ledgerId = :ledgerId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRangeLiveData(ledgerId: Long, startDate: Long, endDate: Long): LiveData<List<Transaction>>

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT * FROM transactions WHERE ledgerId = :ledgerId AND type = :type ORDER BY date DESC")
    suspend fun getTransactionsByType(ledgerId: Long, type: String): List<Transaction>

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT SUM(amount) FROM transactions WHERE ledgerId = :ledgerId AND type = 'income' AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalIncomeByDateRange(ledgerId: Long, startDate: Long, endDate: Long): Double?

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT SUM(amount) FROM transactions WHERE ledgerId = :ledgerId AND type = 'expense' AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpenseByDateRange(ledgerId: Long, startDate: Long, endDate: Long): Double?

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT SUM(amount) FROM transactions WHERE ledgerId = :ledgerId AND type = 'income'")
    suspend fun getTotalIncome(ledgerId: Long): Double?

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT SUM(amount) FROM transactions WHERE ledgerId = :ledgerId AND type = 'expense'")
    suspend fun getTotalExpense(ledgerId: Long): Double?

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT * FROM transactions WHERE ledgerId = :ledgerId ORDER BY date DESC")
    fun getAllTransactionsLiveData(ledgerId: Long): LiveData<List<Transaction>>

    // --- 柱状图查询 (已修改) ---

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT strftime('%m-%d', date / 1000, 'unixepoch') as dayLabel, SUM(amount) as totalAmount FROM transactions " +
            "WHERE ledgerId = :ledgerId AND type = 'income' AND date >= :startDate AND date <= :endDate " +
            "GROUP BY dayLabel " +
            "ORDER BY dayLabel ASC")
    suspend fun getDailyIncomeSummary(ledgerId: Long, startDate: Long, endDate: Long): List<DailySummary>

    // (已修改) 新增 ledgerId 参数
    @Query("SELECT strftime('%m-%d', date / 1000, 'unixepoch') as dayLabel, SUM(amount) as totalAmount FROM transactions " +
            "WHERE ledgerId = :ledgerId AND type = 'expense' AND date >= :startDate AND date <= :endDate " +
            "GROUP BY dayLabel " +
            "ORDER BY dayLabel ASC")
    suspend fun getDailyExpenseSummary(ledgerId: Long, startDate: Long, endDate: Long): List<DailySummary>

    // --- (新增查询) ---
    /**
     * 新增：计算一个特定账本下有多少条交易记录。
     * 这在 "账本管理" 页面删除账本时很有用，
     * 我们可以用它来提示用户 "该账本不为空，是否继续删除？"
     */
    @Query("SELECT COUNT(id) FROM transactions WHERE ledgerId = :ledgerId")
    suspend fun countTransactionsForLedger(ledgerId: Long): Int
}