// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/repo/TodoRepository.kt
package com.zhangzhang.dailytodo.data.repo

import android.content.Context
import com.zhangzhang.dailytodo.data.backup.BackupCodec
import com.zhangzhang.dailytodo.data.db.DatabaseProvider
import com.zhangzhang.dailytodo.data.entity.GoalEntity
import com.zhangzhang.dailytodo.data.entity.SnapshotEntity
import com.zhangzhang.dailytodo.data.entity.TaskEntity
import com.zhangzhang.dailytodo.util.DateUtil
import com.zhangzhang.dailytodo.util.TaskRule
import kotlinx.coroutines.flow.Flow

/**
 * 唯一的数据入口：ViewModel 只认它，Composable 一律不直接操作数据库。
 * 所有 Flow 查询由 Room 在 IO 线程执行，返回的数据变化会自动推送。
 */
class TodoRepository(context: Context) {

    private val db = DatabaseProvider.get(context)

    val tasks: Flow<List<TaskEntity>> = db.taskDao().observeAll()
    val goals: Flow<List<GoalEntity>> = db.goalDao().observeAll()
    val snapshots: Flow<List<SnapshotEntity>> = db.snapshotDao().observeAll()

    /* ==================== 任务 ==================== */

    suspend fun addTask(title: String, date: String, priority: Int = 0, note: String = "") {
        val order = TaskRule.nextOrder(db.taskDao().snapshotAll(), date)
        db.taskDao().insert(
            TaskEntity(
                title = title.trim(),
                date = date,
                scope = "day",
                priority = priority,
                note = note.trim(),
                order = order,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTask(task: TaskEntity) {
        db.taskDao().update(task)
    }

    suspend fun toggleTask(task: TaskEntity) {
        db.taskDao().update(
            if (task.done) {
                task.copy(done = false, doneAt = null)
            } else {
                task.copy(done = true, doneAt = System.currentTimeMillis())
            }
        )
    }

    suspend fun deleteTask(id: Long) {
        db.taskDao().delete(id)
    }

    /** 单条推到明天 */
    suspend fun postponeTask(id: Long, toDate: String) {
        db.taskDao().updateDate(id, toDate)
    }

    /** 一键顺延：返回顺延条数 */
    suspend fun rolloverUndone(from: String, to: String): Int {
        val ids = db.taskDao().idsUndoneOn(from)
        if (ids.isEmpty()) return 0
        db.taskDao().updateDateBatch(ids, to)
        return ids.size
    }

    /** 拖拽排序后按新顺序落库 */
    suspend fun reorderDay(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            db.taskDao().updateOrder(id, index)
        }
    }

    suspend fun clearTasks() {
        db.taskDao().clear()
    }

    /* ==================== 目标 ==================== */

    /** 返回 false 表示已达 5 条上限 */
    suspend fun addGoal(key: String, title: String): Boolean {
        if (db.goalDao().countByKey(key) >= GOAL_MAX) return false
        db.goalDao().insert(
            GoalEntity(
                key = key,
                title = title.trim(),
                order = db.goalDao().countByKey(key)
            )
        )
        return true
    }

    suspend fun toggleGoal(goal: GoalEntity) {
        db.goalDao().update(
            if (goal.done) {
                goal.copy(done = false, doneAt = null)
            } else {
                goal.copy(done = true, doneAt = System.currentTimeMillis())
            }
        )
    }

    suspend fun deleteGoal(id: Long) {
        db.goalDao().delete(id)
    }

    suspend fun clearGoals() {
        db.goalDao().clear()
    }

    /* ==================== 快照 ==================== */

    /** 每天首次打开时调用：同一天只保留一份最新 */
    suspend fun ensureDailySnapshot() {
        val today = DateUtil.today()
        if (db.snapshotDao().countByDate(today) > 0) return
        makeSnapshot("auto")
    }

    suspend fun makeSnapshot(from: String) {
        db.snapshotDao().insert(
            SnapshotEntity(
                date = DateUtil.today(),
                from = from,
                createdAt = System.currentTimeMillis(),
                payload = BackupCodec.export(currentTasks(), currentGoals())
            )
        )
        db.snapshotDao().keepOnly7()
    }

    suspend fun restoreSnapshot(id: Long): Boolean {
        val snap = db.snapshotDao().getById(id) ?: return false
        val payload = BackupCodec.parse(snap.payload) ?: return false
        db.taskDao().clear()
        db.goalDao().clear()
        payload.tasks.forEach { db.taskDao().insert(it) }
        payload.goals.forEach { db.goalDao().insert(it) }
        return true
    }

    suspend fun deleteSnapshot(id: Long) {
        db.snapshotDao().delete(id)
    }

    /* ==================== 导出 / 导入 / 清空 ==================== */

    suspend fun exportJson(): String = BackupCodec.export(currentTasks(), currentGoals())

    /** 覆盖式导入；返回 null 表示文本非法 */
    suspend fun importJson(text: String): Boolean {
        val payload = BackupCodec.parse(text) ?: return false
        makeSnapshot("manual") // 导入前先留一份当前数据
        db.taskDao().clear()
        db.goalDao().clear()
        payload.tasks.forEach { db.taskDao().insert(it) }
        payload.goals.forEach { db.goalDao().insert(it) }
        return true
    }

    suspend fun clearAll() {
        makeSnapshot("manual")
        db.taskDao().clear()
        db.goalDao().clear()
    }

    /* ==================== 内部辅助 ==================== */

    private suspend fun currentTasks(): List<TaskEntity> = db.taskDao().snapshotAll()

    private suspend fun currentGoals(): List<GoalEntity> = db.goalDao().snapshotAll()

    companion object {
        const val GOAL_MAX = 5
    }
}
