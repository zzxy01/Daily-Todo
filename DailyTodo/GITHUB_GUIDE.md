# 从零开始：把工程推到 GitHub，用 Actions 自动出 APK

全程不用碰 Android Studio 的构建，只要能上网就能拿到 APK。
预计耗时 20 分钟左右，大部分时间在等。

---

## 第 1 步：注册 GitHub 账号

1. 打开 https://github.com/signup
2. 填邮箱 → 密码 → 用户名（**用户名记下来**，后面拼地址要用，假设叫 `zhangzhang`）
3. 邮箱收一封验证邮件，点里面的链接完成验证

> 用户名一旦被占用就得换一个。它会出现在仓库地址里，但不影响后面操作。

---

## 第 2 步：安装 Git（Windows）

1. 打开 https://git-scm.com/download/win ，下载会自动开始
2. 双击安装，**一路 Next 就行**，所有选项保持默认（尤其是 "Git from the command line" 那项默认勾着）
3. 装完后重新打开一个终端，输入 `git --version`，能出现 `git version 2.x.x` 就成了

---

## 第 3 步：建一个私有仓库

1. 登录 GitHub，右上角 `+` → **New repository**
2. 按下表填：

   | 字段 | 填什么 | 说明 |
   | --- | --- | --- |
   | Repository name | `DailyTodo` | 随便起，后面命令要对上 |
   | 类型 | **Private**（私有） | 默认就是 Private，确认一下 |
   | Add a README file | **不要勾** | ⚠️ 勾了会导致首次推送冲突 |
   | Add .gitignore | **不要勾** | ⚠️ 工程里已经有了 |
   | Choose a license | **不要勾** | 无所谓，但别勾 |

3. 点 **Create repository**
4. 建好后页面会显示一个 "Quick setup" 的空仓库提示 —— **看到这个提示说明是对的**

---

## 第 4 步：生成推送凭证（这一步最容易卡住）

GitHub 从 2021 年起**不再接受账号密码推送**，必须用令牌。

1. 右上角头像 → **Settings** → 左侧最下面 **Developer settings**
2. **Personal access tokens** → **Tokens (classic)** → **Generate new token (classic)**
3. Note 填 `dailytodo`，Expiration 选 `90 days`
4. 勾选 **`repo`**（勾它会自动勾上下面一串子项，够了）
5. 拉到最底点 **Generate token**
6. **立刻复制那一串 `ghp_xxxxx`** —— 关掉页面就再也看不到了，只能重新生成

后面 `git push` 弹窗时：用户名填你的 GitHub 用户名，**密码框粘贴这个 token**。

---

## 第 5 步：把代码推上去

打开 **Git Bash**（开始菜单搜 "Git Bash"），逐行执行：

```bash
# 1. 设一次默认分支名（只做一次）
git config --global init.defaultBranch main

# 2. 进到工程目录（注意是 D 盘，Git Bash 里写成 /d/）
cd /d/.workbuddy/2026-09-28-10-16-20/DailyTodo

# 3. 初始化仓库
git init

# 4. 登记身份（名字邮箱随便填，只用于提交记录）
git config user.name "zhangzhang"
git config user.email "zhangzhang@example.com"

# 5. 加入全部文件
git add .

# 6. 提交
git commit -m "初始提交：每日清单安卓 App"

# 7. 关联远程仓库 —— 把 zhangzhang 换成你的 GitHub 用户名
git remote add origin https://github.com/zhangzhang/DailyTodo.git

# 8. 改分支名并推送
git branch -M main
git push -u origin main
```

第 8 步会弹登录框：用户名 = GitHub 用户名，密码 = 第 4 步那串 `ghp_` 开头的 token。

成功的样子：出现 `Branch 'main' set up to track 'origin/main'.` 和一堆 `Writing objects: 100%`。

**验证**：刷新 GitHub 仓库页面，应该能看到 47 个文件，`README.md` 直接渲染出来。

---

### 第 5 步的备选：用 GitHub 网页上传

不想装 Git 的话，也可以在仓库页面点 **Add file → Upload files**，把 `DailyTodo` 文件夹里的内容拖进去。

但有三个坑要提前知道：

1. **网页上传不会应用 `.gitignore`**。所以上传前必须确认本地没有 `build\`、`local.properties`（它含你本机 SDK 路径，泄露也没意义）。我们工程是干净的，可以放心传。
2. **以点号开头的隐藏目录可能被浏览器漏掉**（`.github` 就是）。传完一定去仓库里确认能看到 `.github/workflows/build.yml`。看不到的话手动补：
   仓库页 → **Add file → Create new file** → 文件名框里**直接整段输入** `.github/workflows/build.yml`（GitHub 会自动创建多级目录）→ 把文件内容粘贴进去 → Commit。
3. 网页上传**没有 git 历史**，后续改代码还是得靠命令行（第 5 步末尾那三条命令）。建议还是第 5 步的命令行方式更省事。

---

## 第 6 步：等 Actions 跑完

1. 刷新仓库页面，顶部会多出一个橙色圆点 / 黄色小勾，点上面的 **Actions** 标签
2. 列表里会出现一条 `Build Debug APK` 的运行记录，点进去
3. 再点左边 **build** 这个 job，就能看到实时日志

> 第一次会冷启动（下载 Gradle + 全部依赖），约 **5–10 分钟**；之后有缓存，1–2 分钟。
> 黄色圆圈 = 在跑，绿色对勾 = 成功，红色叉 = 失败（把报错发我）。

跑完后在日志里找 **「输出 APK 体积」** 这一步，会打印三个 APK 的**实测字节数** —— 这是真机构建出来的真实体积。

---

## 第 7 步：下载 APK

1. 在 job 页面往下滚到最底部 **Artifacts** 区域
2. 点 **`app-debug`**，浏览器会下一个 zip
3. 解压，里面有三个：

   | 文件名 | 装哪个 |
   | --- | --- |
   | `app-arm64-v8a-debug.apk` | **绝大多数手机装这个**（2017 年后的机器基本都是） |
   | `app-universal-debug.apk` | 不确定机型就装这个，体积大些但通用 |
   | `app-armeabi-v7a-debug.apk` | 很老的 32 位机器 |

4. 传到手机（微信文件传输助手 / QQ / USB 都行），点开安装，允许「未知来源」

---

## 第 8 步（强烈建议）：把缺失的 Gradle Wrapper 补回本地

第 6 步的 Artifacts 区域还有第二个包 **`gradle-wrapper`**，一起下载：

```
gradle-wrapper.zip
├── gradle-wrapper.jar
├── gradlew
└── gradlew.bat
```

按这个位置放回本地工程：

```
D:\.workbuddy\2026-09-28-10-16-20\DailyTodo\
├── gradlew.bat          ← 放这里
├── gradlew              ← 放这里
└── gradle\wrapper\
    ├── gradle-wrapper.jar       ← 放这里
    └── gradle-wrapper.properties （已存在，不要覆盖）
```

放好之后，本机也能用命令行构建了：

```bash
cd /d/.workbuddy/2026-09-28-10-16-20/DailyTodo
./gradlew.bat assembleDebug
```

> 注意：CI 生成的 `gradle-wrapper.properties` 指向官方源 `services.gradle.org`。
> 如果你本地也想走国内镜像，把它的 `distributionUrl` 改回
> `https\://mirrors.cloud.tencent.com/gradle/gradle-8.7-bin.zip` 即可。

---

## 常见问题

**Q：push 报 `refusing to merge unrelated histories`**
说明第 3 步不小心勾了 README。最简单：删掉那个仓库重建一个，重做第 5 步。

**Q：push 报 `Authentication failed`**
密码框里贴的不是 token，或者 token 没勾 `repo`。回第 4 步重新生成一个。

**Q：Actions 第一步就红，报 `gradle-wrapper.jar not found`**
不应该发生 —— workflow 里没有用 `./gradlew`。如果真出现了，把完整报错发我。

**Q：Actions 报 `Could not resolve androidx.xxx`**
依赖下载失败。CI 用的是 GitHub 官方网络，一般没问题；偶发的话点右上角 **Re-run jobs** 重试一次。

**Q：以后改了代码怎么更新 APK**
```bash
cd /d/.workbuddy/2026-09-28-10-16-20/DailyTodo
git add .
git commit -m "改了什么"
git push
```
push 完 Actions 自动重跑，Actions 页面下载新的 Artifact 即可。
