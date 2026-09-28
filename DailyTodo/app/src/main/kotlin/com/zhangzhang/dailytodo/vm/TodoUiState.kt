// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/vm/TodoUiState.kt
package com.zhangzhang.dailytodo.vm

import com.zhangzhang.dailytodo.data.entity.GoalEntity
import com.zhangzhang.dailytodo.data.entity.SnapshotEntity
import com.zhangzhang.dailytodo.data.entity.TaskEntity

/**
 * 界面状态：不可变 data class。
 * 三个 tab 共用同一份 tasks，因此今日打勾后，周完成率与月历圆点会自动同步刷新。
 */
data class TodoUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val snapshots: List<SnapshotEntity> = emptyList(),
    /** 当前选中日期 "yyyy-MM-dd"（今日/周/月三个视图共用） */
    val selectedDate: String = "",
    /** 月视图当前显示的月份 "yyyy-MM" */
    val monthCursor: String = "",
    /** 正在编辑的任务，非空时弹出编辑层 */
    val editorTask: TaskEntity? = null
)
