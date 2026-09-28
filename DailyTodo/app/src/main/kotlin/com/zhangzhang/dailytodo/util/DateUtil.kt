// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/util/DateUtil.kt
package com.zhangzhang.dailytodo.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

/**
 * 日期工具：全流程用 "yyyy-MM-dd" 字符串流转，基于 java.time（minSdk 26 原生支持）。
 * 不用 SimpleDateFormat，避免时区和线程安全问题。
 */
object DateUtil {

    private val FMT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val WEEK_LABELS = arrayOf("一", "二", "三", "四", "五", "六", "日")

    fun today(): String = LocalDate.now().format(FMT)

    fun parse(value: String): LocalDate = LocalDate.parse(value, FMT)

    fun format(date: LocalDate): String = date.format(FMT)

    fun addDays(value: String, days: Long): String = parse(value).plusDays(days).format(FMT)

    /** b - a，单位天 */
    fun diffDays(a: String, b: String): Long = ChronoUnit.DAYS.between(parse(a), parse(b))

    /** "2026-09" */
    fun monthKey(value: String): String = value.substring(0, 7)

    /** ISO 周 key："2026-W40" */
    fun weekKey(value: String): String {
        val d = parse(value)
        val week = d.get(WeekFields.ISO.weekOfWeekBasedYear())
        val year = d.get(WeekFields.ISO.weekBasedYear())
        return year.toString() + "-W" + week.toString().padStart(2, '0')
    }

    /** 0 = 周一 … 6 = 周日 */
    fun weekdayIndex(value: String): Int = parse(value).dayOfWeek.value - 1

    fun weekdayLabel(value: String): String = "周" + WEEK_LABELS[weekdayIndex(value)]

    /** 所在周的周一 */
    fun weekStart(value: String): String = addDays(value, -weekdayIndex(value).toLong())

    /** 所在周七天的日期 */
    fun weekDates(value: String): List<String> {
        val start = weekStart(value)
        return (0..6).map { addDays(start, it.toLong()) }
    }

    /** 当月全部日期（1 号到最后一天） */
    fun monthDates(monthKey: String): List<String> {
        val year = monthKey.substring(0, 4).toInt()
        val month = monthKey.substring(5, 7).toInt()
        val last = LocalDate.of(year, month, 1).lengthOfMonth()
        return (1..last).map { day ->
            year.toString() + "-" + month.toString().padStart(2, '0') + "-" + day.toString().padStart(2, '0')
        }
    }

    /**
     * 月历网格：固定 6 行 × 7 列 = 42 格，从当月 1 号所在周的周一开始。
     * 返回的是日期字符串，是否为本月由调用方用 monthKey 判断。
     */
    fun monthGrid(monthKey: String): List<String> {
        val first = monthKey + "-01"
        val start = weekStart(first)
        return (0..41).map { addDays(start, it.toLong()) }
    }

    /** "9月28日 周一" */
    fun friendly(value: String): String {
        val d = parse(value)
        return "${d.monthValue}月${d.dayOfMonth}日 " + weekdayLabel(value)
    }

    /** "2026年9月" */
    fun monthLabel(monthKey: String): String {
        return monthKey.substring(0, 4).toInt().toString() + "年" + monthKey.substring(5, 7).toInt().toString() + "月"
    }

    /** 时间戳 -> "09:24" */
    fun timeLabel(millis: Long): String {
        if (millis <= 0L) return ""
        val d = java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis),
            java.time.ZoneId.systemDefault()
        )
        return d.hour.toString().padStart(2, '0') + ":" + d.minute.toString().padStart(2, '0')
    }

    fun isFuture(value: String): Boolean = diffDays(today(), value) > 0

    /** 从今天往前数 n 天（含今天），升序 */
    fun lastNDays(n: Int): List<String> {
        val t = today()
        return (n - 1 downTo 0).map { addDays(t, -it.toLong()) }
    }

    /** 月份游标加减：monthKey 前进/后退 step 个月 */
    fun shiftMonth(monthKey: String, step: Int): String {
        var year = monthKey.substring(0, 4).toInt()
        var month = monthKey.substring(5, 7).toInt() + step
        if (month < 1) {
            month = 12
            year -= 1
        } else if (month > 12) {
            month = 1
            year += 1
        }
        return year.toString() + "-" + month.toString().padStart(2, '0')
    }
}
