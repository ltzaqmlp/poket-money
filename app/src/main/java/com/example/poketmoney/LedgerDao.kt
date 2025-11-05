package com.example.poketmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * LedgerDao (账本数据访问对象)
 */
@Dao
interface LedgerDao {

    // (无变化)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ledger: Ledger)

    // (无变化)
    @Update
    suspend fun update(ledger: Ledger)

    // (无变化)
    @Delete
    suspend fun delete(ledger: Ledger)

    // (无变化) 按 ID 倒序，确保最新的在最上面
    @Query("SELECT * FROM ledgers ORDER BY id DESC")
    fun getAllLedgers(): LiveData<List<Ledger>>

    // (无变化)
    @Query("SELECT * FROM ledgers WHERE name = :name LIMIT 1")
    suspend fun findLedgerByName(name: String): Ledger?

    // (无变化)
    @Query("SELECT * FROM ledgers ORDER BY id ASC LIMIT 1")
    suspend fun getFirstLedger(): Ledger?

    // (无变化)
    @Query("SELECT * FROM ledgers WHERE id = :id")
    fun getLedgerById(id: Long): LiveData<Ledger?>

    // --- (!!! 1. 新增查询 !!!) ---
    /**
     * (新增) 查询账本的总数。
     * 我们将用这个来判断是否为 "新用户" (count == 0)。
     * 返回 LiveData 是为了让 MainViewModel 能够观察这个计数值。
     */
    @Query("SELECT COUNT(id) FROM ledgers")
    fun getLedgerCount(): LiveData<Int>
}