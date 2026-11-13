# 修改说明

**项目**：HalfText 计算器（Android 移动应用开发实验）
**分支**：`HalfText`（仓库 `Pxy0903/android-app-course`）
**题目**：`D-大学-大学学业-andriod移动应用开发/试题/题目.docx`
**技术栈**：Kotlin + XML 布局 + ViewBinding；AGP 9.3.3 / Gradle 9.5.0 / compileSdk 37 / minSdk 24
**应用标识**：`com.pengxiangyuan.halftext`，项目名 `HalfText_PengXiangYuan`（含姓名拼音）

---

## 一、题目要求与实现对照

| 题目要求 | 实现方式 |
| --- | --- |
| 项目名包含姓名拼音 | `settings.gradle.kts`：`rootProject.name = "HalfText_PengXiangYuan"` |
| 实现计算类（两个数加减乘除） | `Calculator.kt`：`Operator` 枚举 + `add/subtract/multiply/divide`，`calculate()` 统一调度；除数为 0 抛 `IllegalArgumentException` |
| 第 1 个 Activity：输入两个数、选择 `+ - * /`、调用计算类并显示结果 | `MainActivity` + `activity_main.xml`，四选一 `RadioGroup`，输入为空/非法时用 `setError` 提示 |
| 横竖屏也能正常显示结果 | `layout/` 与 `layout-land/` 双布局；`onSaveInstanceState` 保存结果；`fullSensor` 跟随方向传感器 |
| 历史菜单跳转第 2 个 Activity | `menu_main.xml` 的「历史记录」菜单项 + `startActivity` |
| 第 2 个 Activity 用 RecyclerView 显示历史 | `HistoryActivity` + `HistoryAdapter`（`ListAdapter`）+ `item_history.xml`，含空态提示与「清空历史」 |
| 第一个 Activity 标题为「姓名+学号」 | `strings.xml`：`app_name = 彭湘媛2024110126`，并按要求改为棕色字体 |
| 界面合理、代码规范、尽量无错误警告 | 日/夜间配色、输入校验、Lint `No issues found.` |

历史记录由 `HistoryRepository` 用 `SharedPreferences` + `JSONArray` 持久化（表达式、结果、时间，最多 100 条），重启应用不丢失，支持一键清空。

---

## 二、提交记录

| 提交 | 内容 |
| --- | --- |
| `e382742` | 初始化 HalfText 实验项目代码 |
| `9b61bab` | chore: 忽略 `.workbuddy/` 项目数据目录 |
| `2a9de08` | feat: 完成双 Activity 计算器实验（ViewBinding） |
| `f6ba9c3` | feat: 完善横竖屏适配并将姓名学号标题改为棕色 |
| `0d039b7` | fix: 横屏时跟随方向传感器并优化横屏输入体验 |
| 本次提交 | 补充本《修改说明》文档，并忽略 IDE 生成的 `.idea/` 目录 |

---

## 三、逐轮改动说明

### 第 1 轮：完成实验主体（`2a9de08`）

- 开启 `buildFeatures.viewBinding`，新增 RecyclerView 依赖；三个页面/列表项分别通过 `ActivityMainBinding`、`ActivityHistoryBinding`、`ItemHistoryBinding` 访问控件，**全程未使用 `findViewById`**。
- 新建 `Calculator.kt`、`HistoryRepository.kt`、`HistoryActivity.kt`、`HistoryAdapter.kt`，改写 `MainActivity.kt`；补齐竖屏/横屏布局、菜单、矢量图标、字符串/颜色/主题资源，并在 Manifest 注册第二个 Activity。
- 删除模板自带的 `ExampleUnitTest.kt`，新增 `CalculatorTest.kt`（6 个单元测试，覆盖四则运算与除零）。
- 踩坑记录：AGP 9 内置 Kotlin 时，ViewBinding 生成类位于 `<namespace>.databinding` 子包，Kotlin 文件必须显式 `import`，否则大量 `Unresolved reference`。

### 第 2 轮：横竖屏适配 + 姓名学号改棕色（`f6ba9c3`）

**配色**
- 新增 `title_brown`（日间 `#6B3E1E` 深棕 / 夜间 `#E8B478` 浅棕）与 `toolbar_bg`（日间浅暖米白 / 夜间深暖棕）。
- 主页面标题 `彭湘媛2024110126` 使用棕色；因棕色压在深蓝标题栏上不可读，标题栏统一改为浅暖色底，并同步把菜单图标、返回箭头改为棕色（加 `app:iconTint` / `app:navigationIconTint`），状态栏图标随日夜间切换深浅色。
- 历史页标题栏沿用同一浅色风格，但标题文字用中性深色，使「姓名学号」成为页面里唯一的棕色文字。

**布局**
- 新增 `layout-land/activity_history.xml`：横屏时历史列表限宽 72% 居中，避免条目被拉得过长；控件 id 与竖屏完全一致，ViewBinding 不受影响。
- 主页面横屏运算符行由 `wrap_content + 间距` 改为 `0dp + layout_weight` 均分，并改用纯符号短文本（`＋ － × ÷`），窄横屏不会被挤出屏幕。
- 两版 `tvResult` 改为 `match_parent + maxLines=3`，长小数结果（如 `1 ÷ 3`）不会被裁掉。
- 旋转时结果由 `onSaveInstanceState` 显式保存，输入框与运算符选中状态由系统自动恢复，横竖屏切换不丢数据。

**工程规范**
- 新增 `app/lint.xml` 忽略 `AndroidGradlePluginVersion`（「AGP 9.3.3 有新版」的联网提示，与代码无关且会造成警告数波动），项目仍固定 AGP 9.3.3。

### 第 3 轮：修复横屏显示异常（`0d039b7`）

- **现象**：横屏时整个界面文字竖排、标题栏变成左侧竖条，运算符仍显示竖屏布局的「＋ 加」。
- **根因**：系统方向锁定 / 自动旋转关闭，使 Activity 仍收到 `portrait` 配置，`layout-land` 未被加载，系统把竖屏画面旋转 90° 显示。已核对 APK 内确实打包了 `res/layout-land/activity_main.xml`，**不是资源缺失或布局 XML 错误**。
- **修复**：
  - 两个 Activity 声明 `android:screenOrientation="fullSensor"`。Android 官方文档明确 `sensor` / `fullSensor` 会忽略旋转锁定、按方向传感器运行；Android 16 的忽略规则仅适用于 `sw600dp` 以上大屏设备，普通手机不受影响。对应 `DiscouragedApi` 提示用 `tools:ignore` 精准抑制。
  - 两个输入框加 `android:imeOptions="flagNoExtractUi"`，禁止横屏输入法进入全屏提取编辑模式。
  - 横屏标题栏高度 56→48dp、左右内边距 16→12dp、结果区内容改为按内容高度垂直居中，软键盘弹出、可用高度变小时结果仍完整可见。

---

## 四、验证结果

```
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
BUILD SUCCESSFUL
testDebugUnitTest : CalculatorTest 6 tests, 0 failures, 0 errors
lintDebug         : No issues found. (0 errors / 0 warnings)
```

- 产物：`app/build/outputs/apk/debug/app-debug.apk`
- 静态校验：主页面与历史页的横/竖屏布局 id 集合完全一致；字符串、颜色、图标、菜单引用无缺失。

**验证方式**
1. 覆盖安装 `app-debug.apk`，竖屏输入两个数、选择运算符、点「计 算」查看结果。
2. 横过手机，应看到左侧输入+运算、右侧大号结果的**两栏界面**，运算符为 `＋ － × ÷` 纯符号；转回竖屏结果保持不变。
3. 右上角「历史记录」进入历史页，可查看/清空记录；横屏时列表居中收窄。
4. 若仍出现整体旋转的竖屏画面，说明设备存在厂商级「强制竖屏 / 应用方向控制」，需在系统设置中关闭并打开「自动旋转」——这一层应用无法覆盖。

---

## 五、注意事项

- 姓名「彭湘媛」与学号「2024110126」取自仓库 Lab5 分支的历史作业；如有出入，只需修改 `res/values/strings.xml` 的 `app_name` 与 `settings.gradle.kts` 的项目名。
- 题目文件夹中的「上机测试实验报告模板.docx」需另写实验报告并附运行截图与主要代码，本说明不等同于实验报告。
