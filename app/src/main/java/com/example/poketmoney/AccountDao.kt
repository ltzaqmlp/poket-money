package com.example.poketmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * AccountDao (账户数据访问对象)
 *
 * (!!! 已更新：排序方式改为 ID 倒序 !!!)
 *
 * 这个接口定义了所有与 "accounts" (账户) 表相关的数据库操作。
 * Room 库会自动为我们实现这些方法。
 */
@Dao
interface AccountDao {

    /**
     * (无变化) 插入一个新账户。
     * OnConflictStrategy.REPLACE：如果 ID 冲突，则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    /**
     * (无变化) 更新一个现有的账户 (例如，修改余额或备注)。
     */
    @Update
    suspend fun update(account: Account)

    /**
     * (无变化) 删除一个账户。
     */
    @Delete
    suspend fun delete(account: Account)

    /**
     * (!!! 关键修改：排序方式变更 !!!)
     * 查询 *特定账本* (ledgerId) 的所有账户。
     * (!!! ORDER BY id DESC：让最新创建的显示在最上面 !!!)
     *
     * @param ledgerId 必须传入当前激活的账本ID。
     */
    @Query("SELECT * FROM accounts WHERE ledgerId = :ledgerId ORDER BY id DESC")
    fun getAllAccountsByLedger(ledgerId: Long): LiveData<List<Account>>

    /**
     * (无变化)
     * 根据 ID 查询单个账户。
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    /**
     * (!!! 关键修改：排序方式变更 !!!)
     * (新增) 查询特定账本下的所有账户 (非 LiveData 版本)
     * (!!! ORDER BY id DESC：让最新创建的显示在最上面 !!!)
     *
     * @param ledgerId 必须传入当前激活的账本ID。
     */
    @Query("SELECT * FROM accounts WHERE ledgerId = :ledgerId ORDER BY id DESC")
    suspend fun getAccountsByLedgerSuspend(ledgerId: Long): List<Account>

    // --- (!!! 2. 新增查询：同步余额的核心 !!!) ---
    /**
     * (无变化) 更新一个账户的余额。
     *
     * @param accountId 要更新的账户ID
     * @param amount 要 (增加/减少) 的金额
     */
    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: Long, amount: Double)
}