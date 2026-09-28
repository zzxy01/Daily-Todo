// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/theme/Theme.kt
package com.zhangzhang.dailytodo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** 主题模式：跟随系统 / 强制浅色 / 强制深色 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightColors = lightColorScheme(
    primary = Mint,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = MintDark,
    background = BgLight,
    onBackground = TextMain,
    surface = CardWhite,
    onSurface = TextMain,
    surfaceVariant = BgLight,
    onSurfaceVariant = TextSub,
    outline = LineGray,
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = MintOnDark,
    onPrimary = Color(0xFF06210F),
    primaryContainer = MintContainerDark,
    onPrimaryContainer = MintOnDark,
    background = BgDark,
    onBackground = TextMainDark,
    onSurface = TextMainDark,
    surface = CardDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSubDark,
    outline = LineGrayDark,
    error = Danger
)

/**
 * 应用主题。
 * 刻意不用 Android 12 的动态取色：那会把主色换成壁纸色，破坏"薄荷绿"这一硬性约定。
 * Typography 用系统默认字体，不内置任何字体文件。
 */
@Composable
fun DailyTodoTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}

/** 当前是否深色模式（供月历热力等自定义配色取色） */
@Composable
fun isDarkThemeNow(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}
