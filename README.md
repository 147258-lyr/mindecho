# MindEcho 「智愈心海」

> **倾诉即销毁，回望皆释然** — 轻量化桌面情绪解压疗愈工具

[![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?logo=java)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey?logo=windows)](https://www.microsoft.com/windows)

---

## 📖 项目简介

MindEcho「智愈心海」不是传统情绪日记。它的核心主张是：**负面情绪不需要被永久记住，流动释放才是真正的疗愈**。

这是一款可以**双击直接启动**的桌面程序，你说出来，AI 承接，然后一起销毁。治愈或毒舌的回应，粉碎动画里消散的文字，随时可以清零的加密痕迹。

**目标用户**：学生、职场年轻人，日常有细碎委屈、焦虑、内耗，需要低成本情绪宣泄出口的人群。

---

## ✨ 核心功能

### 🔥 情绪粉碎机
- 无字数、无格式限制的自由倾诉输入框
- 点击「粉碎」触发文字消散破碎动画，带来物理层面的情绪释放感
- AI 双模式随机回应：**温柔治愈** 或 **清醒毒舌**，15~40 字，精准承接情绪
- OpenAI API 异常时自动切换本地备用话术库兜底，永不空白

### 🔒 隐私加密
- 倾诉原文采用 **AES-256-GCM** 加密存入本地 SQLite，明文绝不落盘
- 全局一键销毁：彻底清空所有日志与加密数据，不留痕迹
- 所有数据仅存本机，无任何云端上传

### 🌤️ 情绪天气
- 首页顶部实时情绪气象动画，基于当日情绪标签分布自动计算
- 大量愤怒 → ⛈️ 雷雨 | 持续焦虑 → ☁️ 阴天乌云 | 少量委屈 → 🌧️ 小雨 | 平稳 → 🌤️ 微风晴

### 🎰 回收站刮刮乐
- 每日 3 次机会，随机抽取一条历史销毁记录
- JavaFX Canvas 刮开交互，揭示的只是 AI 的回应与日期，不暴露痛苦原文
- 「原来当时困住你的小事，现在已经不值一提啦」

### 📊 月度情绪报告
- 自动聚合当月数据：高频情绪排名、每周宣泄频次折线图、治愈/毒舌占比饼图
- 个性化文字调节建议，报告支持导出为 PNG（不含任何原文内容）

### 🗑️ 解压垃圾桶（无痕模式）
- 独立页面，输入后直接销毁，**不写入任何数据库**，适合不想留下任何痕迹的极端情绪

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 桌面 UI 框架 | JavaFX 21 |
| 构建工具 | Maven |
| AI 服务 | OpenAI API（通过 OkHttp + Gson 调用） |
| 加密方案 | AES-256-GCM（`javax.crypto` 标准库） |
| 数据库 | SQLite（`sqlite-jdbc` 驱动，单文件零配置） |
| 图表渲染 | JavaFX Canvas（自绘折线图 / 饼图） |
| 截图导出 | JavaFX `WritableImage` → PNG |
| 配置管理 | `.properties` 本地配置文件 |
| 打包发布 | `jpackage`（打包为独立 `.exe` 安装包） |

---

## 🚀 快速开始

### 环境要求

- JDK 17+（推荐 [Eclipse Temurin](https://adoptium.net/)）
- Maven 3.8+

### 安装步骤

```bash
# 1. 克隆仓库
git clone https://github.com/your-username/mindecho.git
cd mindecho

# 2. 配置 API Key
# 编辑 src/main/resources/config.properties
# openai.api.key=sk-xxxxxxxxxxxxxxxxxxxxxxxx
# openai.model=gpt-4o-mini
# openai.max_tokens=100

# 3. 编译并运行
mvn clean javafx:run
```

### 打包为独立 .exe

```bash
# 打包 jar
mvn clean package -DskipTests

# 使用 jpackage 生成 Windows 安装包
jpackage --input target/ ^
         --name MindEcho ^
         --main-jar mindecho-1.0.0.jar ^
         --type exe ^
         --win-shortcut ^
         --win-menu ^
         --icon src/main/resources/icon.ico
```

打包完成后，`MindEcho-1.0.0.exe` 即可在任意 Windows 机器上双击运行，无需提前安装 JRE。

---

## 📁 项目结构

```
mindecho/
├── pom.xml                          # Maven 构建配置
├── src/
│   ├── main/
│   │   ├── java/com/mindecho/
│   │   │   ├── App.java             # JavaFX 应用入口
│   │   │   ├── controller/          # FXML 页面控制器
│   │   │   │   ├── MainController.java      # 主页（粉碎机）
│   │   │   │   ├── ScratchController.java   # 刮刮乐
│   │   │   │   ├── ReportController.java    # 月度报告
│   │   │   │   ├── TrashController.java     # 解压垃圾桶
│   │   │   │   └── SettingsController.java  # 系统设置
│   │   │   ├── service/
│   │   │   │   ├── AiEngine.java            # AI 回应生成器
│   │   │   │   ├── Encryptor.java           # AES-256-GCM 加密
│   │   │   │   ├── LogStore.java            # SQLite 日志 CRUD
│   │   │   │   ├── WeatherEngine.java       # 情绪天气计算
│   │   │   │   └── ReportEngine.java        # 月度统计报告
│   │   │   ├── model/
│   │   │   │   ├── DestructionLog.java      # 日志实体
│   │   │   │   ├── EmotionLabel.java        # 情绪标签枚举
│   │   │   │   ├── AiStyle.java             # AI 风格枚举
│   │   │   │   └── EmotionWeather.java      # 天气类型枚举
│   │   │   └── util/
│   │   │       ├── DatabaseHelper.java      # SQLite 连接管理
│   │   │       └── ConfigManager.java       # 配置文件读写
│   │   └── resources/
│   │       ├── fxml/                        # 页面布局文件
│   │       │   ├── main.fxml
│   │       │   ├── scratch.fxml
│   │       │   ├── report.fxml
│   │       │   ├── trash.fxml
│   │       │   └── settings.fxml
│   │       ├── css/
│   │       │   ├── light-theme.css          # 浅色主题
│   │       │   └── dark-theme.css           # 深色主题
│   │       ├── fallback_phrases.json        # 本地备用话术库
│   │       ├── icon.ico                     # 应用图标
│   │       └── config.properties            # 应用配置模板
│   └── test/java/com/mindecho/
│       ├── EncryptorTest.java       # 加解密往返测试
│       ├── AiEngineTest.java        # AI 回应字段测试
│       ├── LogStoreTest.java        # 日志 CRUD 测试
│       ├── WeatherEngineTest.java   # 天气计算规则测试
│       └── ReportEngineTest.java    # 月度统计自洽测试
└── README.md
```

---

## 🔐 隐私说明

MindEcho 从设计层面保护你的隐私：

- ✅ **倾诉原文全程加密**：写入数据库前使用 AES-256-GCM 加密，明文不接触磁盘
- ✅ **密钥本地保管**：加密密钥存储于本机用户目录，不上传任何服务器
- ✅ **AI 调用安全**：原文通过 HTTPS 发送至 OpenAI，不经过任何第三方中转
- ✅ **一键彻底清零**：设置页面支持删除全部数据库记录，不留任何痕迹
- ✅ **无云端存储**：所有日志数据仅存本机 SQLite，不同步至任何云服务

> ⚠️ `config.properties` 中的 API Key 和 SQLite 数据库文件已加入 `.gitignore`，请勿手动提交至版本控制。

---

## 🧪 运行测试

```bash
# 运行全部测试
mvn test

# 运行指定测试类
mvn test -Dtest=EncryptorTest
```

---

## 与传统情绪日记的差异

| 维度 | 传统情绪日记 | MindEcho |
|------|-------------|----------|
| 存储逻辑 | 明文完整留存所有文字 | 原文加密，可一键彻底清除 |
| 核心目的 | 长期记录、深度反思 | 即时宣泄、消解负面情绪 |
| 回顾方式 | 翻看历史文字复盘 | 刮刮乐展示中立 AI 回复，剥离痛苦 |
| 数据存储 | 多为云端 | 全量本地，支持彻底清零 |
| 情绪导向 | 鼓励长期留存 | 主张负面情绪流动释放即可 |
| 启动方式 | 需要浏览器或网络 | 双击 .exe 直接启动 |

---

## 📱 功能截图

> *(开发完成后在此处添加截图)*

| 情绪粉碎机 | 刮刮乐 | 月度报告 |
|:---:|:---:|:---:|
| ![shredder](docs/screenshots/shredder.png) | ![scratch](docs/screenshots/scratch.png) | ![report](docs/screenshots/report.png) |

---

## 🗺️ 后续迭代计划

- [ ] 本地离线大模型支持（脱离 OpenAI 外网 API）
- [ ] 粉碎、刮刮乐配套解压轻音效
- [ ] 情绪贴纸：输入时添加简易情绪表情包标签
- [ ] 导出无隐私的月度情绪统计报表（不含任何个人倾诉内容）

---

## 📄 许可证

[MIT License](LICENSE) · 本项目仅供个人情绪解压使用

---

<div align="center">

**如果这个项目帮你消解了一点点烦恼，欢迎点个 ⭐**

*倾诉即销毁，回望皆释然。*

</div>
