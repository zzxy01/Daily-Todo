// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/db/TaskDao.kt
package com.zhangzhang.dailytodo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zhangzhang.dailytodo.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/** 任务表的所有 SQL 都集中在这里，Composable 一律不直接碰数据库 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY date ASC, sort_order ASC, createdAt ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    /** 一次性取全量（导出/快照用，不走 Flow） */
    @Query("SELECT * FROM tasks ORDER BY date ASC, sort_order ASC, createdAt ASC")
    suspend fun snapshotAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY sort_order ASC, createdAt ASC")
    fun observeByDate(date: String): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun clear()

    /** 单条顺延到某天 */
    @Query("UPDATE tasks SET date = :date WHERE id = :id")
    suspend fun updateDate(id: Long, date: String)

    /** 一键顺延：批量改日期 */
    @Query("UPDATE tasks SET date = :date WHERE id IN (:ids)")
    suspend fun updateDateBatch(ids: List<Long>, date: String)

    @Query("SELECT id FROM tasks WHERE date = :date AND done = 0")
    suspend fun idsUndoneOn(date: String): List<Long>

    /** 拖拽排序结束后重写某一整天的顺序 */
    @Query("UPDATE tasks SET sort_order = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)
}
