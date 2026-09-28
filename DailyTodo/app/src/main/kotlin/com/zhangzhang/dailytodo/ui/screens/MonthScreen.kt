// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/screens/MonthScreen.kt
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangzhang.dailytodo.ui.components.EmptyState
import com.zhangzhang.dailytodo.ui.components.GoalPanel
import com.zhangzhang.dailytodo.ui.components.MonthCalendar
import com.zhangzhang.dailytodo.ui.components.ProgressBar
import com.zhangzhang.dailytodo.ui.components.SwipeTaskItem
import com.zhangzhang.dailytodo.ui.theme.ThemeMode
import com.zhangzhang.dailytodo.ui.theme.isDarkThemeNow
import com.zhangzhang.dailytodo.util.DateUtil
import com.zhangzhang.dailytodo.util.HapticUtil
import com.zhangzhang.dailytodo.util.StatsUtil
import com.zhangzhang.dailytodo.util.TaskRule
import com.zhangzhang.dailytodo.vm.TodoViewModel

/**
 * 月计划：月历热力图 + 本月目标 + 月度回顾（含断更日）+ 选中日任务列表。
 * 统计与备份入口在右上角（由 AppRoot 的 TopAppBar 提供）。
 */
@Composable
fun MonthScreen(
    vm: TodoViewModel,
    themeMode: ThemeMode
) {
    val uiState by vm.uiState.collectAsState()
    val context: Context = LocalContext.current
    val dark = isDarkThemeNow(themeMode)

    val monthCursor = uiState.monthCursor
    val selectedDate = uiState.selectedDate

    val grid = remember(monthCursor) { DateUtil.monthGrid(monthCursor) }
    val monthAllDays = remember(monthCursor) { DateUtil.monthDates(monthCursor) }
    val heats = remember(uiState.tasks, monthCursor) {
        StatsUtil.heatList(uiState.tasks, grid)
    }
    val monthGoals = remember(uiState.goals, monthCursor) {
        uiState.goals.filter { it.key == monthCursor }
    }
    val monthSummary = remember(uiState.tasks, monthCursor) {
        StatsUtil.summary(uiState.tasks, monthAllDays)
    }
    val missed = remember(uiState.tasks, monthCursor) {
        StatsUtil.missedDays(uiState.tasks, monthAllDays)
    }
    val dayTasks = remember(uiState.tasks, selectedDate) {
        TaskRule.byDate(uiState.tasks, selectedDate)
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

        /* ---------- 月历 ---------- */
        MonthCalendar(
            monthLabel = DateUtil.monthLabel(monthCursor),
            monthKey = monthCursor,
            grid = grid,
            heats = heats,
            selectedDate = selectedDate,
            today = DateUtil.today(),
            dark = dark,
            onPrevMonth = { vm.shiftMonth(-1) },
            onNextMonth = { vm.shiftMonth(1) },
            onSelect = {
                HapticUtil.tick(context)
                vm.selectDate(it)
            }
        )

        /* ---------- 本月目标 ---------- */
        GoalPanel(
            title = "本月目标",
            goals = monthGoals,
            placeholder = "例如：读完两本书",
            onAdd = { vm.addGoal(monthCursor, it) },
            onToggle = { vm.toggleGoal(it) },
            onDelete = { vm.deleteGoal(it.id) }
        )

        /* ---------- 月度回顾 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "月度回顾",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(modifier = Modifier.padding(top = 14.dp)) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = monthSummary.done.toString(),
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "/${monthSummary.total}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                            )
                        }
                        Text(
                            text = "完成 / 计划",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = monthSummary.percent.toString(),
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "%",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                            )
                        }
                        Text(
                            text = "月度进度",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                ProgressBar(
                    percent = monthSummary.percent,
                    modifier = Modifier.padding(top = 16.dp),
                    height = 10.dp
                )

                Text(
                    text = "断更日（${missed.size} 天）",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                )
                if (missed.isEmpty()) {
                    Text(
                        text = "这个月还没有断更，保持住",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // 用换行文本展示，避免使用实验性的 FlowRow
                    Text(
                        text = missed.joinToString("、") { it.substring(5).replace('-', '/') },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
            EmptyState(text = "这一天还没有任务", sub = "点日历选一天，再回到今日页添加")
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
