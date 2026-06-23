# MindEcho 智愈心海

> 轻量化桌面网页端情绪解压疗愈工具 —— 倾诉即销毁，回望皆释然

---

## 产品简介

**MindEcho 智愈心海** 不是传统情绪日记。它的核心目标不是留存、复盘情绪，而是帮助你**宣泄、消解负面情绪**。

把委屈、焦虑、内耗全部丢进来，AI 用温柔或毒舌的方式回应你，然后一键粉碎——烦恼不需要被永远记住，流动释放就够了。

**目标用户**：学生、职场年轻人，日常有细碎委屈、焦虑、内耗，需要低成本情绪宣泄出口，同时喜欢轻度趣味交互与 AI 陪伴。

---

## 核心功能

### 🔨 情绪粉碎机（主流程）
- 无字数限制的自由输入，无需整理逻辑，碎碎念即可
- AI 随机切换 **温柔治愈 / 清醒毒舌** 双模式，识别情绪类型匹配话术
- 点击「粉碎」触发消散破碎动画，输入框清空，烦恼加密入库
- 首页实时更新「今日已粉碎烦恼数」

### 🪄 回收站刮刮乐（释然回顾）
- 每日 3 次抽取机会，随机取出一条历史销毁记录
- Canvas 全屏刮刮乐，刮开后展示当时的 AI 回复与日期（不展示痛苦原文）
- 刮完可选择永久删除本条记录或直接关闭

### 📊 情绪天气 & 月度报告
- 基于当日情绪标签自动生成情绪天气（晴/微风/小雨/阴天/雷雨）
- 月度报告：每周宣泄频次、高频情绪词、治愈/毒舌占比、可视化图表
- 报告支持截图保存，**不含任何用户原文**

### 🗑️ 无痕宣泄垃圾桶
- 独立轻页面，输入后直接丢弃，**不写入任何数据库，零痕迹**

### 🔒 隐私加密 & 一键清零
- 用户倾诉原文采用 **Fernet 对称加密** 存储，页面不主动读取
- 一键清空全部本地数据，彻底不留痕迹

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Python · FastAPI |
| AI 服务 | OpenAI API（治愈/毒舌双模式封装） |
| 数据库 | SQLite（本地轻量，无需额外部署） |
| 加密 | cryptography · Fernet 对称加密 |
| 前端 | HTML + CSS + 原生 JavaScript |
| 动效 / 图表 | Canvas API · matplotlib |
| 配置管理 | python-dotenv |
| 其他依赖 | python-multipart |

---

## 快速开始

### 环境要求
- Python 3.10+
- 现代浏览器（Chrome / Edge 推荐）

### 安装步骤

```bash
# 1. 克隆仓库
git clone https://github.com/your-username/mindecho.git
cd mindecho

# 2. 安装依赖
pip install -r requirements.txt

# 3. 配置环境变量
cp .env.example .env
# 编辑 .env，填入你的 OpenAI API Key
# OPENAI_API_KEY=sk-xxxx

# 4. 启动后端服务
uvicorn main:app --reload --port 8000

# 5. 用浏览器打开前端页面
# 直接打开 frontend/index.html 即可
```

### 目录结构

```
mindecho/
├── main.py                 # FastAPI 入口
├── routers/
│   ├── crush.py            # 情绪粉碎接口
│   ├── lottery.py          # 刮刮乐接口
│   └── report.py           # 月度报告接口
├── services/
│   ├── ai_service.py       # OpenAI 双模式封装
│   ├── encrypt.py          # Fernet 加密工具
│   └── emotion.py          # 情绪识别 & 天气生成
├── models/
│   └── database.py         # SQLite 数据库模型
├── frontend/
│   ├── index.html          # 首页（粉碎机）
│   ├── lottery.html        # 刮刮乐页面
│   ├── report.html         # 月度报告页面
│   ├── trash.html          # 无痕垃圾桶页面
│   └── settings.html       # 设置页面
├── .env.example
├── requirements.txt
└── README.md
```

---

## 数据库设计

销毁日志表 `destruction_log`：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 自增主键 |
| created_at | DATETIME | 销毁时间戳（精确到秒） |
| ai_response | TEXT | AI 生成回复（明文） |
| encrypted_content | TEXT | 用户原文（Fernet 加密） |
| ai_mode | TEXT | 治愈 / 毒舌 |
| emotion_tag | TEXT | 焦虑 / 委屈 / 愤怒 / 疲惫 / 失落 |

所有数据仅存储在本地 SQLite 文件，**不上传任何服务器**。

---

## 隐私说明

- 用户倾诉原文全程本地加密，不以明文形式写入磁盘
- 刮刮乐仅展示 AI 回复与日期，不读取加密原文
- 月度报告不包含任何用户原始内容
- 支持一键彻底清空全部本地数据

---

## 与传统情绪日记的差异

| 维度 | 传统情绪日记 | MindEcho |
|------|-------------|----------|
| 存储逻辑 | 明文完整留存所有文字 | 原文加密，可一键彻底清除 |
| 核心目的 | 长期记录、深度反思 | 即时宣泄、消解负面情绪 |
| 回顾方式 | 翻看历史文字复盘 | 刮刮乐展示中立 AI 回复，剥离痛苦 |
| 数据存储 | 多为云端 | 全量本地，支持彻底清零 |
| 情绪导向 | 鼓励长期留存 | 主张负面情绪流动释放即可 |

---

## 后续迭代计划

- [ ] 支持本地离线大模型，脱离 OpenAI 外网依赖
- [ ] 粉碎 & 刮刮乐配套解压轻音效
- [ ] 输入时添加简易情绪表情包标签
- [ ] 打包为桌面 .exe，无需浏览器直接启动
- [ ] 导出无隐私内容的月度情绪统计报表

---

## License

MIT License · 本项目仅供个人情绪解压使用，请勿用于任何商业场景。

---

<p align="center">
  <em>烦恼不需要被永远记住 · 流动释放就够了</em>
</p>
