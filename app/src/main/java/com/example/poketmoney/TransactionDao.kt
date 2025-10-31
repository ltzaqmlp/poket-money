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
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction>

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
}