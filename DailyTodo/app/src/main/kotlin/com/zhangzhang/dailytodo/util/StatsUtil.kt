// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/util/StatsUtil.kt
package com.zhangzhang.dailytodo.util

import com.zhangzhang.dailytodo.data.entity.TaskEntity

/**
 * 统计计算：完成率、热力等级、连续打卡、断更日、30 天趋势。
 * 全部为纯函数，输入任务列表，输出可直接渲染的结构。
 */
object StatsUtil {

    data class DayCount(val total: Int, val done: Int)

    data class Summary(val total: Int, val done: Int, val percent: Int)

    data class DayPoint(val date: String, val day: String, val total: Int, val done: Int, val height: Float)

    /** 按日期聚合 */
    fun countMap(tasks: List<TaskEntity>): Map<String, DayCount> {
        val map = LinkedHashMap<String, DayCount>()
        for (t in tasks) {
            if (t.scope != "day" || t.date.isBlank()) continue
            val cur = map[t.date]
            if (cur == null) {
                map[t.date] = DayCount(1, if (t.done) 1 else 0)
            } else {
                map[t.date] = DayCount(cur.total + 1, cur.done + (if (t.done) 1 else 0))
            }
        }
        return map
    }

    fun percent(done: Int, total: Int): Int = if (total <= 0) 0 else (done * 100 / total)

    /**
     * 热力等级 0-4：
     * 0 无任务 / 1 完成 <34% / 2 <67% / 3 <100% / 4 全部完成
     */
    fun heatLevel(map: Map<String, DayCount>, date: String): Int {
        val c = map[date] ?: return 0
        if (c.total <= 0) return 0
        return when {
            c.done >= c.total -> 4
            c.done * 100 / c.total >= 67 -> 3
            c.done * 100 / c.total >= 34 -> 2
            else -> 1
        }
    }

    /** 给一组日期补上完成度热力（月历/周条用） */
    fun heatList(tasks: List<TaskEntity>, dates: List<String>): List<Int> {
        val map = countMap(tasks)
        return dates.map { heatLevel(map, it) }
    }

    /** 一组日期上的汇总 */
    fun summary(tasks: List<TaskEntity>, dates: List<String>): Summary {
        var total = 0
        var done = 0
        val set = dates.toSet()
        for (t in tasks) {
            if (t.scope != "day") continue
            if (set.contains(t.date)) {
                total++
                if (t.done) done++
            }
        }
        return Summary(total, done, percent(done, total))
    }

    /**
     * 连续打卡天数：连续"当天至少完成 1 项"的天数。
     * 今天还没完成任何任务不算断（从昨天起算），避免早上打开就显示 0。
     */
    fun streakDays(tasks: List<TaskEntity>): Int {
        val map = countMap(tasks)
        val today = DateUtil.today()
        var cursor = today
        val todayDone = map[today]?.done ?: 0
        if (todayDone <= 0) cursor = DateUtil.addDays(today, -1)

        var streak = 0
        for (i in 0..3650) {
            val c = map[cursor]
            if (c != null && c.done > 0) {
                streak++
                cursor = DateUtil.addDays(cursor, -1)
            } else {
                break
            }
        }
        return streak
    }

    /** 断更日：有任务但 0 完成，或压根没记录；只统计到今天，未来不算 */
    fun missedDays(tasks: List<TaskEntity>, dates: List<String>): List<String> {
        val map = countMap(tasks)
        val today = DateUtil.today()
        return dates.filter { d ->
            if (DateUtil.diffDays(d, today) < 0) return@filter false
            val c = map[d]
            c == null || c.done <= 0
        }
    }

    /** 最近 n 天趋势，height 为 0-100 的柱高百分比 */
    fun lastNDaysSeries(tasks: List<TaskEntity>, n: Int): List<DayPoint> {
        val map = countMap(tasks)
        val dates = DateUtil.lastNDays(n)
        var max = 1
        for (d in dates) {
            val c = map[d]
            if (c != null && c.total > max) max = c.total
        }
        return dates.map { d ->
            val c = map[d]
            val total = c?.total ?: 0
            val done = c?.done ?: 0
            DayPoint(
                date = d,
                day = d.substring(8),
                total = total,
                done = done,
                height = (done.toFloat() / max.toFloat() * 100f).coerceAtLeast(4f)
            )
        }
    }

    /** 累计完成总数 */
    fun totalDone(tasks: List<TaskEntity>): Int = tasks.count { it.done }
}
