# MindEcho 「智愈心海」

> **倾诉即销毁，回望皆释然** — 轻量化桌面情绪解压疗愈工具

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?logo=java)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey?logo=windows)](https://www.microsoft.com/windows)
[![Status](https://img.shields.io/badge/Status-Design%20Complete-blue)](docs/设计文档.md)

> ⚠️ **项目状态**：当前处于开发完成阶段，代码已实现并可运行。详细设计文档请参考 [docs/设计文档.md](docs/设计文档.md)。

***

## 📖 项目简介

MindEcho「智愈心海」不是传统情绪日记。它的核心主张是：**负面情绪不需要被永久记住，流动释放才是真正的疗愈**。

这是一款可以**双击直接启动**的桌面程序，你说出来，AI 承接，然后一起销毁。治愈或毒舌的回应，粉碎动画里消散的文字，随时可以清零的加密痕迹。

**目标用户**：学生、职场年轻人，日常有细碎委屈、焦虑、内耗，需要低成本情绪宣泄出口的人群。

***

## ✨ 核心功能

### 🔥 情绪粉碎机

- 无字数、无格式限制的自由倾诉输入框
- 点击「粉碎」触发文字消散破碎粒子动画（300\~500 粒子，≤ 3 秒），带来物理层面的情绪释放感
- AI 双模式随机回应：**温柔治愈（GENTLE）** 或 **清醒毒舌（SHARP）**，15\~40 字，精准承接情绪
- OpenAI API 异常时自动切换本地备用话术库兜底，永不空白

### 🔒 隐私加密

- 倾诉原文采用 **AES-256-GCM** 加密存入本地 SQLite，明文绝不落盘
- 密钥存储于本机用户目录（`%APPDATA%/MindEcho/key.bin`），不上传任何服务器
- 全局一键销毁：彻底清空所有日志与加密数据，不留痕迹
- 所有数据仅存本机，无任何云端上传

### 🌤️ 情绪天气

- 首页顶部实时情绪气象动画，基于当日情绪标签分布自动计算
- 大量愤怒（> 40%）→ ⛈️ 雷雨 | 持续焦虑（最高频）→ ☁️ 阴天 | 有委屈 → 🌧️ 小雨 | 其余 → 🌤️ 晴

### 🎰 情绪胶囊

- 将当下的情绪封存进「时间胶囊」，设定解锁时间（3天/7天/30天）
- 胶囊墙展示所有胶囊，显示情绪图标、封存日期、解锁倒计时
- 到达设定时间后可打开，AI 生成「成长回顾」寄语
- 每天最多封存 3 个胶囊

### 📊 月度情绪报告

- 自动聚合当月数据：高频情绪排名、每周宣泄频次、治愈/毒舌占比
- 个性化文字调节建议（≥ 1 条）

### 📚 情绪文章推荐

- AI 心情识别：输入心情描述，自动分析情绪类型和压力等级
- 根据识别结果自动推荐对应主题的文章
- 心情识别与音效联动：识别情绪后自动切换环境音效
- 文章分类浏览：情绪管理、压力管理、悲伤疗愈、焦虑管理等
- AI 生成文章：选择情绪主题生成定制文章
- 网络文章导入：一键从第三方 API（ZenQuotes、Hitokoto、ApiZero）导入精选文章

### 🎧 情绪共振音系统

- 根据当前情绪天气自动切换对应环境音景，将情绪体验从视觉延伸至听觉
- 四种情绪音场映射：
  - **愤怒（THUNDERSTORM）** → 低沉鼓点 + 压迫性风声（`anger.mp3`）
  - **焦虑（CLOUDY）** → 低频心跳 + 风声混合音景（`anxiety.mp3`）
  - **悲伤（RAINY）** → 雨声 + 空间混响（`sadness.mp3`）
  - **平静（SUNNY）** → 自然白噪音 + 轻风（`calm.mp3`）
- 渐进式**淡入 / 淡出（1\~3 秒）**，模拟情绪自然衰减，避免突兀切换
- 基于 **JavaFX MediaPlayer** 实现，轻量无侵入，支持音效开关

***

## 🛠️ 技术栈

| 层级       | 技术                                                 |
| -------- | -------------------------------------------------- |
| 桌面 UI 框架 | JavaFX 21 + FXML                                   |
| 构建工具     | Maven                                              |
| AI 服务    | OpenAI Chat Completions API（OkHttp 4.x + Gson 2.x） |
| 加密方案     | AES-256-GCM（`javax.crypto` 标准库）                    |
| 数据库      | SQLite（`sqlite-jdbc` 驱动，单文件零配置）                    |
| 音频播放     | JavaFX `MediaPlayer`（情绪共振音系统）                      |
| 配置管理     | `.properties` 本地配置文件                               |
| 测试框架     | jqwik 1.8.5（属性测试）+ JUnit 5 + Mockito + TestFX      |
| 打包发布     | `jpackage`（打包为独立 `.exe`，捆绑 JRE）                    |
| 平台       | Windows 10+ (x64)                                  |

***

## 🏗️ 架构设计

### 整体分层架构

项目采用清晰的分层架构设计：

```
┌─────────────────────────────────────────────────────────────┐
│                    表现层（UI Layer）                        │
│  JavaFX FXML + Controller                                   │
│  MainController | ScratchController | ReportController      │
│  TrashController | SettingsController                       │
└──────────────┬──────────────────────────────────────────────┘
               │ 调用 Service 接口
┌──────────────▼──────────────────────────────────────────────┐
│                    服务层（Service Layer）                    │
│  AiEngine | Encryptor | WeatherEngine                        │
│  ResonanceAudioEngine | ReportEngine | EmotionCapsuleService  │
└──────────────┬──────────────────────────────────────────────┘
               │ 调用数据访问接口
┌──────────────▼──────────────────────────────────────────────┐
│                数据访问层（Data Access Layer）                │
│  LogStoreService (接口)                                      │
│  └─ SqliteLogStore (实现)                                    │
│  EmotionCapsuleService (接口)                                │
│  └─ SqliteCapsuleStore (实现)                                │
└──────────────┬──────────────────────────────────────────────┘
               │ 使用 DatabaseHelper
┌──────────────▼──────────────────────────────────────────────┐
│                数据持久层（Data Persistence Layer）          │
│  DatabaseHelper (连接管理 + DDL)                            │
└──────────────┬──────────────────────────────────────────────┘
               │ 读写 SQLite 数据库
┌──────────────▼──────────────────────────────────────────────┐
│                  数据库层（Database Layer）                  │
│  SQLite Database (mindecho.db)                              │
└─────────────────────────────────────────────────────────────┘
```

### 关键设计决策

1. **单例 Service**：所有 Service 组件以单例形式通过 `ServiceLocator`（简单工厂）管理，避免 Controller 持有多个 Service 实例导致状态不一致。
2. **异步 AI 调用**：AiEngine 的 OpenAI 调用在 JavaFX `Task<AiResponse>` 中执行，结果通过 `Platform.runLater()` 回调至 UI 线程，保证界面不卡顿。
3. **观察者联动**：LogStore 写入成功后通过 `EmotionEventBus`（基于 JavaFX `ObjectProperty`）广播情绪变化事件，WeatherEngine 与 ResonanceAudioEngine 订阅该事件实现自动联动。
4. **备用话术库懒加载**：`fallback_phrases.json` 在 AiEngine 首次 fallback 时一次性解析缓存至内存。

***

## 🗄️ 数据库设计

### SQLite 数据库 Schema

数据库文件位于：`项目 data 目录下 mindecho.db`

**核心表结构**：

```sql
-- 销毁日志表：存储每次倾诉行为的加密记录
CREATE TABLE IF NOT EXISTS destruction_log (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    encrypted_text BLOB    NOT NULL,         -- IV(12B) + ciphertext + GCM Tag(16B)
    ai_response    TEXT    NOT NULL,         -- AI 回应明文，15~40 字
    emotion_label  TEXT    NOT NULL CHECK (emotion_label IN ('ANGER','ANXIETY','SADNESS','CALM')),
    ai_style       TEXT    NOT NULL CHECK (ai_style IN ('GENTLE','SHARP')),
    created_at     TEXT    NOT NULL          -- ISO-8601 格式时间戳
);

-- 情绪胶囊表：存储用户封存的情绪胶囊，支持时间解锁和成长回顾
CREATE TABLE IF NOT EXISTS emotion_capsule (
    id             TEXT    PRIMARY KEY,      -- 胶囊唯一标识（UUID）
    encrypted_content BLOB NOT NULL,         -- 封存内容（AES-256-GCM 加密）
    emotion        TEXT    NOT NULL,         -- 情绪标签
    stress_level   INTEGER NOT NULL,         -- 压力等级 1-5
    created_at     TEXT    NOT NULL,         -- 封存时间
    unlock_at      TEXT    NOT NULL,         -- 解锁时间
    opened         INTEGER NOT NULL DEFAULT 0, -- 是否已解锁（0/1）
    growth_review  TEXT                      -- AI 成长回顾
);

-- 应用元数据表：存储应用版本、数据库版本等元信息
CREATE TABLE IF NOT EXISTS app_metadata (
    key            TEXT    PRIMARY KEY,
    value          TEXT    NOT NULL
);
```

### 加密设计

- 倾诉原文使用 **AES-256-GCM** 加密后存储
- 密钥存储于：`项目 data 目录下 key.bin`（与数据库文件分离）
- 加密数据格式：`[IV(12B)][ciphertext][GCM Tag(16B)]`

***

## 🚀 快速开始

### 当前状态说明

项目目前处于**开发完成阶段**，代码已实现并可运行。详细的技术设计文档请参考 [docs/设计文档.md](docs/设计文档.md)。

### 环境要求

- JDK 21+（推荐 [Eclipse Temurin](https://adoptium.net/)）
- Maven 3.8+

### 运行步骤

```bash
# 1. 克隆仓库
git clone https://github.com/your-username/mindecho.git
cd mindecho

# 2. 配置 API Key（首次运行也可在应用内设置页填写）
# 编辑 src/main/resources/config.properties
# openai.api.key=sk-xxxxxxxxxxxxxxxxxxxxxxxx
# openai.model=gpt-4o-mini
# openai.max_tokens=100

# 3. 编译并运行
mvn clean javafx:run
```

### 打包为独立 .exe

```bash
# 一键打包（需要 JDK 21 自带的 jpackage 工具）
mvn clean package -Ppackage
```

打包完成后，`target/MindEcho-*.exe` 可在任意 Windows 机器上双击运行，无需提前安装 JRE。

***

## 📁 项目结构

### 当前实际目录

```
mindecho-1/
├── README.md                                # 项目说明文档（本文件）
└── docs/
    └── 设计文档.md                          # 完整技术设计文档
```

### 完整目录结构

```
mindecho-1/
├── pom.xml                                  # Maven 构建配置，依赖版本锁定
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mindecho/
│   │   │       ├── App.java                 # JavaFX Application 入口，启动流程编排
│   │   │       ├── ServiceLocator.java      # 单例注册表，管理所有 Service 实例
│   │   │       │
│   │   │       ├── controller/              # 表现层：JavaFX Controller
│   │   │       │   ├── MainController.java      # 情绪粉碎机主页
│   │   │       │   ├── ScratchController.java   # AI 情绪胶囊页面
│   │   │       │   ├── ReportController.java    # 月度报告页面
│   │   │       │   ├── TrashController.java     # 情绪文章推荐页面
│   │   │       │   └── SettingsController.java  # 设置页面
│   │   │       │
│   │   │       ├── service/                 # 服务层接口与实现
│   │   │       │   ├── AiEngineService.java         # AI 引擎接口
│   │   │       │   ├── LogStoreService.java         # 日志存储接口
│   │   │       │   ├── EmotionCapsuleService.java   # 情绪胶囊服务接口
│   │   │       │   ├── EmotionArticleService.java   # 文章推荐服务接口
│   │   │       │   ├── WeatherEngine.java           # 情绪天气引擎
│   │   │       │   ├── ResonanceAudioEngine.java    # 情绪共振音引擎
│   │   │       │   └── ReportEngine.java            # 月度报告引擎
│   │   │       │
│   │   │       ├── model/                   # 数据模型与枚举
│   │   │       │   ├── DestructionLog.java      # 销毁日志实体
│   │   │       │   ├── AiResponse.java          # AI 回应值对象（record）
│   │   │       │   ├── MonthlyReport.java       # 月度报告 DTO（record）
│   │   │       │   ├── EmotionLabel.java        # 枚举：ANGER / ANXIETY / SADNESS / CALM
│   │   │       │   ├── AiStyle.java             # 枚举：GENTLE / SHARP
│   │   │       │   ├── EmotionWeather.java      # 枚举：THUNDERSTORM / CLOUDY / RAINY / SUNNY
│   │   │       │   ├── EmotionArticle.java      # 情绪文章实体
│   │   │       │   └── EmotionCapsule.java      # 情绪胶囊实体
│   │   │       │
│   │   │       ├── util/                    # 工具层
│   │   │       │   ├── DatabaseHelper.java      # SQLite 连接管理，DDL 初始化
│   │   │       │   ├── ConfigManager.java       # config.properties 读写
│   │   │       │   ├── Encryptor.java           # AES-256-GCM 加解密
│   │   │       │   ├── FallbackPhraseStore.java # 备用话术库懒加载
│   │   │       │   ├── EmotionClassifier.java   # 关键词规则情绪分类
│   │   │       │   └── EmotionEventBus.java     # JavaFX ObjectProperty 事件总线
│   │   │       │
│   │   │       └── ui/                      # 自定义 UI 组件
│   │   │           ├── ShredParticleAnimator.java  # 粉碎粒子动画（AnimationTimer）
│   │   │           └── ScratchCardCanvas.java      # 胶囊翻转动画组件
│   │   │
│   │   └── resources/
│   │       ├── com/mindecho/
│   │       │   ├── fxml/
│   │       │   │   ├── main.fxml            # 情绪粉碎机主页布局
│   │       │   │   ├── scratch.fxml         # AI 情绪胶囊页面布局
│   │       │   │   ├── report.fxml          # 月度报告页面布局
│   │       │   │   ├── trash.fxml           # 情绪文章推荐页面布局
│   │       │   │   └── settings.fxml        # 设置页面布局
│   │       │   └── css/
│   │       │       ├── light.css            # 浅色主题「晨雾」样式表
│   │       │       └── dark.css             # 深色主题「夜海」样式表
│   │       ├── audio/
│   │       │   ├── anger.mp3                # 愤怒情绪音场
│   │       │   ├── anxiety.mp3              # 焦虑情绪音场
│   │       │   ├── sadness.mp3              # 悲伤情绪音场
│   │       │   └── calm.mp3                 # 平稳情绪音场
│   │       └── fallback_phrases.json        # 本地备用话术库（GENTLE / SHARP 两组）
│   │
│   └── test/
│       └── java/
│           └── com/mindecho/
│               ├── util/
│               │   ├── EncryptorPropertyTest.java       # P15~P18：加解密属性测试
│               │   ├── DatabaseHelperTest.java          # P14：初始化幂等性
│               │   └── ConfigManagerPropertyTest.java   # P13：配置读写往返
│               ├── service/
│               │   ├── LogStorePropertyTest.java        # P4、P5、P7：日志存储属性测试
│               │   ├── WeatherEnginePropertyTest.java   # P3：天气计算规则
│               │   ├── AiEnginePropertyTest.java        # P1、P2：AI 风格分布 + fallback
│               │   ├── AiEngineIntegrationTest.java     # 集成测试：Mock HTTP 四种场景
│               │   ├── ReportEnginePropertyTest.java    # P9~P11：月度报告属性测试
│               │   ├── ScratchPropertyTest.java         # P6、P8：胶囊配额不变量 + 不暴露原文
│               │   └── ResonanceAudioEngineTest.java    # 单元测试：Mock MediaPlayer
│               └── controller/
│                   ├── TrashControllerPropertyTest.java # P12：文章推荐无副作用
│                   ├── MainControllerTest.java          # TestFX：粉碎机 UI 流程
│                   └── ScratchControllerTest.java       # TestFX：情绪胶囊 UI 状态
│
└── docs/
    ├── 设计文档.md                          # 完整技术设计文档
    └── 需求文档.md                          # 产品需求文档
```

> **包命名约定**：根包 `com.mindecho`，子包按层次划分（`controller` / `service` / `service.impl` / `model` / `util` / `ui`）。FXML 通过 `getClass().getResource()` 加载，资源路径与包结构对齐。

***

## 🔐 隐私说明

MindEcho 从设计层面保护你的隐私：

- ✅ **倾诉原文全程加密**：写入数据库前使用 AES-256-GCM 加密，明文不接触磁盘
- ✅ **密钥本地保管**：密钥存储于 `项目 data 目录下 key.bin`，不上传任何服务器
- ✅ **AI 调用安全**：原文通过 HTTPS 发送至 OpenAI，不经过任何第三方中转
- ✅ **一键彻底清零**：设置页面支持删除全部数据库记录，不留任何痕迹
- ✅ **无云端存储**：所有日志数据仅存本机 SQLite（`项目 data 目录下 mindecho.db`），不同步任何云服务

> ⚠️ `config.properties` 中的 API Key 和 SQLite 数据库文件请勿提交至版本控制。

***

## 🧪 运行测试

```bash
# 运行全部测试（含 jqwik 属性测试 P1~P18、单元测试、集成测试）
mvn test

# 运行指定测试类
mvn test -Dtest=EncryptorPropertyTest

# 查看属性测试详细统计
mvn test -Dtest=WeatherEnginePropertyTest -Djqwik.reporting=detailed

# 只运行服务层检查点
mvn test -Dtest="DatabaseHelperTest,ConfigManagerPropertyTest,EncryptorPropertyTest,LogStorePropertyTest,AiEnginePropertyTest,WeatherEnginePropertyTest"
```

### 正确性属性（Properties）

设计文档中定义了以下 18 个正确性属性，用于保证系统行为：

- **P1-P2**: AI 引擎相关（风格分布、Fallback 话术）
- **P3**: 天气计算规则
- **P4-P5**: 日志存储（往返完整性、全量删除）
- **P6-P8**: 情绪胶囊相关（配额、时间解锁、成长回顾）
- **P9-P11**: 月度报告（情绪排名、统计自洽、调节建议）
- **P12**: 文章推荐无副作用
- **P13-P18**: 工具层（配置管理、数据库初始化、加解密等）

***

## 与传统情绪日记的差异

| 维度   | 传统情绪日记     | MindEcho           |
| ---- | ---------- | ------------------ |
| 存储逻辑 | 明文完整留存所有文字 | 原文加密，可一键彻底清除       |
| 核心目的 | 长期记录、深度反思  | 即时宣泄、消解负面情绪        |
| 回顾方式 | 翻看历史文字复盘   | 情绪胶囊展示 AI 成长回顾，剥离痛苦 |
| 数据存储 | 多为云端       | 全量本地，支持彻底清零        |
| 情绪导向 | 鼓励长期留存     | 主张负面情绪流动释放即可       |
| 启动方式 | 需要浏览器或网络   | 双击 .exe 直接启动       |

***

## 📱 功能截图（待开发）

> *(开发完成后在此处添加截图)*

| 情绪粉碎机 |  情绪胶囊  |  月度报告 |
| :---: | :---: | :---: |
| (待添加) | (待添加) | (待添加) |

***

## 🗺️ 后续迭代计划

- [ ] 本地离线大模型支持（脱离 OpenAI 外网 API）
- [ ] 情绪贴纸：输入时添加简易情绪表情包标签
- [ ] 多语言支持（英语 / 日语界面）
- [x] 粉碎、胶囊配套解压轻音效（已通过情绪共振音系统实现）
- [x] 导出无隐私的月度情绪统计报表（已在月度报告功能实现）

***

## 📄 许可证

[MIT License](LICENSE) · 本项目仅供个人情绪解压使用

***

<div align="center">

**如果这个项目帮你消解了一点点烦恼，欢迎点个 ⭐**

*倾诉即销毁，回望皆释然。*

</div>
