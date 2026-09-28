// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/entity/SnapshotEntity.kt
package com.zhangzhang.dailytodo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日快照表：payload 存整份数据的 JSON 字符串。
 * 最多保留最近 7 份，超出由 DAO 自动清理最旧的。
 */
@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val from: String = "auto",     // "auto" 每日自动 / "manual" 手动
    val createdAt: Long = System.currentTimeMillis(),
    val payload: String = ""
)
