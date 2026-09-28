// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/db/GoalDao.kt
package com.zhangzhang.dailytodo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zhangzhang.dailytodo.data.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/** 周目标 / 月目标 DAO */
@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY goal_key ASC, sort_order ASC, createdAt ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    /** 一次性取全量（导出/快照用，不走 Flow） */
    @Query("SELECT * FROM goals ORDER BY goal_key ASC, sort_order ASC, createdAt ASC")
    suspend fun snapshotAll(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE goal_key = :key ORDER BY sort_order ASC, createdAt ASC")
    fun observeByKey(key: String): Flow<List<GoalEntity>>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM goals")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM goals WHERE goal_key = :key")
    suspend fun countByKey(key: String): Int
}
