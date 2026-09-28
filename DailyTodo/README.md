# 每日清单 · DailyTodo（安卓原生 App）

Kotlin + Jetpack Compose + Material 3，单机离线，数据只存本机。

- 包名：`com.zhangzhang.dailytodo`
- minSdk 26（Android 8.0）/ targetSdk 34 / compileSdk 34
- 权限：仅 `VIBRATE`，**没有 INTERNET**
- 依赖：Compose BOM + material3 + activity-compose + navigation-compose + lifecycle-viewmodel-compose + Room + 协程（无广告/统计/推送/Firebase/GMS）

---

## 零、先补一个文件：gradle-wrapper.jar（重要）

本工程只写了 `gradle/wrapper/gradle-wrapper.properties`，**没有** `gradlew.bat` / `gradlew` / `gradle-wrapper.jar`
（jar 是二进制文件，无法通过文本生成）。所以直接跑 `gradlew.bat assembleDebug` 会报
`Could not find or load main class org.gradle.wrapper.GradleWrapperMain`。

任选一种方式补齐（一次性）：

- **方式 A（推荐）**：Android Studio 打开工程后，若提示 wrapper 缺失，点 "Generate" / "OK" 让它自动生成。
- **方式 B**：机器上有任意 gradle 时，在工程根目录执行 `gradle wrapper --gradle-version 8.7`。
- **方式 C**：从你已有的任何一个安卓工程里，把 `gradle\gradle-wrapper.jar` 复制到本工程的 `gradle\wrapper\` 下。

补好后工程根目录会出现 `gradlew` 与 `gradlew.bat`，之后即可用命令行构建。

## 一、导入 Android Studio

1. Android Studio 版本要求：**Koala(2024.1.1) 或更高**（AGP 8.5.2 需要）。
2. `File → New → Import Project`（或启动页 `Open`），选中 **`DailyTodo` 目录**（含 `settings.gradle.kts` 的那一层），点 OK。
3. 首次打开会弹 "Gradle Sync"，点 **Sync Now**。它需要联网下载 Gradle 8.7 与依赖（只是编译环境联网，App 本身不联网）。
4. 如果提示 JDK：用 `File → Settings → Build Tools → Gradle → Gradle JDK` 选 **JDK 17**（AGP 8.5 强制要求 17）。
5. Sync 成功后，工具栏运行配置选 `app`，目标设备选你的手机，点 ▶ 运行。

### 网络镜像（国内必看）

工程已预置国内镜像，一般不用再动：

| 文件 | 镜像 |
| --- | --- |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 发行包走**腾讯云** `mirrors.cloud.tencent.com/gradle/`；想换阿里云改成 `mirrors.aliyun.com/gradle/gradle-8.7-bin.zip` |
| `settings.gradle.kts` → `pluginManagement` | 阿里云 `maven.aliyun.com/repository/{google, gradle-plugin, public}`，AGP / Kotlin / KSP 三个插件从这里下 |
| `settings.gradle.kts` → `dependencyResolutionManagement` | 阿里云 `maven.aliyun.com/repository/{google, public}`，Compose / Room / Navigation 等依赖从这里下 |

镜像在前、官方源在后兜底，镜像缺包会自动回落，不需要手动切换。

**排查清单**
- Sync 仍卡住 → 先看 Android Studio 右下角的进度提示，确认卡在 "Download gradle-8.7-bin.zip" 还是某个 `.pom`/`.aar`。前者改 `distributionUrl`，后者看是不是镜像没覆盖到。
- 下载到一半失败 → 删掉 `C:\Users\<你>\.gradle\wrapper\dists\gradle-8.7-bin\` 整个目录，重新 Sync。
- 报 "Could not resolve …" → 检查 `Settings → Build Tools → Gradle` 是否误开了 **Offline work**（离线模式必须关掉）。
- 镜像整体抽风 → 把 `settings.gradle.kts` 里带 `maven.aliyun.com` 的行整行删掉，退回官方源。

## 二、编译 APK

**Debug 包（自己用）**
`Build → Build Bundle(s)/APK(s) → Build APK(s)`，完成后右下角点 `locate`，
产物在 `app/build/outputs/apk/debug/app-debug.apk`。
注意：debug 包未压缩，体积偏大，不能作为最终体积结论。

**Release 签名包（对外/装机用）**
1. `Build → Generate Signed Bundle / APK…` → 选 **APK** → Next。
2. Key store path 点 `Create new…`：
   - 保存路径建议放到工程外，例如 `D:\keys\dailytodo.jks`
   - 密码自己记牢（丢失后无法升级覆盖安装）
   - Alias：`dailytodo`，有效期默认 25 年
3. 选 **release** 变体，勾选 **V7 (Full APK Signature)**，Finish。
4. 产物：
   - `app/build/outputs/apk/release/app-arm64-v8a-release.apk`（**绝大多数现代手机装这个，体积最小**）
   - `app/build/outputs/apk/release/app-armeabi-v7a-release.apk`（很老的 32 位机器）
   - `app/build/outputs/apk/release/app-universal-release.apk`（通用包，含两个 ABI，体积最大）

## 三、装到手机

- **USB**：手机开发者选项打开「USB 调试」，连电脑，Android Studio 直接 ▶ 运行；或把 APK 拷到手机存储里点开安装（需允许「未知来源」）。
- **微信/QQ 传文件**：手机端打开 APK 安装即可（不需要电脑）。
- 安装后桌面图标是薄荷绿底白色对勾，应用名「每日清单」。

## 四、数据备份与恢复

入口：底部切到 **周** 或 **月** → 右上角 **统计** → 「数据备份与恢复」卡片。

| 操作 | 路径 | 说明 |
| --- | --- | --- |
| 导出 | 统计页 →「导出为 JSON 文件」 | 弹出系统「保存到」，自己选位置（微信/网盘/本地都行），文件名默认 `daily-todo-backup-2026-09-28.json` |
| 导入 | 统计页 →「从 JSON 文件导入」 | 弹出系统文件选择器挑那个 JSON。**覆盖式**：导入前会自动先存一份快照，可回滚 |
| 快照 | 统计页 →「立即存一份快照」 | 每天首次打开也会自动存一份，最多保留最近 7 份，超出自动删最旧的 |
| 回滚 | 统计页 → 快照列表 →「恢复」 | 把整份数据还原到某份快照的状态 |
| 清空 | 统计页 →「清空所有数据」 | 同样会先自动存快照 |

换手机的正确姿势：老手机导出 JSON → 传到新手机 → 装 App → 导入那个 JSON。

## 五、体积

- 约定：以 **arm64-v8a 的 release APK** 为准，目标 < 8MB。
- 查看实际大小：Android Studio 菜单 `Build → Analyze APK…`，选中 `app-arm64-v8a-release.apk`。
- 已经在做的事情：`minifyEnabled` + `shrinkResources`、只打 arm64-v8a/armeabi-v7a、无 png 图标（全 Vector）、不内置字体、不开 multidex。
- 如果实测仍然超标，按这个顺序砍：① 只发 arm64-v8a 单 ABI（去掉 `isUniversalApk`）→ ② 移除 `values-night` 之外的冗余资源 → ③ 改用 AAB 上架。

## 六、GitHub Actions 自动构建（无需本机装 Android Studio 也能出包）

`.github/workflows/build.yml` 已配置好：push 到 `main` 分支自动 `assembleDebug`，产物作为 Artifact 上传，保留 30 天。

产物有两个：

| Artifact 名 | 内容 | 用途 |
| --- | --- | --- |
| `app-debug` | 三个 Debug APK（arm64-v8a / armeabi-v7a / universal），含**实测字节数**（日志里打印） | 装手机试用 |
| `gradle-wrapper` | `gradlew`、`gradlew.bat`、`gradle-wrapper.jar` | **下载后放回本地工程**，补齐本机缺失的 wrapper |

> 私有仓库每月有 2000 分钟 Actions 免费额度，一次构建约 5-10 分钟，够用。

## 七、目录结构

```
DailyTodo/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── app/
│   ├── build.gradle.kts / proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/  (themes / strings / 矢量图标)
│       └── kotlin/com/zhangzhang/dailytodo/
│           ├── MainActivity.kt          唯一 Activity
│           ├── ui/
│           │   ├── AppRoot.kt           底部三 Tab + 统计路由 + 编辑弹层
│           │   ├── theme/               Color.kt / Theme.kt
│           │   ├── components/          进度条 / 空态 / 任务行 / 目标面板 /
│           │   │                        周条 / 月历 / 编辑弹层
│           │   └── screens/             Today / Week / Month / Stats
│           ├── vm/                      TodoViewModel / TodoUiState
│           ├── data/
│           │   ├── entity/              Task / Goal / Snapshot
│           │   ├── db/                  3 个 DAO + AppDatabase
│           │   ├── backup/BackupCodec.kt
│           │   └── repo/TodoRepository.kt
│           └── util/                    Date / TaskRule / Stats / Haptic / Prefs
```
