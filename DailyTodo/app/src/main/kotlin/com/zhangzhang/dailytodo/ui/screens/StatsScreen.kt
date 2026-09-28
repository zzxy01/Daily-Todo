// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/screens/StatsScreen.kt
package com.zhangzhang.dailytodo.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangzhang.dailytodo.ui.theme.ThemeMode
import com.zhangzhang.dailytodo.util.DateUtil
import com.zhangzhang.dailytodo.util.StatsUtil
import com.zhangzhang.dailytodo.vm.TodoViewModel

/**
 * 统计与备份：完成率、连续打卡、断更日、30 天趋势，
 * 以及导出 / 导入 / 快照 / 清空 与主题设置。
 *
 * 文件读写走系统文件选择器（SAF），因此不需要任何存储权限。
 */
@Composable
fun StatsScreen(
    vm: TodoViewModel,
    themeMode: ThemeMode
) {
    val uiState by vm.uiState.collectAsState()
    val context: Context = LocalContext.current

    val monthCursor = remember { DateUtil.monthKey(DateUtil.today()) }
    val monthAllDays = remember(monthCursor) { DateUtil.monthDates(monthCursor) }

    val series = remember(uiState.tasks) { StatsUtil.lastNDaysSeries(uiState.tasks, 30) }
    val monthSummary = remember(uiState.tasks) { StatsUtil.summary(uiState.tasks, monthAllDays) }
    val missed = remember(uiState.tasks) { StatsUtil.missedDays(uiState.tasks, monthAllDays) }
    val streak = remember(uiState.tasks) { StatsUtil.streakDays(uiState.tasks) }
    val totalDone = remember(uiState.tasks) { StatsUtil.totalDone(uiState.tasks) }

    // 导出：弹出系统"保存到"界面，用户自己选位置（零权限）
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) vm.exportTo(uri)
    }

    // 导入：从系统文件选择器挑一个 JSON
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) vm.importFrom(uri)
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

        /* ---------- 关键数字 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(20.dp)) {
                StatCell(
                    value = totalDone.toString(),
                    label = "累计完成",
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    value = streak.toString(),
                    unit = "天",
                    label = "连续打卡",
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    value = monthSummary.percent.toString(),
                    unit = "%",
                    label = "本月完成率",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        /* ---------- 30 天趋势 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "近 30 天完成情况",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(120.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    series.forEach { point ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((point.height / 100f * 110f).dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "柱高 = 当天完成数量（相对这 30 天里最高的一天）",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        /* ---------- 本月断更 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = DateUtil.monthLabel(monthCursor) + "断更日（${missed.size} 天）",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (missed.isEmpty()) "这个月每天都完成了任务"
                    else missed.joinToString("、") { it.substring(5).replace('-', '/') },
                    fontSize = 13.sp,
                    color = if (missed.isEmpty()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "本月计划 ${monthSummary.total} 项，已完成 ${monthSummary.done} 项",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        /* ---------- 备份与恢复 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "数据备份与恢复",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "数据只存在这台手机上，不联网、不上传。换手机或清缓存前请先导出。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )

                ActionButton(
                    text = "导出为 JSON 文件（自己选保存位置）",
                    background = MaterialTheme.colorScheme.primary,
                    textColor = Color.White
                ) {
                    exportLauncher.launch("daily-todo-backup-${DateUtil.today()}.json")
                }
                ActionButton(
                    text = "从 JSON 文件导入（覆盖当前数据）",
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface
                ) {
                    importLauncher.launch(arrayOf("application/json", "text/plain"))
                }
                ActionButton(
                    text = "立即存一份快照",
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface
                ) { vm.makeSnapshot() }
                ActionButton(
                    text = "清空所有数据",
                    background = MaterialTheme.colorScheme.errorContainer,
                    textColor = MaterialTheme.colorScheme.error
                ) { vm.clearAll() }
            }
        }

        /* ---------- 主题设置 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "主题",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ThemeMode.SYSTEM to "跟随系统",
                        ThemeMode.LIGHT to "浅色",
                        ThemeMode.DARK to "深色"
                    ).forEach { (mode, label) ->
                        val selected = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { vm.setThemeMode(mode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        /* ---------- 快照列表 ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "快照（自动保留最近 7 天）",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "每天首次打开自动存一份，超出 7 份自动删最旧的。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (uiState.snapshots.isEmpty()) {
                    Text(
                        text = "还没有快照",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    uiState.snapshots.forEach { snap ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = snap.date,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = (if (snap.from == "auto") "自动" else "手动") +
                                        " · " + DateUtil.timeLabel(snap.createdAt),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text(
                                text = "恢复",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { vm.restoreSnapshot(snap.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                            Text(
                                text = "删除",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { vm.deleteSnapshot(snap.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "数据保存在本机 · 不联网 · 不上传",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )
    }
}

/** 顶部统计格子 */
@Composable
private fun StatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
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

/** 通栏按钮 */
@Composable
private fun ActionButton(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 14.sp, color = textColor)
    }
}
