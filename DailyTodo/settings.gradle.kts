// DailyTodo/settings.gradle.kts
//
// 仓库顺序说明（踩过坑，别再改回去）：
// pluginManagement 里官方源必须排最前。之前把阿里云镜像排在最前面，
// 导致 com.google.devtools.ksp 插件标记解析失败（报 Plugin was not found）。
// 现在顺序固定为：gradlePluginPortal() -> mavenCentral() -> google() -> 阿里云镜像（兜底，带 content 过滤）。

pluginManagement {
    repositories {
        // —— 1. Gradle 插件门户：KSP / Kotlin 等插件的插件标记在这里 ——
        gradlePluginPortal()

        // —— 2. Maven Central：显式声明，不依赖镜像 ——
        mavenCentral()

        // —— 3. Google Maven：AGP / androidx 官方源 ——
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // —— 4. 阿里云镜像（兜底，本机国内网络用；CI 上基本用不到）——
        //      每个镜像都加了 content 过滤，只让它响应自己该管的那些 group，
        //      避免镜像返回异常结果时波及无关坐标。
        //      若仍出现插件解析失败，直接把下面三个 maven{} 整段删掉。
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
            content {
                includeGroupByRegex("org\\.jetbrains.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            content {
                includeGroupByRegex("org\\.jetbrains.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // —— 阿里云镜像：Compose / Room / Navigation / Lifecycle / 协程 都落在这些 group 里 ——
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            content {
                includeGroupByRegex("org\\.jetbrains.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // —— 官方源（兜底）——
        google()
        mavenCentral()
    }
}

rootProject.name = "DailyTodo"
include(":app")
