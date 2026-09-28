// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/components/WeekDayStrip.kt
package com.zhangzhang.dailytodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.zhangzhang.dailytodo.ui.theme.Heat0
import com.zhangzhang.dailytodo.ui.theme.Heat1
import com.zhangzhang.dailytodo.ui.theme.Heat2
import com.zhangzhang.dailytodo.ui.theme.Heat3
import com.zhangzhang.dailytodo.ui.theme.Heat4
import com.zhangzhang.dailytodo.ui.theme.Heat0Dark
import com.zhangzhang.dailytodo.ui.theme.Heat1Dark
import com.zhangzhang.dailytodo.ui.theme.Heat2Dark
import com.zhangzhang.dailytodo.ui.theme.Heat3Dark
import com.zhangzhang.dailytodo.ui.theme.Heat4Dark

/** 热力等级 -> 颜色（浅色 / 深色两套） */
@Composable
fun heatColor(level: Int, dark: Boolean): Color = when (level) {
    1 -> if (dark) Heat1Dark else Heat1
    2 -> if (dark) Heat2Dark else Heat2
    3 -> if (dark) Heat3Dark else Heat3
    4 -> if (dark) Heat4Dark else Heat4
    else -> if (dark) Heat0Dark else Heat0
}

/** 本周七天切换条：每天的完成度用小圆点颜色深浅表示 */
@Composable
fun WeekDayStrip(
    dates: List<String>,
    labels: List<String>,
    dayNumbers: List<String>,
    heats: List<Int>,
    selectedDate: String,
    today: String,
    dark: Boolean,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dates.forEachIndexed { index, date ->
                val selected = date == selectedDate
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onSelect(date) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = labels.getOrNull(index) ?: "",
                        fontSize = 12.sp,
                        color = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dayNumbers.getOrNull(index) ?: "",
                        fontSize = 16.sp,
                        color = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(heatColor(heats.getOrNull(index) ?: 0, dark))
                    )
                    if (date == today) {
                        Text(
                            text = "今",
                            fontSize = 10.sp,
                            color = if (selected) Color.White
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
