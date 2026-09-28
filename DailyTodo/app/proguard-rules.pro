# DailyTodo/app/proguard-rules.pro
# Room 与 Compose 的 keep 规则由各自库通过 consumerProguardFiles 自带，这里只补必要项。

# 保留注解（Room 生成代码依赖注解元数据）
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

# 数据库实体：导出 JSON 时用的是手写 org.json，不依赖反射，
# 这里保留字段仅为避免 R8 改名后影响可读性（无副作用）
-keep class com.zhangzhang.dailytodo.data.entity.** { *; }

# Room 的 DAO 实现类与数据库类不能混淆
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.zhangzhang.dailytodo.data.db.*_Impl { *; }

# org.json 是系统自带库，屏蔽可能的告警
-dontwarn org.json.**

# Compose 编译器生成的稳定性元数据
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
