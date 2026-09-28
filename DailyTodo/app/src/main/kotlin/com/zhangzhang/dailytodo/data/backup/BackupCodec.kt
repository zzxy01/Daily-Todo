// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/data/backup/BackupCodec.kt
package com.zhangzhang.dailytodo.data.backup

import com.zhangzhang.dailytodo.data.entity.GoalEntity
import com.zhangzhang.dailytodo.data.entity.TaskEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * 备份文件的编解码。
 * 使用系统自带的 org.json（Android 框架自带，零依赖、零体积），
 * 不引入 Gson / Moshi / kotlinx.serialization 等第三方库。
 *
 * 导入是覆盖式的：id 不保留（重新自增），避免与现有数据主键冲突。
 */
object BackupCodec {

    private const val VERSION = 1

    fun export(tasks: List<TaskEntity>, goals: List<GoalEntity>): String {
        val root = JSONObject()
        root.put("app", "daily-todo")
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val taskArray = JSONArray()
        for (t in tasks) {
            val o = JSONObject()
            o.put("title", t.title)
            o.put("date", t.date)
            o.put("scope", t.scope)
            o.put("done", t.done)
            o.put("doneAt", t.doneAt ?: 0L)
            o.put("priority", t.priority)
            o.put("note", t.note)
            o.put("order", t.order)
            o.put("createdAt", t.createdAt)
            taskArray.put(o)
        }
        root.put("tasks", taskArray)

        val goalArray = JSONArray()
        for (g in goals) {
            val o = JSONObject()
            o.put("key", g.key)
            o.put("title", g.title)
            o.put("done", g.done)
            o.put("doneAt", g.doneAt ?: 0L)
            o.put("order", g.order)
            o.put("createdAt", g.createdAt)
            goalArray.put(o)
        }
        root.put("goals", goalArray)

        return root.toString(2)
    }

    data class Payload(val tasks: List<TaskEntity>, val goals: List<GoalEntity>)

    /**
     * 解析备份文本。失败返回 null（调用方据此提示用户）。
     * 做了基本的结构校验，避免把任意 JSON 灌进数据库。
     */
    fun parse(text: String): Payload? {
        return try {
            val root = JSONObject(text)
            if (!root.has("tasks")) return null
            val taskArray = root.getJSONArray("tasks")
            val tasks = ArrayList<TaskEntity>(taskArray.length())
            for (i in 0 until taskArray.length()) {
                val o = taskArray.getJSONObject(i)
                val doneAt = o.optLong("doneAt", 0L)
                tasks.add(
                    TaskEntity(
                        id = 0, // 重新分配主键
                        title = o.optString("title", ""),
                        date = o.optString("date", ""),
                        scope = o.optString("scope", "day"),
                        done = o.optBoolean("done", false),
                        doneAt = if (doneAt > 0L) doneAt else null,
                        priority = o.optInt("priority", 0),
                        note = o.optString("note", ""),
                        order = o.optInt("order", 0),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val goalArray = root.optJSONArray("goals")
            val goals = ArrayList<GoalEntity>()
            if (goalArray != null) {
                for (i in 0 until goalArray.length()) {
                    val o = goalArray.getJSONObject(i)
                    val doneAt = o.optLong("doneAt", 0L)
                    goals.add(
                        GoalEntity(
                            id = 0,
                            key = o.optString("key", ""),
                            title = o.optString("title", ""),
                            done = o.optBoolean("done", false),
                            doneAt = if (doneAt > 0L) doneAt else null,
                            order = o.optInt("order", 0),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            Payload(tasks, goals)
        } catch (e: Exception) {
            null
        }
    }
}
