package com.example.poketmoney

import androidx.room.*
import androidx.lifecycle.LiveData

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    // 查询特定日期范围内的记录
    // 新增：返回 LiveData 的版本，用于在 switchMap 中使用
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRangeLiveData(startDate: Long, endDate: Long): LiveData<List<Transaction>>

    // 查询特定类型（收入/支出）的记录
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    suspend fun getTransactionsByType(type: String): List<Transaction>

    // 统计特定日期范围内的总收入
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income' AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Double?

    // 统计特定日期范围内的总支出
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Double?

    // 统计所有记录的总收入
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income'")
    suspend fun getTotalIncome(): Double?

    // 统计所有记录的总支出
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense'")
    suspend fun getTotalExpense(): Double?

    //处理“无时间范围”的情况
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsLiveData(): LiveData<List<Transaction>>

    // --- 柱状图新查询 (替换饼图查询) ---

    // 步骤 6 新增：按天统计指定日期范围内的总收入
    @Query("SELECT strftime('%m-%d', date / 1000, 'unixepoch') as dayLabel, SUM(amount) as totalAmount FROM transactions " +
            "WHERE type = 'income' AND date >= :startDate AND date <= :endDate " +
            "GROUP BY dayLabel " +
            "ORDER BY dayLabel ASC") // 按日期标签升序排序
    suspend fun getDailyIncomeSummary(startDate: Long, endDate: Long): List<DailySummary>

    // 步骤 6 新增：按天统计指定日期范围内的总支出
    @Query("SELECT strftime('%m-%d', date / 1000, 'unixepoch') as dayLabel, SUM(amount) as totalAmount FROM transactions " +
            "WHERE type = 'expense' AND date >= :startDate AND date <= :endDate " +
            "GROUP BY dayLabel " +
            "ORDER BY dayLabel ASC") // 按日期标签升序排序
    suspend fun getDailyExpenseSummary(startDate: Long, endDate: Long): List<DailySummary>
}

