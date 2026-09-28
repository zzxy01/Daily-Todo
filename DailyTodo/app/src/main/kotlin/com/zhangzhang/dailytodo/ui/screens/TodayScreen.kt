// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/screens/TodayScreen.kt
package com.zhangzhang.dailytodo.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangzhang.dailytodo.ui.components.EmptyState
import com.zhangzhang.dailytodo.ui.components.ProgressBar
import com.zhangzhang.dailytodo.ui.components.SwipeTaskItem
import com.zhangzhang.dailytodo.util.DateUtil
import com.zhangzhang.dailytodo.util.HapticUtil
import com.zhangzhang.dailytodo.util.StatsUtil
import com.zhangzhang.dailytodo.util.TaskRule
import com.zhangzhang.dailytodo.vm.TodoViewModel
import kotlin.math.roundToInt

private val INPUT_BAR_HEIGHT: Dp = 72.dp

/**
 * 今日清单：进度卡 + 一键顺延 + 任务列表（左滑删除 / 长按拖动排序）+ 常驻输入框。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(vm: TodoViewModel) {
    val uiState by vm.uiState.collectAsState()
    val context: Context = LocalContext.current
    val density = LocalDensity.current

    val date = uiState.selectedDate
    // 只依赖"任务集 + 日期"，数据库一变就重算
    val tasks = remember(uiState.tasks, date) { TaskRule.byDate(uiState.tasks, date) }

    // 拖拽期间维护一份本地顺序，抬手后一次性落库，避免每移动一格就写一次数据库
    var list by remember(tasks) { mutableStateOf(tasks) }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    var dragAccum by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    var input by remember { mutableStateOf("") }
    var showRolloverDialog by remember { mutableStateOf(false) }

    // 列表项高度：把手指位移换算成"移动了几格"
    val itemHeightPx = with(density) { 72.dp.toPx() }

    // 一次性提示（Toast）
    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val doneCount = TaskRule.doneCount(list)
    val percent = StatsUtil.percent(doneCount, list.size)
    val undoneCount = TaskRule.undoneCount(list)
    val isToday = date == DateUtil.today()

    Column(modifier = Modifier.fillMaxSize()) {

        /* ---------- 今日进度 ---------- */
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
                        text = "今日进度",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "已完成 $doneCount/${list.size}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ProgressBar(
                    percent = percent,
                    modifier = Modifier.padding(top = 14.dp),
                    height = 12.dp
                )
                Text(
                    text = DateUtil.friendly(date) + if (isToday) " · 今天" else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        /* ---------- 一键顺延 ---------- */
        if (isToday && undoneCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { showRolloverDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "顺延未完成的 $undoneCount 项到明天",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "›", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        /* ---------- 任务列表 ---------- */
        if (list.isEmpty()) {
            EmptyState(
                text = "今天还没有任务",
                sub = "在下面输入框记一笔，回车即可添加",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = draggingId == null
            ) {
                items(items = list, key = { it.id }) { task ->
                    val isDragging = draggingId == task.id
                    SwipeTaskItem(
                        task = task,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = if (isDragging) dragOffsetY.roundToInt() else 0
                                )
                            }
                            .pointerInput(task.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        dragStartIndex = list.indexOfFirst { it.id == task.id }
                                        draggingId = task.id
                                        dragAccum = 0f
                                        dragOffsetY = 0f
                                        HapticUtil.heavy(context)
                                    },
                                    onDrag = { _, dragAmount ->
                                        dragAccum += dragAmount.y
                                        val start = dragStartIndex
                                        val currentIndex = list.indexOfFirst { it.id == draggingId }
                                        if (start >= 0 && currentIndex >= 0 && list.isNotEmpty()) {
                                            // 累计位移换算成目标下标
                                            val target =
                                                (start + (dragAccum / itemHeightPx).roundToInt())
                                                    .coerceIn(0, list.size - 1)
                                            if (target != currentIndex) {
                                                list = TaskRule.move(list, currentIndex, target)
                                            }
                                            // 被拖动项跟手：总位移减去整格部分
                                            dragOffsetY = dragAccum - (target - start) * itemHeightPx
                                        }
                                    },
                                    onDragEnd = {
                                        vm.reorderDay(list.map { it.id })
                                        draggingId = null
                                        dragStartIndex = -1
                                        dragAccum = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggingId = null
                                        dragStartIndex = -1
                                        dragAccum = 0f
                                        dragOffsetY = 0f
                                    }
                                )
                            },
                        onToggle = {
                            HapticUtil.tick(context)
                            vm.toggleTask(task)
                        },
                        onEdit = { vm.openEditor(task) },
                        onDelete = { vm.deleteTask(task.id) }
                    )
                }
                item {
                    Text(
                        text = "左滑删除 · 长按可拖动排序",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            }
        }

        /* ---------- 常驻输入框 ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(INPUT_BAR_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = {
                    Text(
                        text = "添加到 " + DateUtil.friendly(date) + "，回车确认",
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(INPUT_BAR_HEIGHT),
                shape = RoundedCornerShape(22.dp)
            )
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (input.isBlank()) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
                    .clickable {
                        if (input.isNotBlank()) {
                            vm.addTask(input, date)
                            input = ""
                        }
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "添加", fontSize = 15.sp, color = Color.White)
            }
        }
    }

    /* ---------- 顺延确认 ---------- */
    if (showRolloverDialog) {
        AlertDialog(
            onDismissRequest = { showRolloverDialog = false },
            title = { Text(text = "顺延到明天", fontSize = 16.sp) },
            text = { Text(text = "把 $undoneCount 项未完成的任务挪到明天？", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showRolloverDialog = false
                    vm.rolloverUndone()
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showRolloverDialog = false }) { Text("取消") }
            }
        )
    }
}
