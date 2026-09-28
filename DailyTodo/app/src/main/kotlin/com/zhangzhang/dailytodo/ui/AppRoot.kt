// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/AppRoot.kt
package com.zhangzhang.dailytodo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zhangzhang.dailytodo.ui.components.TaskEditorSheet
import com.zhangzhang.dailytodo.ui.screens.MonthScreen
import com.zhangzhang.dailytodo.ui.screens.StatsScreen
import com.zhangzhang.dailytodo.ui.screens.TodayScreen
import com.zhangzhang.dailytodo.ui.screens.WeekScreen
import com.zhangzhang.dailytodo.ui.theme.ThemeMode
import com.zhangzhang.dailytodo.util.HapticUtil
import com.zhangzhang.dailytodo.vm.TodoViewModel

/** 主页面路由：三个 Tab 共用这一条路由，Tab 切换不走导航（避免页面状态被重建） */
private const val ROUTE_MAIN = "main"

/** 统计与备份路由：从周 / 月页右上角进入，返回键回退到主页面 */
private const val ROUTE_STATS = "stats"

/**
 * 应用根界面：
 * - 底部三个 Tab：今日 / 周 / 月（不设第 4 个 Tab）
 * - 统计与备份入口放在周、月两页的右上角
 * - 任务编辑弹层挂在最外层，任何页面点任务都能弹出
 */
@Composable
fun AppRoot(viewModel: TodoViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current

    val navController: NavHostController = rememberNavController()
    val backEntry by navController.currentBackStackEntryAsState()
    val onStatsPage = backEntry?.destination?.route == ROUTE_STATS

    var tab by remember { mutableStateOf(0) }

    // 切 Tab 时校正选中日期（今日回到今天、周落回本周、月以月游标为准）
    LaunchedEffect(tab) { viewModel.onTabSelected(tab) }

    // 统计页按返回键回退（显式处理，不依赖导航库的默认行为）
    BackHandler(enabled = onStatsPage) { navController.popBackStack() }

    Scaffold(
        bottomBar = {
            if (!onStatsPage) {
                BottomTabs(selected = tab) { index ->
                    if (index != tab) {
                        tab = index
                        HapticUtil.tick(context)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_MAIN,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() }
            ) {
                // 主页面：三个 Tab 之一
                composable(ROUTE_MAIN) {
                    MainPage(
                        viewModel = viewModel,
                        themeMode = themeMode,
                        tab = tab,
                        onOpenStats = { navController.navigate(ROUTE_STATS) }
                    )
                }
                // 统计与备份
                composable(ROUTE_STATS) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SimpleTopBar(
                            title = "统计与备份",
                            actionText = null,
                            onAction = {},
                            showBack = true,
                            onBack = { navController.popBackStack() }
                        )
                        StatsScreen(vm = viewModel, themeMode = themeMode)
                    }
                }
            }
        }
    }

    /* ---------- 任务编辑弹层（全局唯一） ---------- */
    val editorTask by viewModel.editorTask.collectAsState()
    TaskEditorSheet(
        task = editorTask,
        onDismiss = viewModel::closeEditor,
        onSubmit = { title, note, priority, date ->
            val id = editorTask?.id
            if (id != null) viewModel.updateTask(id, title, note, priority, date)
            viewModel.closeEditor()
        },
        onDelete = {
            val id = editorTask?.id
            if (id != null) viewModel.deleteTask(id)
            viewModel.closeEditor()
        },
        haptic = { HapticUtil.tick(context) }
    )
}

/** 主页面：顶部标题栏（今日页不显示）+ 当前 Tab 的页面内容 */
@Composable
private fun MainPage(
    viewModel: TodoViewModel,
    themeMode: ThemeMode,
    tab: Int,
    onOpenStats: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (tab != 0) {
            SimpleTopBar(
                title = if (tab == 1) "周计划" else "月计划",
                actionText = "统计",
                onAction = onOpenStats,
                showBack = false,
                onBack = {}
            )
        }
        Crossfade(targetState = tab, label = "tabCrossfade") { index ->
            when (index) {
                0 -> TodayScreen(vm = viewModel)
                1 -> WeekScreen(vm = viewModel, themeMode = themeMode)
                else -> MonthScreen(vm = viewModel, themeMode = themeMode)
            }
        }
    }
}

/* ==================== 顶部标题栏 ==================== */

/**
 * 自绘的极简标题栏（不用 Material3 的 TopAppBar，避免不同版本间实验性 API 的差异）。
 * 右侧可放一个文字入口（周 / 月页为「统计」），左侧可选返回箭头。
 */
@Composable
private fun SimpleTopBar(
    title: String,
    actionText: String?,
    onAction: () -> Unit,
    showBack: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "‹", fontSize = 26.sp, color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.size(12.dp))
        }

        Text(
            text = title,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (showBack) 4.dp else 12.dp)
        )

        if (actionText != null) {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clickable { onAction() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = actionText, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.size(48.dp))
        }
    }
}

/* ==================== 底部三个 Tab ==================== */

private val TAB_TITLES = listOf("今日", "周", "月")

@Composable
private fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 顶部一条分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TAB_TITLES.forEachIndexed { index, title ->
                val isSelected = index == selected
                val color =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TabGlyph(kind = index, color = color)
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 用 Canvas 直接画 Tab 图标：零图片资源、零图标库依赖（不引入 material-icons）。
 * kind: 0=今日（圆 + 勾） 1=周（四根柱） 2=月（九宫格点）
 */
@Composable
private fun TabGlyph(kind: Int, color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        when (kind) {
            0 -> {
                val r = w * 0.40f
                val c = Offset(w / 2f, h / 2f)
                drawCircle(color = color, radius = r, style = Stroke(width = w * 0.10f))
                val p1 = Offset(c.x - r * 0.42f, c.y + r * 0.02f)
                val p2 = Offset(c.x - r * 0.10f, c.y + r * 0.38f)
                val p3 = Offset(c.x + r * 0.48f, c.y - r * 0.36f)
                drawLine(
                    color = color, start = p1, end = p2,
                    strokeWidth = w * 0.10f, cap = StrokeCap.Round
                )
                drawLine(
                    color = color, start = p2, end = p3,
                    strokeWidth = w * 0.10f, cap = StrokeCap.Round
                )
            }

            1 -> {
                val barW = w * 0.15f
                val gap = w * 0.225f
                val heights = floatArrayOf(0.38f, 0.72f, 0.52f, 0.95f)
                val startX = (w - (3 * gap + barW)) / 2f
                for (i in heights.indices) {
                    val bh = h * heights[i]
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(startX + i * gap, h - bh),
                        size = Size(barW, bh),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                }
            }

            else -> {
                val r = w * 0.09f
                val steps = floatArrayOf(0.22f, 0.5f, 0.78f)
                for (x in steps) {
                    for (y in steps) {
                        drawCircle(color = color, radius = r, center = Offset(w * x, h * y))
                    }
                }
            }
        }
    }
}
