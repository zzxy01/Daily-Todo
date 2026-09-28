// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/entity/TaskEntity.kt
package com.zhangzhang.dailytodo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 任务表。date 用 "yyyy-MM-dd" 字符串存储，方便按天查询与排序。
 * 注意：order 是 SQL 关键字，列名改为 sort_order，Kotlin 侧仍叫 order。
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,
    val scope: String = "day",
    val done: Boolean = false,
    val doneAt: Long? = null,
    val priority: Int = 0,          // 0 普通 / 1 重要 / 2 紧急
    val note: String = "",
    @ColumnInfo(name = "sort_order") val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
