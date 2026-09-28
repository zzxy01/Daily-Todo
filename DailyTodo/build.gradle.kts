// DailyTodo/build.gradle.kts
// 根工程：只声明插件版本，不实际应用
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // Kotlin 2.0 起，Compose 编译器以插件形式提供（替代旧的 composeOptions.kotlinCompilerExtensionVersion）
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    // Room 的注解处理器：编译期生成 _Impl 代码，不打包进 APK
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
}
