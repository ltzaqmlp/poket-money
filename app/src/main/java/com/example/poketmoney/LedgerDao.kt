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

    // (已修复Bug) 按 ID 倒序，确保最新的在最上面
    @Query("SELECT * FROM ledgers ORDER BY id DESC")
    fun getAllLedgers(): LiveData<List<Ledger>>

    // (无变化)
    @Query("SELECT * FROM ledgers WHERE name = :name LIMIT 1")
    suspend fun findLedgerByName(name: String): Ledger?

    // (无变化)
    @Query("SELECT * FROM ledgers ORDER BY id ASC LIMIT 1")
    suspend fun getFirstLedger(): Ledger?

    // --- (!!! 1. 新增查询 !!!) ---
    /**
     * 根据 ID 查询单个账本。
     * 我们返回 LiveData，以便 MainViewModel 可以观察它。
     * @param id 要查询的账本 ID
     * @return 返回一个包含该账本的 LiveData (可能为 null)
     */
    @Query("SELECT * FROM ledgers WHERE id = :id")
    fun getLedgerById(id: Long): LiveData<Ledger?>
}