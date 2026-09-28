// DailyTodo/app/src/main/kotlin/com/zhangzhang/dailytodo/ui/theme/Color.kt
package com.zhangzhang.dailytodo.ui.theme

import androidx.compose.ui.graphics.Color

/** 主色：薄荷绿 */
val Mint = Color(0xFF34C759)
val MintDark = Color(0xFF28A745)     // 浅色模式下的强调文字色
val MintContainer = Color(0xFFE9F8EE)
val MintSurface = Color(0xFFF2FBF5)

/** 深色模式下主色需要提亮，保证在深底上有足够对比度 */
val MintOnDark = Color(0xFF5FD980)
val MintContainerDark = Color(0xFF14421F)

/** 中性色（浅色） */
val TextMain = Color(0xFF1C1C1E)
val TextSub = Color(0xFF8A8F8E)
val TextLight = Color(0xFFB9BEBD)
val LineGray = Color(0xFFEDEFF0)
val CardWhite = Color(0xFFFFFFFF)
val BgLight = Color(0xFFF5F7F6)

/** 中性色（深色） */
val TextMainDark = Color(0xFFECECEE)
val TextSubDark = Color(0xFF9AA0A0)
val TextLightDark = Color(0xFF6E7473)
val LineGrayDark = Color(0xFF2A2C2C)
val CardDark = Color(0xFF1C1D1F)
val BgDark = Color(0xFF121314)

/** 语义色 */
val Danger = Color(0xFFFF5A5F)
val Warn = Color(0xFFFF9F0A)
val Urgent = Color(0xFFFF453A)

/** 月历热力：从"没记录"到"全部完成"五档 */
val Heat0 = Color(0xFFF2F4F3)
val Heat1 = Color(0xFFDFF3E4)
val Heat2 = Color(0xFFA9E5BC)
val Heat3 = Color(0xFF65D18C)
val Heat4 = Color(0xFF34C759)

val Heat0Dark = Color(0xFF232526)
val Heat1Dark = Color(0xFF1B3A25)
val Heat2Dark = Color(0xFF276B3E)
val Heat3Dark = Color(0xFF39A05E)
val Heat4Dark = Color(0xFF5FD980)
