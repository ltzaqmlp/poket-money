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
 * 这个接口定义了所有与 "accounts" (账户) 表相关的数据库操作。
 * Room 库会自动为我们实现这些方法。
 */
@Dao
interface AccountDao {

    /**
     * 插入一个新账户。
     * OnConflictStrategy.REPLACE：如果 ID 冲突，则替换 (虽然在这里 ID 是自动生成的)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    /**
     * 更新一个现有的账户 (例如，修改余额或备注)。
     */
    @Update
    suspend fun update(account: Account)

    /**
     * 删除一个账户。
     */
    @Delete
    suspend fun delete(account: Account)

    /**
     * 查询所有的账户。
     * 我们按账户类别 (type) 升序排序，然后再按余额 (balance) 降序排序。
     * 返回 LiveData，以便在账户管理页面中自动刷新UI。
     */
    @Query("SELECT * FROM accounts ORDER BY type ASC, balance DESC")
    fun getAllAccounts(): LiveData<List<Account>>

    /**
     * 根据 ID 查询单个账户。
     * (这在 "编辑账户" 时会用到)
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    // (未来可能需要：获取所有账户的总余额等，但现在我们先保持简单)
}