// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/db/AppDatabase.kt
package com.zhangzhang.dailytodo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zhangzhang.dailytodo.data.entity.GoalEntity
import com.zhangzhang.dailytodo.data.entity.SnapshotEntity
import com.zhangzhang.dailytodo.data.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, GoalEntity::class, SnapshotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun snapshotDao(): SnapshotDao
}

/**
 * 懒加载单例：第一次真正用到时才建库。
 * 刻意不在 Application.onCreate 里初始化 —— 启动阶段不做任何 IO。
 * Room 默认使用系统自带 SQLite，不会额外打包 .so。
 */
object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "daily_todo.db"
            ).build().also { instance = it }
        }
    }
}
