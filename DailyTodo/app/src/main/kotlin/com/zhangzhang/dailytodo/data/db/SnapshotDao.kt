// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/db/SnapshotDao.kt
package com.zhangzhang.dailytodo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zhangzhang.dailytodo.data.entity.SnapshotEntity
import kotlinx.coroutines.flow.Flow

/** 快照 DAO：写入后统一调用 keepOnly7() 清理超限的旧快照 */
@Dao
interface SnapshotDao {

    @Query("SELECT * FROM snapshots ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SnapshotEntity>>

    @Insert
    suspend fun insert(snapshot: SnapshotEntity): Long

    @Query("DELETE FROM snapshots WHERE id = :id")
    suspend fun delete(id: Long)

    /** 只保留最近 7 份，其余（最旧的）删除 */
    @Query("DELETE FROM snapshots WHERE id NOT IN (SELECT id FROM snapshots ORDER BY createdAt DESC LIMIT 7)")
    suspend fun keepOnly7()

    @Query("SELECT COUNT(*) FROM snapshots WHERE date = :date")
    suspend fun countByDate(date: String): Int

    @Query("SELECT * FROM snapshots WHERE id = :id")
    suspend fun getById(id: Long): SnapshotEntity?
}
