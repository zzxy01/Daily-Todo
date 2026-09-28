// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/components/TaskRow.kt
package com.zhangzhang.dailytodo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangzhang.dailytodo.data.entity.TaskEntity
import com.zhangzhang.dailytodo.ui.theme.Danger
import com.zhangzhang.dailytodo.ui.theme.Urgent
import com.zhangzhang.dailytodo.ui.theme.Warn
import com.zhangzhang.dailytodo.util.DateUtil

/** 圆形复选框：打勾时对勾缩放弹出，背景色渐变；对勾用系统字体字符，不内置字体文件 */
@Composable
fun CircleCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tickScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tick"
    )
    val bgColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "checkboxBg"
    )
    val borderColor =
        if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .size(48.dp) // 热区 48dp，满足单手操作要求
            .clip(CircleShape)
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(width = 2.dp, color = borderColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = tickScale
                    scaleY = tickScale
                }
            )
        }
    }
}

/** 单条任务：复选框 + 标题 + 备注 + 完成时间 + 优先级标记 */
@Composable
fun TaskRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleCheckbox(checked = task.done, onCheckedChange = onToggle)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.priority > 0) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (task.priority == 2) Urgent else Warn)
                        )
                    }
                    val textColor =
                        if (task.done) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        color = textColor,
                        style = TextStyle(
                            textDecoration = if (task.done) TextDecoration.LineThrough else null
                        )
                    )
                }
                if (task.note.isNotBlank()) {
                    Text(
                        text = task.note,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (task.done && task.doneAt != null) {
                    Text(
                        text = "完成于 " + DateUtil.timeLabel(task.doneAt ?: 0L),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 可左滑删除的任务条目：向左滑到底即删除（背景显示"删除"）。
 * 内容区用 TaskRow，额外挂上拖拽用的 modifier 与拖拽中的位移。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeTaskItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()   // 滑到底直接删除
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Danger),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(text = "删除", color = Color.White, fontSize = 15.sp)
                }
            }
        },
        content = {
            TaskRow(
                task = task,
                onToggle = onToggle,
                onClick = onEdit
            )
        }
    )
}
