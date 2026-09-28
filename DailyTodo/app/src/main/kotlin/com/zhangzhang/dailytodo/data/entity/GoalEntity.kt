// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/entity/GoalEntity.kt
package com.zhangzhang.dailytodo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 周目标 / 月目标表。
 * key 的取值：周 -> "2026-W40"，月 -> "2026-09"。
 * 同样因 key / order 是 SQL 关键字，列名分别改为 goal_key / sort_order。
 */
@Entity(tableName = "goals", indices = [Index(value = ["goal_key"])])
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_key") val key: String,
    val title: String,
    val done: Boolean = false,
    val doneAt: Long? = null,
    @ColumnInfo(name = "sort_order") val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
