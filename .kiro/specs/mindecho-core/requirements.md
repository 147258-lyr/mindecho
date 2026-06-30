# Requirements Document

## Introduction

MindEcho「智愈心海」是一款轻量化桌面情绪解压疗愈工具，面向学生与职场年轻人，核心主张是「负面情绪不需要被永久记住，流动释放才是真正的疗愈」。用户可双击独立 `.exe` 直接启动，无需安装额外运行时。系统基于 JavaFX 21 + Maven 构建，通过 OpenAI API 提供 AI 情绪回应，使用 AES-256-GCM 加密本地 SQLite 数据库，所有数据仅存本机，不涉及任何云端上传。

本文档覆盖七大核心功能模块：情绪粉碎机、隐私加密、情绪天气、回收站刮刮乐、月度情绪报告、解压垃圾桶（无痕模式）、情绪共振音系统。

---

## Glossary

- **系统（System）**：MindEcho 桌面应用整体。
- **粉碎机（Shredder）**：情绪粉碎机模块，负责接收用户倾诉文本、触发粉碎动画并获取 AI 回应。
- **AI 引擎（AiEngine）**：负责调用 OpenAI API 或本地备用话术库生成情绪回应的服务组件。
- **加密器（Encryptor）**：使用 AES-256-GCM 算法对倾诉原文进行加解密的服务组件。
- **日志存储（LogStore）**：基于 SQLite 的本地加密日志持久化服务组件。
- **情绪天气引擎（WeatherEngine）**：根据当日情绪标签分布计算天气类型的服务组件。
- **音频引擎（ResonanceAudioEngine）**：基于 JavaFX MediaPlayer 管理情绪共振音景的服务组件。
- **月度报告引擎（ReportEngine）**：聚合当月情绪数据并生成统计报告的服务组件。
- **刮刮乐（Scratch）**：回收站刮刮乐功能模块，供用户刮开查看历史 AI 回应。
- **垃圾桶（Trash）**：解压垃圾桶（无痕模式）功能模块。
- **情绪标签（EmotionLabel）**：枚举类型，取值为：`ANGER`（愤怒）、`ANXIETY`（焦虑）、`SADNESS`（委屈/悲伤）、`CALM`（平稳）。
- **天气类型（EmotionWeather）**：枚举类型，取值为：`THUNDERSTORM`（⛈️ 雷雨）、`CLOUDY`（☁️ 阴天）、`RAINY`（🌧️ 小雨）、`SUNNY`（🌤️ 晴）。
- **AI 风格（AiStyle）**：枚举类型，取值为：`GENTLE`（温柔治愈）、`SHARP`（清醒毒舌）。
- **销毁日志（DestructionLog）**：记录一次倾诉行为的实体，包含加密原文、AI 回应、情绪标签、AI 风格、时间戳等字段。
- **配置管理器（ConfigManager）**：读写 `.properties` 本地配置文件的工具组件。
- **数据库助手（DatabaseHelper）**：管理 SQLite 连接生命周期的工具组件。

---

## Requirements

### Requirement 1: 情绪粉碎机——文本倾诉与粉碎动画

**User Story:** 作为一名有负面情绪的用户，我希望在一个无限制的输入框中自由写下心中所想，然后触发粉碎动画让文字消散，从而获得物理层面的情绪释放感。

#### Acceptance Criteria

1. THE 粉碎机 SHALL 提供一个无字数上限、无格式限制的多行文本输入框，供用户自由输入倾诉内容。
2. WHEN 用户点击「粉碎」按钮，THE 粉碎机 SHALL 在输入框区域触发文字消散破碎粒子动画，动画时长不超过 3 秒。
3. WHEN 粉碎动画播放完毕，THE 粉碎机 SHALL 清空输入框内容并将焦点重置至输入框。
4. IF 用户在输入框为空时点击「粉碎」按钮，THEN THE 粉碎机 SHALL 保持输入框不变并不触发任何动画或 AI 请求。

---

### Requirement 2: 情绪粉碎机——AI 双模式回应

**User Story:** 作为用户，我希望在粉碎动画后收到一条 AI 情绪回应（温柔治愈或清醒毒舌），感受被承接与理解，而不是情绪泄露后的空白。

#### Acceptance Criteria

1. WHEN 用户点击「粉碎」按钮且输入框非空，THE AI 引擎 SHALL 以 `GENTLE`（温柔治愈）或 `SHARP`（清醒毒舌）其中一种风格生成 AI 回应，两种风格随机选取，各占约 50% 概率。
2. THE AI 引擎 SHALL 生成字数在 15 至 40 字（含）之间的中文 AI 回应。
3. WHEN 调用 OpenAI API 成功，THE AI 引擎 SHALL 将 API 返回的回应文本作为最终 AI 回应展示给用户。
4. IF OpenAI API 调用失败（包括网络超时、HTTP 错误、响应格式异常），THEN THE AI 引擎 SHALL 从本地 `fallback_phrases.json` 备用话术库中随机选取一条对应风格的话术作为回应，确保回应区域不为空白。
5. THE AI 引擎 SHALL 在调用 OpenAI API 时设置不超过 10 秒的请求超时。
6. WHEN AI 回应生成完毕，THE 粉碎机 SHALL 在界面显著位置展示该 AI 回应文本及其对应的 AI 风格标签。

---

### Requirement 3: 情绪粉碎机——情绪标签识别与日志存储

**User Story:** 作为用户，我希望每次倾诉都被自动打上情绪标签并加密保存，以便后续情绪天气、刮刮乐、月度报告等功能使用这些数据。

#### Acceptance Criteria

1. WHEN 用户完成一次倾诉并获得 AI 回应，THE AI 引擎 SHALL 根据倾诉内容自动识别并分配一个情绪标签（`ANGER`、`ANXIETY`、`SADNESS`、`CALM` 之一）。
2. WHEN 情绪标签确定后，THE 加密器 SHALL 使用 AES-256-GCM 算法对倾诉原文进行加密，生成密文与认证标签。
3. WHEN 加密完成，THE 日志存储 SHALL 将包含加密原文密文、AI 回应明文、情绪标签、AI 风格、时间戳的销毁日志写入本地 SQLite 数据库，不将原文明文写入任何磁盘文件。
4. THE 加密器 SHALL 使用存储于本机用户目录的本地密钥文件进行加解密，密钥文件不上传至任何服务器或版本控制系统。
5. IF 数据库写入失败，THEN THE 日志存储 SHALL 记录错误日志至本地日志文件，并向用户展示友好的错误提示，不影响 AI 回应的正常显示。

---

### Requirement 4: 隐私加密——全局一键销毁

**User Story:** 作为注重隐私的用户，我希望能够一键彻底清除所有倾诉记录，不留任何痕迹。

#### Acceptance Criteria

1. THE 系统 SHALL 在设置页面提供「一键销毁全部数据」操作入口。
2. WHEN 用户触发「一键销毁」操作，THE 系统 SHALL 弹出二次确认对话框，明确提示此操作将永久删除所有日志记录且不可恢复。
3. WHEN 用户在二次确认对话框中确认，THE 日志存储 SHALL 清空 SQLite 数据库中的全部销毁日志记录。
4. WHEN 全部数据销毁完成，THE 系统 SHALL 向用户展示销毁成功的确认提示，并将情绪天气、刮刮乐剩余次数等依赖当日数据的界面元素重置为初始状态。
5. IF 用户在二次确认对话框中取消操作，THEN THE 系统 SHALL 关闭对话框，不执行任何删除操作。

---

### Requirement 5: 情绪天气——实时气象动画展示

**User Story:** 作为用户，我希望在首页顶部看到一个根据我今天情绪状态自动变化的天气动画，直观感受自己当前的情绪氛围。

#### Acceptance Criteria

1. THE 情绪天气引擎 SHALL 根据当日（自然日 00:00:00 至 23:59:59）所有销毁日志的情绪标签分布计算一个天气类型。
2. THE 情绪天气引擎 SHALL 按如下规则确定天气类型：当 `ANGER` 标签占当日总数超过 40% 时映射为 `THUNDERSTORM`（⛈️ 雷雨）；当 `ANXIETY` 标签为当日最高频标签且不满足雷雨条件时映射为 `CLOUDY`（☁️ 阴天）；当 `SADNESS` 标签存在且不满足上述条件时映射为 `RAINY`（🌧️ 小雨）；其余情况映射为 `SUNNY`（🌤️ 晴）。
3. WHEN 应用启动或新的销毁日志写入完成，THE 系统 SHALL 重新计算当日天气类型并更新首页顶部气象动画。
4. WHILE 当日无任何销毁日志，THE 系统 SHALL 在首页顶部展示 `SUNNY`（🌤️ 晴）默认天气动画。
5. THE 系统 SHALL 在首页顶部以动态动画形式渲染天气类型对应的气象效果（雷雨、阴天、小雨、晴四种视觉状态）。

---

### Requirement 6: 回收站刮刮乐——每日限次随机回顾

**User Story:** 作为用户，我希望偶尔能够刮开一张「过去的 AI 回应」，体验「当时困住我的事情，现在已不值一提」的释然感，同时不被迫回顾痛苦的原文。

#### Acceptance Criteria

1. THE 刮刮乐 SHALL 为每位用户（本机）每日提供且仅提供 3 次刮刮乐机会，按自然日重置。
2. WHEN 用户进入刮刮乐页面，THE 刮刮乐 SHALL 展示当日剩余次数。
3. WHEN 用户点击「开始刮」且剩余次数大于 0，THE 刮刮乐 SHALL 从全部历史销毁日志中随机抽取一条记录，并以 JavaFX Canvas 刮刮乐交互形式展示一张待刮开的卡片。
4. WHEN 用户通过鼠标/触控操作刮开卡片，THE 刮刮乐 SHALL 仅展示该条记录的 AI 回应文本与日期，不展示倾诉原文（密文或明文均不展示）。
5. WHEN 一次刮刮乐完成（卡片刮开超过 80% 面积），THE 刮刮乐 SHALL 将当日剩余次数减 1 并持久化至本地。
6. IF 当日剩余次数为 0，THEN THE 刮刮乐 SHALL 禁用「开始刮」按钮并展示「今日次数已用完，明天再来」提示。
7. IF 历史销毁日志总数为 0，THEN THE 刮刮乐 SHALL 展示「还没有记录哦，先去倾诉一次吧」提示，不触发随机抽取。

---

### Requirement 7: 月度情绪报告——数据统计与可视化

**User Story:** 作为用户，我希望每月能够查看自己的情绪统计报告，了解情绪分布趋势，获得个性化调节建议，并能导出图片留存。

#### Acceptance Criteria

1. THE 月度报告引擎 SHALL 聚合当自然月（第 1 日至最后 1 日）范围内的全部销毁日志，统计各情绪标签出现频次并按频次降序排列，生成高频情绪排名列表。
2. THE 月度报告引擎 SHALL 以每周为单位统计倾诉次数，使用 JavaFX Canvas 自绘折线图展示全月每周宣泄频次趋势。
3. THE 月度报告引擎 SHALL 统计本月 `GENTLE`（温柔治愈）与 `SHARP`（清醒毒舌）回应各自占比，使用 JavaFX Canvas 自绘饼图展示治愈/毒舌比例。
4. THE 月度报告引擎 SHALL 根据高频情绪标签自动生成不少于 1 条个性化文字调节建议，建议内容与情绪标签对应（如高频 `ANXIETY` 对应减压建议，高频 `ANGER` 对应情绪疏导建议）。
5. WHEN 用户点击「导出报告」，THE 系统 SHALL 使用 JavaFX `WritableImage` 将报告页面渲染为 PNG 图片并保存至用户指定路径，导出图片不包含任何倾诉原文（明文或密文）。
6. WHILE 当月无任何销毁日志，THE 系统 SHALL 在报告页面展示「本月暂无数据」提示，不渲染空图表。

---

### Requirement 8: 解压垃圾桶（无痕模式）

**User Story:** 作为用户，当我有极端情绪需要发泄但不想留下任何记录时，我希望能够在一个完全无痕的模式下倾诉，输入内容在提交后即刻彻底消失。

#### Acceptance Criteria

1. THE 系统 SHALL 提供独立的解压垃圾桶页面，与情绪粉碎机页面相互独立。
2. THE 垃圾桶 SHALL 提供与情绪粉碎机相同规格的无字数上限自由文本输入框。
3. WHEN 用户在解压垃圾桶页面提交内容，THE 垃圾桶 SHALL 清空输入框，不将任何内容写入 SQLite 数据库、本地文件或任何持久化存储介质。
4. THE 垃圾桶 SHALL 不调用 AI 引擎、不生成情绪标签、不触发情绪天气或刮刮乐相关逻辑。
5. WHEN 用户提交内容，THE 垃圾桶 SHALL 播放视觉反馈动效（如内容消失效果），向用户确认内容已销毁。

---

### Requirement 9: 情绪共振音系统——音景自动切换

**User Story:** 作为用户，我希望应用能根据我当前的情绪状态自动播放对应的环境音景，将情绪体验从视觉延伸至听觉，增强情绪释放的沉浸感。

#### Acceptance Criteria

1. THE 音频引擎 SHALL 维护四种情绪音场映射：`ANXIETY` → 低频心跳 + 风声音景（`anxiety.mp3`）、`SADNESS` → 雨声 + 空间混响音景（`sadness.mp3`）、`ANGER` → 低沉鼓点 + 压迫性风声音景（`anger.mp3`）、`CALM` → 自然白噪音 + 轻风音景（`calm.mp3`）。
2. WHEN 情绪标签发生变化（包括新的销毁日志写入后天气类型重新计算），THE 音频引擎 SHALL 自动切换至与当前情绪天气类型对应的音场。
3. THE 音频引擎 SHALL 在音场切换时对当前播放的音频执行渐进式淡出（fade out，持续时间 1~3 秒），并在淡出完成后对新音场执行渐进式淡入（fade in，持续时间 1~3 秒），避免突兀切换。
4. THE 音频引擎 SHALL 基于 JavaFX `MediaPlayer` 实现，不引入任何额外第三方音频依赖库。
5. WHEN 应用启动，THE 音频引擎 SHALL 根据当前情绪天气类型自动播放对应音场音频。
6. THE 系统 SHALL 在设置页面提供音效开关，WHEN 用户关闭音效，THE 音频引擎 SHALL 停止播放并不再自动切换音场；WHEN 用户重新开启音效，THE 音频引擎 SHALL 恢复播放当前情绪天气对应音场。
7. IF 音频文件加载失败，THEN THE 音频引擎 SHALL 记录错误日志并静默跳过该音场，不向用户展示错误弹窗，不影响其他功能模块正常运行。

---

### Requirement 10: 系统配置与应用启动

**User Story:** 作为用户，我希望应用能双击直接启动、无需手动安装 JRE，并能通过配置文件管理 API Key 等参数。

#### Acceptance Criteria

1. THE 系统 SHALL 通过 `jpackage` 打包为独立 Windows `.exe` 安装包，捆绑 JRE，用户双击即可启动，无需提前安装 Java 运行时。
2. THE 配置管理器 SHALL 从 `config.properties` 文件读取 OpenAI API Key、模型名称、最大 token 数等配置项。
3. IF `config.properties` 文件中 API Key 为空或文件不存在，THEN THE 系统 SHALL 在首次启动时引导用户通过设置页面配置 API Key，并在 API Key 未配置时禁用 AI 引擎的在线调用，回退至本地备用话术库。
4. THE 数据库助手 SHALL 在应用首次启动时自动初始化 SQLite 数据库文件与所需数据表结构，无需用户手动操作。
5. THE 系统 SHALL 支持浅色主题与深色主题切换，主题配置持久化至 `config.properties`。

---

### Requirement 11: 加解密正确性——往返属性

**User Story:** 作为开发者，我需要保证 AES-256-GCM 加密器的加解密往返正确性，确保任意原文经加密后再解密能还原原始内容。

#### Acceptance Criteria

1. FOR ALL 非空字符串原文，THE 加密器 SHALL 保证对原文加密后再解密所得字符串与原文完全相同（加解密往返属性）。
2. FOR ALL 非空字符串原文，THE 加密器 SHALL 对同一原文的两次加密操作生成不同的密文（IV 随机性保证），但解密结果均与原文相同。
3. IF 解密时提供的密钥与加密时使用的密钥不同，THEN THE 加密器 SHALL 抛出认证失败异常，不返回任何明文数据。
4. IF 解密时提供的密文内容被篡改（任意字节变更），THEN THE 加密器 SHALL 抛出认证失败异常（GCM 完整性验证失败），不返回任何明文数据。
