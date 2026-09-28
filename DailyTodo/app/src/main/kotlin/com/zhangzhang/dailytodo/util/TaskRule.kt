// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/util/TaskRule.kt
package com.zhangzhang.dailytodo.util

import com.zhangzhang.dailytodo.data.entity.TaskEntity

/**
 * 任务规则：纯函数，不碰数据库，方便单测与复用。
 * scope 目前固定为 "day"（日任务），保留字段以便将来扩展周/月任务。
 */
object TaskRule {

    /** 某一天的任务，按 order 再按创建时间排序，保证顺序稳定 */
    fun byDate(tasks: List<TaskEntity>, date: String): List<TaskEntity> =
        tasks.filter { it.scope == "day" && it.date == date }
            .sortedWith(compareBy({ it.order }, { it.createdAt }))

    /** 同一天内的下一个排序号 */
    fun nextOrder(tasks: List<TaskEntity>, date: String): Int {
        val max = tasks.filter { it.date == date }.maxOfOrNull { it.order }
        return (max ?: -1) + 1
    }

    /** 未完成数量 */
    fun undoneCount(tasks: List<TaskEntity>): Int = tasks.count { !it.done }

    /** 已完成数量 */
    fun doneCount(tasks: List<TaskEntity>): Int = tasks.count { it.done }

    /** 顺延：把当天未完成的任务整体挪到目标日期，返回新列表与条数 */
    fun rollover(tasks: List<TaskEntity>, from: String, to: String): Pair<List<TaskEntity>, Int> {
        var count = 0
        val out = tasks.map { t ->
            if (t.scope == "day" && t.date == from && !t.done) {
                count++
                t.copy(date = to)
            } else {
                t
            }
        }
        return out to count
    }

    /** 列表内移动一项，from -> to */
    fun move(list: List<TaskEntity>, from: Int, to: Int): List<TaskEntity> {
        if (from == to) return list
        if (from !in list.indices || to !in list.indices) return list
        val mutable = list.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        return mutable
    }
}
