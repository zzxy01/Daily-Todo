// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/components/MonthCalendar.kt
package com.zhangzhang.dailytodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangzhang.dailytodo.util.DateUtil

/**
 * 月历热力图：6 行 × 7 列，每格背景色深浅表示当天完成度。
 * 纯 Compose 绘制，不使用任何图片素材。
 */
@Composable
fun MonthCalendar(
    monthLabel: String,
    monthKey: String,         // "yyyy-MM"，用于判断格子是否属于本月
    grid: List<String>,       // 42 个日期
    heats: List<Int>,         // 与 grid 一一对应的热力等级
    selectedDate: String,
    today: String,
    dark: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 月份切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onPrevMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "‹", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = monthLabel,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onNextMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 星期表头
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // 6 行 × 7 列
            val rows = 6
            Column(modifier = Modifier.padding(top = 4.dp)) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 7) {
                            val index = r * 7 + c
                            val date = grid.getOrNull(index) ?: ""
                            val inMonth = date.isNotBlank() && DateUtil.monthKey(date) == monthKey
                            val heat = heats.getOrNull(index) ?: 0
                            val isSelected = date == selectedDate
                            val isToday = date == today

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { if (date.isNotBlank()) onSelect(date) }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(heatColor(heat, dark))
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (date.isNotBlank()) DateUtil.parse(date).dayOfMonth.toString() else "",
                                        fontSize = 13.sp,
                                        color = when {
                                            !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            heat >= 3 -> Color.White
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.Bold
                                        else androidx.compose.ui.text.font.FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 图例
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "少", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                for (level in 0..4) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(heatColor(level, dark))
                    )
                }
                Text(text = "多", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
