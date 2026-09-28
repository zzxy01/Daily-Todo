// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/screens/WeekScreen.kt
package com.zhangzhang.dailytodo.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangzhang.dailytodo.ui.components.EmptyState
import com.zhangzhang.dailytodo.ui.components.GoalPanel
import com.zhangzhang.dailytodo.ui.components.ProgressBar
import com.zhangzhang.dailytodo.ui.components.SwipeTaskItem
import com.zhangzhang.dailytodo.ui.components.WeekDayStrip
import com.zhangzhang.dailytodo.ui.theme.isDarkThemeNow
import com.zhangzhang.dailytodo.util.DateUtil
import com.zhangzhang.dailytodo.util.HapticUtil
import com.zhangzhang.dailytodo.util.StatsUtil
import com.zhangzhang.dailytodo.util.TaskRule
import com.zhangzhang.dailytodo.vm.TodoViewModel

/**
 * 周计划：本周回顾 + 本周目标 + 七天切换 + 选中日的任务列表。
 * 这里的列表不开启拖拽排序（order 只对当日清单有意义），但保留左滑删除。
 */
@Composable
fun WeekScreen(
    vm: TodoViewModel,
    themeMode: com.zhangzhang.dailytodo.ui.theme.ThemeMode
) {
    val uiState by vm.uiState.collectAsState()
    val context: Context = LocalContext.current
    val dark = isDarkThemeNow(themeMode)

    val selectedDate = uiState.selectedDate
    val weekDates = remember(selectedDate) { DateUtil.weekDates(selectedDate) }
    val weekGoals = remember(uiState.goals, selectedDate) {
        val key = DateUtil.weekKey(selectedDate)
        uiState.goals.filter { it.key == key }
    }
    val dayTasks = remember(uiState.tasks, selectedDate) {
        TaskRule.byDate(uiState.tasks, selectedDate)
    }
    val weekSummary = remember(uiState.tasks, selectedDate) {
        StatsUtil.summary(uiState.tasks, weekDates)
    }
    val streak = remember(uiState.tasks) { StatsUtil.streakDays(uiState.tasks) }
    val heats = remember(uiState.tasks, selectedDate) {
        StatsUtil.heatList(uiState.tasks, weekDates)
    }

    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------- 本周回顾 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本周回顾",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = weekDates.first().substring(5).replace('-', '/') + " - " +
                            weekDates.last().substring(5).replace('-', '/'),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(modifier = Modifier.padding(top = 16.dp)) {
                    StatBlock(
                        value = weekSummary.done.toString(),
                        label = "完成任务",
                        modifier = Modifier.weight(1f)
                    )
                    StatBlock(
                        value = weekSummary.percent.toString(),
                        unit = "%",
                        label = "完成率",
                        modifier = Modifier.weight(1f)
                    )
                    StatBlock(
                        value = streak.toString(),
                        unit = "天",
                        label = "连续打卡",
                        modifier = Modifier.weight(1f)
                    )
                }

                ProgressBar(
                    percent = weekSummary.percent,
                    modifier = Modifier.padding(top = 16.dp),
                    height = 10.dp
                )
            }
        }

        /* ---------- 本周目标 ---------- */
        GoalPanel(
            title = "本周目标",
            goals = weekGoals,
            placeholder = "例如：完成季度复盘",
            onAdd = { vm.addGoal(DateUtil.weekKey(selectedDate), it) },
            onToggle = { vm.toggleGoal(it) },
            onDelete = { vm.deleteGoal(it.id) }
        )

        /* ---------- 七天切换 ---------- */
        WeekDayStrip(
            dates = weekDates,
            labels = weekDates.map { DateUtil.weekdayLabel(it).removePrefix("周") },
            dayNumbers = weekDates.map { DateUtil.parse(it).dayOfMonth.toString() },
            heats = heats,
            selectedDate = selectedDate,
            today = DateUtil.today(),
            dark = dark,
            onSelect = {
                HapticUtil.tick(context)
                vm.selectDate(it)
            }
        )

        /* ---------- 选中日任务 ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateUtil.friendly(selectedDate),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${dayTasks.size} 项",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (dayTasks.isEmpty()) {
            EmptyState(text = "这一天还没有任务", sub = "点上面的日期，可用底部输入框添加")
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                dayTasks.forEach { task ->
                    SwipeTaskItem(
                        task = task,
                        onToggle = {
                            HapticUtil.tick(context)
                            vm.toggleTask(task)
                        },
                        onEdit = { vm.openEditor(task) },
                        onDelete = { vm.deleteTask(task.id) }
                    )
                }
            }
        }

        Text(
            text = "数据只保存在本机",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

/** 统计数字块：大数字 + 小标签 */
@Composable
private fun StatBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
