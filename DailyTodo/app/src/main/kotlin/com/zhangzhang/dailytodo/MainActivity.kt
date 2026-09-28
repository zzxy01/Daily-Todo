// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/MainActivity.kt
package com.zhangzhang.dailytodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhangzhang.dailytodo.ui.AppRoot
import com.zhangzhang.dailytodo.ui.theme.DailyTodoTheme
import com.zhangzhang.dailytodo.vm.TodoViewModel

/**
 * 唯一 Activity。启动时只做一件事：装载 Compose 界面。
 * 不在这里做任何网络请求、文件扫描或数据库预热。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: TodoViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsState()
            DailyTodoTheme(themeMode = themeMode) {
                AppRoot(viewModel = vm)
            }
        }
    }
}
