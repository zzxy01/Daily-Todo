// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/util/PrefsUtil.kt
package com.zhangzhang.dailytodo.util

import android.content.Context
import com.zhangzhang.dailytodo.ui.theme.ThemeMode

/**
 * 极少量设置项（目前只有主题模式）用系统 SharedPreferences 持久化。
 * 刻意不引入 DataStore：它会带进 protobuf-lite + okio，约 0.5~0.9MB，
 * 为一个开关付出这么多体积不划算。
 */
object PrefsUtil {

    private const val FILE_NAME = "daily_todo_prefs"
    private const val KEY_THEME = "theme_mode"

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(context: Context): ThemeMode {
        val value = prefs(context).getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return runCatching { ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME, mode.name).apply()
    }
}
