// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/vm/TodoViewModel.kt
package com.zhangzhang.dailytodo.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhangzhang.dailytodo.data.entity.GoalEntity
import com.zhangzhang.dailytodo.data.entity.TaskEntity
import com.zhangzhang.dailytodo.data.repo.TodoRepository
import com.zhangzhang.dailytodo.ui.theme.ThemeMode
import com.zhangzhang.dailytodo.util.DateUtil
import com.zhangzhang.dailytodo.util.PrefsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 唯一 ViewModel：持有全部界面状态与所有业务动作。
 *
 * 关于"页面不可见时停止收集"：
 * 这里用 stateIn(SharingStarted.WhileSubscribed(5000))，
 * 界面退到后台后没有订阅者，Room 的 Flow 查询会自动停止（5 秒宽限避免来回重建）。
 * 因此不需要 lifecycle-runtime-compose 的 collectAsStateWithLifecycle，也就没引入清单外的依赖。
 */
class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TodoRepository(application)

    private val app: Application = application

    /* ---------------- 可变的小状态 ---------------- */

    private val _selectedDate = MutableStateFlow(DateUtil.today())
    private val _monthCursor = MutableStateFlow(DateUtil.monthKey(DateUtil.today()))
    private val _editorTask = MutableStateFlow<TaskEntity?>(null)
    private val _themeMode = MutableStateFlow(PrefsUtil.getThemeMode(application))

    /** 一次性提示（Toast） */
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages

    val themeMode: StateFlow<ThemeMode> = _themeMode

    /** 编辑层状态单独暴露：kotlinx.coroutines 的 combine 最多支持 5 个 Flow */
    val editorTask: StateFlow<TaskEntity?> = _editorTask

    /* ---------------- 主界面状态 ---------------- */

    // combine 的强类型重载最多 5 个参数，因此这里正好合并 5 个流
    val uiState: StateFlow<TodoUiState> = combine(
        repo.tasks,
        repo.goals,
        repo.snapshots,
        _selectedDate,
        _monthCursor
    ) { tasks, goals, snapshots, selectedDate, monthCursor ->
        TodoUiState(
            tasks = tasks,
            goals = goals,
            snapshots = snapshots,
            selectedDate = selectedDate,
            monthCursor = monthCursor
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoUiState(
            selectedDate = _selectedDate.value,
            monthCursor = _monthCursor.value
        )
    )

    init {
        // 每日首次打开打一份快照（同一天只保留一份），最多保留 7 份
        viewModelScope.launch {
            runCatching { repo.ensureDailySnapshot() }
        }
    }

    /* ==================== 任务 ==================== */

    fun addTask(title: String, date: String = _selectedDate.value, priority: Int = 0, note: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.addTask(title, date, priority, note)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch { repo.toggleTask(task) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repo.deleteTask(id) }
    }

    /** 单条推到明天 */
    fun postponeTask(task: TaskEntity) {
        viewModelScope.launch {
            repo.postponeTask(task.id, DateUtil.addDays(task.date, 1))
            _messages.emit("已推到明天")
        }
    }

    /** 一键顺延当天所有未完成项 */
    fun rolloverUndone() {
        val from = _selectedDate.value
        viewModelScope.launch {
            val count = repo.rolloverUndone(from, DateUtil.addDays(from, 1))
            _messages.emit(if (count > 0) "已顺延 $count 项到明天" else "没有需要顺延的任务")
        }
    }

    /** 拖拽排序结束：按新顺序写回 */
    fun reorderDay(orderedIds: List<Long>) {
        viewModelScope.launch { repo.reorderDay(orderedIds) }
    }

    fun updateTask(id: Long, title: String, note: String, priority: Int, date: String) {
        val task = uiState.value.tasks.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            repo.updateTask(
                task.copy(title = title, note = note, priority = priority, date = date)
            )
            _messages.emit("已保存")
        }
    }

    /* ==================== 编辑层 ==================== */

    fun openEditor(task: TaskEntity) {
        _editorTask.value = task
    }

    fun closeEditor() {
        _editorTask.value = null
    }

    /* ==================== 日期 / 月份 ==================== */

    fun selectDate(date: String) {
        _selectedDate.value = date
        _monthCursor.value = DateUtil.monthKey(date)
    }

    fun shiftMonth(step: Int) {
        val next = DateUtil.shiftMonth(_monthCursor.value, step)
        _monthCursor.value = next
        // 选中日期若不在新月份里，落到该月 1 号；若新月份就是本月，落回今天
        val today = DateUtil.today()
        _selectedDate.value = if (DateUtil.monthKey(_selectedDate.value) == next) {
            _selectedDate.value
        } else if (next == DateUtil.monthKey(today)) {
            today
        } else {
            next + "-01"
        }
    }

    /** 切到某个 tab 时校正选中日期 */
    fun onTabSelected(tabIndex: Int) {
        val today = DateUtil.today()
        when (tabIndex) {
            0 -> {
                // 今日：永远回到今天
                _selectedDate.value = today
                _monthCursor.value = DateUtil.monthKey(today)
            }
            1 -> {
                // 周：选中日不在本周则回到今天
                if (DateUtil.weekKey(_selectedDate.value) != DateUtil.weekKey(today)) {
                    _selectedDate.value = today
                }
            }
            2 -> {
                // 月：以月游标为准
                if (DateUtil.monthKey(_selectedDate.value) != _monthCursor.value) {
                    _selectedDate.value =
                        if (_monthCursor.value == DateUtil.monthKey(today)) today else _monthCursor.value + "-01"
                }
            }
        }
    }

    /* ==================== 目标 ==================== */

    fun addGoal(key: String, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val ok = repo.addGoal(key, title)
            _messages.emit(if (ok) "已添加" else "最多 ${TodoRepository.GOAL_MAX} 条")
        }
    }

    fun toggleGoal(goal: GoalEntity) {
        viewModelScope.launch { repo.toggleGoal(goal) }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch { repo.deleteGoal(id) }
    }

    /* ==================== 快照 / 备份 ==================== */

    fun makeSnapshot() {
        viewModelScope.launch {
            repo.makeSnapshot("manual")
            _messages.emit("已存快照")
        }
    }

    fun restoreSnapshot(id: Long) {
        viewModelScope.launch {
            val ok = repo.restoreSnapshot(id)
            _messages.emit(if (ok) "已恢复" else "快照不存在")
        }
    }

    fun deleteSnapshot(id: Long) {
        viewModelScope.launch { repo.deleteSnapshot(id) }
    }

    /** 导出到用户在系统文件选择器里指定的位置（SAF，零权限） */
    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) { repo.exportJson() }
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    app.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                }.isSuccess
            }
            _messages.emit(if (ok) "已导出备份" else "导出失败")
        }
    }

    /** 从用户在系统文件选择器里选的 JSON 导入（覆盖式，导入前会自动存快照） */
    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    app.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                }.getOrNull()
            }
            if (text.isNullOrBlank()) {
                _messages.emit("读不到文件内容")
                return@launch
            }
            val ok = withContext(Dispatchers.IO) { repo.importJson(text) }
            _messages.emit(if (ok) "导入成功" else "不是有效的备份文件")
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.clearAll()
            _messages.emit("已清空")
        }
    }

    /* ==================== 设置 ==================== */

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        PrefsUtil.setThemeMode(app, mode)
    }
}
