// DailyTodo/settings.gradle.kts
//
// 国内网络可用的镜像配置（阿里云）：
//   https://maven.aliyun.com/repository/google         -> 镜像 Google Maven（androidx / AGP）
//   https://maven.aliyun.com/repository/public         -> 镜像 Maven Central
//   https://maven.aliyun.com/repository/gradle-plugin  -> 镜像 Gradle 插件门户
//
// 说明两点：
// 1. 镜像放在前面，官方源放后面兜底。镜像缺包时 Gradle 会自动往下找，不需要手动切换。
// 2. pluginManagement 必须一起改 —— AGP / Kotlin / KSP 三个插件是在这里解析的，
//    只改 dependencyResolutionManagement 的话，Sync 仍然会卡在插件下载。
// 如果镜像挂了或返回很慢，把对应的 maven{} 整行删掉即可退回官方源。

pluginManagement {
    repositories {
        // —— 阿里云镜像 ——
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // —— 官方源（兜底）——
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // —— 阿里云镜像 ——
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // —— 官方源（兜底）——
        google()
        mavenCentral()
    }
}

rootProject.name = "DailyTodo"
include(":app")
