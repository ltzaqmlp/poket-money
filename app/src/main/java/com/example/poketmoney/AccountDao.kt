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
<<<<<<< HEAD
 * (!!! 已修改：`insert` 返回 Long, `getAllAccounts` 按 ledgerId 过滤 !!!)
=======
 * (!!! 已更新：排序方式改为 ID 倒序 !!!)
 *
 * 这个接口定义了所有与 "accounts" (账户) 表相关的数据库操作。
 * Room 库会自动为我们实现这些方法。
>>>>>>> temp-branch
 */
@Dao
interface AccountDao {

    /**
<<<<<<< HEAD
     * 插入一个新账户。
     * (!!! 修改：现在返回新插入账户的 ID (Long) !!!)
     * (这是为了 "选择账户" 弹窗中的 "新增" 功能)
=======
     * (无变化) 插入一个新账户。
     * OnConflictStrategy.REPLACE：如果 ID 冲突，则替换
>>>>>>> temp-branch
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    /**
<<<<<<< HEAD
     * 更新一个现有的账户。
=======
     * (无变化) 更新一个现有的账户 (例如，修改余额或备注)。
>>>>>>> temp-branch
     */
    @Update
    suspend fun update(account: Account)

    /**
     * (无变化) 删除一个账户。
     */
    @Delete
    suspend fun delete(account: Account)

    /**
<<<<<<< HEAD
     * (!!! 修改：新增 ledgerId 参数 !!!)
     * 查询 *特定账本* (ledgerId) 的所有账户。
     * 返回 LiveData，以便在账户管理页面中自动刷新UI。
     */
    @Query("SELECT * FROM accounts WHERE ledgerId = :ledgerId ORDER BY type ASC, balance DESC")
    fun getAllAccounts(ledgerId: Long): LiveData<List<Account>>
=======
     * (!!! 关键修改：排序方式变更 !!!)
     * 查询 *特定账本* (ledgerId) 的所有账户。
     * (!!! ORDER BY id DESC：让最新创建的显示在最上面 !!!)
     *
     * @param ledgerId 必须传入当前激活的账本ID。
     */
    @Query("SELECT * FROM accounts WHERE ledgerId = :ledgerId ORDER BY id DESC")
    fun getAllAccountsByLedger(ledgerId: Long): LiveData<List<Account>>
>>>>>>> temp-branch

    /**
     * (无变化)
     * 根据 ID 查询单个账户。
<<<<<<< HEAD
     * (此查询保持不变，因为 ID 是全局唯一的)
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?
=======
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
>>>>>>> temp-branch
}