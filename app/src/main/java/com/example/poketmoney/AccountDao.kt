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
 * (!!! 已修改：`insert` 返回 Long, `getAllAccounts` 按 ledgerId 过滤 !!!)
 */
@Dao
interface AccountDao {

    /**
     * 插入一个新账户。
     * (!!! 修改：现在返回新插入账户的 ID (Long) !!!)
     * (这是为了 "选择账户" 弹窗中的 "新增" 功能)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    /**
     * 更新一个现有的账户。
     */
    @Update
    suspend fun update(account: Account)

    /**
     * 删除一个账户。
     */
    @Delete
    suspend fun delete(account: Account)

    /**
     * (!!! 修改：新增 ledgerId 参数 !!!)
     * 查询 *特定账本* (ledgerId) 的所有账户。
     * 返回 LiveData，以便在账户管理页面中自动刷新UI。
     */
    @Query("SELECT * FROM accounts WHERE ledgerId = :ledgerId ORDER BY type ASC, balance DESC")
    fun getAllAccounts(ledgerId: Long): LiveData<List<Account>>

    /**
     * 根据 ID 查询单个账户。
     * (此查询保持不变，因为 ID 是全局唯一的)
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?
}