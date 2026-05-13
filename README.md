# 云上柜周 — 项目介绍

## 一、项目概述

### 1.1 项目名称
**云上柜周**（Cloud Wardrobe Weekly）

### 1.2 项目定位
一款由AI全链路驱动的智能衣柜Android应用。用户录入衣物后，AI自动获取当日天气并结合衣柜内容，为用户智能推荐每日穿搭方案。

### 1.3 核心价值
- 解决"今天穿什么"的每日决策难题
- AI根据天气温度、湿度、风力，结合衣物厚薄、材质、风格进行匹配
- 追踪穿着频率，避免连续穿着同一件衣物
- 标记衣物清洗状态，确保推荐的都是可穿衣物

---

## 二、功能详解

### 2.1 衣物管理

| 功能 | 说明 |
|------|------|
| 手动录入 | 输入名称、分类、颜色、材质、厚薄、季节、风格 |
| AI分析 | 调用LLM生成100-200字的专业衣物描述 |
| 拍照录入 | 支持从相册选择衣物照片 |
| 衣柜浏览 | 双列网格展示，支持按分类筛选和名称搜索 |
| 衣物编辑 | 详情页可修改所有字段 |
| 清洗标记 | 标记衣物为"清洗中"，推荐时自动排除 |
| 删除确认 | 删除前弹出确认对话框 |

### 2.2 AI智能推荐

| 功能 | 说明 |
|------|------|
| 天气感知 | 获取当日温度、天气状况、湿度、风力 |
| 智能搭配 | AI综合天气+衣物属性推荐最佳搭配 |
| 穿着追踪 | 按日历计算连续穿着天数 |
| 历史记录 | 保存所有推荐记录，支持回溯查看 |
| 定时推送 | 每天凌晨3点自动生成并推送通知 |

### 2.3 天气数据源

| 来源 | 免费 | 需要密钥 | 备注 |
|------|------|----------|------|
| Open-Meteo | 是 | 否 | **默认**，全球覆盖，无需配置 |
| OpenWeatherMap | 是 | 是 | 需在 openweathermap.org 注册 |
| WeatherAPI.com | 是 | 是 | 需在 weatherapi.com 注册 |
| 和风天气 | 是 | 是 | 需在 qweather.com 注册 |

天气获取采用**三级容错**：主源 → 自动回退 Open-Meteo → 使用默认天气值（25°C晴天），确保AI推荐不因天气失败而中断。

### 2.4 GPS定位
- 启动时自动请求定位权限
- 设置页提供「获取当前位置」按钮
- 一键填入经纬度，无需手动输入坐标
- 使用Google Play Services的FusedLocationProvider

---

## 三、技术架构

### 3.1 技术栈

| 层次 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI框架 | Jetpack Compose + Material3 |
| 数据库 | Room (SQLite) |
| 依赖注入 | Hilt |
| 网络请求 | OkHttp + Retrofit |
| 后台任务 | WorkManager |
| 配置存储 | DataStore Preferences |
| 图片加载 | Coil |
| JSON解析 | Gson |
| 定位服务 | Google Play Services Location |
| 最低SDK | API 26 (Android 8.0) |
| 目标SDK | API 34 (Android 14) |

### 3.2 项目结构

```
app/src/main/java/com/yunshangguizhou/app/
├── YunShangGuiZhouApp.kt          # Application，WorkManager配置
├── MainActivity.kt                 # 单Activity，权限请求
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room数据库
│   │   ├── LocationProvider.kt     # GPS定位
│   │   ├── dao/                    # 数据访问对象
│   │   │   ├── ClothingDao.kt
│   │   │   ├── RecommendationDao.kt
│   │   │   └── WearRecordDao.kt
│   │   └── entity/                 # 数据实体
│   │       ├── ClothingEntity.kt
│   │       ├── RecommendationEntity.kt
│   │       └── WearRecordEntity.kt
│   ├── remote/                     # 远程服务
│   │   ├── AiClient.kt             # AI API客户端
│   │   ├── WeatherClient.kt        # 天气路由器
│   │   ├── OpenMeteoClient.kt      # Open-Meteo天气
│   │   ├── QWeatherClient.kt       # 和风天气
│   │   ├── WeatherPartnerModels.kt # 第三方天气模型
│   │   └── WeatherPartnerApi.kt    # 第三方天气接口
│   └── repository/                 # 数据仓库
│       ├── ClothingRepository.kt
│       ├── RecommendationRepository.kt
│       └── SettingsRepository.kt
├── di/
│   └── AppModule.kt                # Hilt依赖注入模块
├── ui/
│   ├── theme/                      # 主题
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── navigation/
│   │   └── NavGraph.kt             # 导航图
│   ├── home/                       # 首页
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── wardrobe/                   # 衣柜
│   │   ├── WardrobeScreen.kt
│   │   ├── WardrobeViewModel.kt
│   │   ├── AddClothingScreen.kt
│   │   └── AddClothingViewModel.kt
│   ├── detail/                     # 衣物详情
│   │   ├── ClothingDetailScreen.kt
│   │   └── ClothingDetailViewModel.kt
│   ├── settings/                   # 设置
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── recommendation/             # 推荐历史
│   │   └── RecommendationHistoryScreen.kt
│   └── debug/                      # 调试日志
│       └── DebugScreen.kt
└── worker/
    └── DailyRecommendationWorker.kt # 每日推荐Worker
```

### 3.3 数据库设计

**clothing 表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 主键 |
| name | String | 衣物名称 |
| category | String | 分类（上衣/裤子/裙子/外套/鞋子/配饰） |
| color | String | 颜色 |
| material | String | 材质 |
| thickness | String | 厚薄（薄/适中/厚） |
| season | String | 适合季节 |
| style | String | 风格 |
| description | String | AI生成的详细描述 |
| imageUri | String? | 照片URI |
| isWashing | Boolean | 是否清洗中 |
| consecutiveWearDays | Int | 连续穿着天数 |
| lastWornDate | Long? | 上次穿着时间戳 |
| createdAt | Long | 创建时间 |

**recommendation 表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 主键 |
| date | String | 日期 (yyyy-MM-dd) |
| topId | Long? | 推荐上衣ID |
| bottomId | Long? | 推荐下装ID |
| outerwearId | Long? | 推荐外套ID |
| shoesId | Long? | 推荐鞋子ID |
| accessoryId | Long? | 推荐配饰ID |
| weatherDesc | String | 天气描述 |
| temperature | String | 温度范围 |
| reasoning | String | AI推荐理由 |

**wear_record 表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 主键 |
| clothingId | Long | 衣物ID |
| date | String | 穿着日期 |
| wasWorn | Boolean | 是否穿着 |

### 3.4 数据流

```
用户操作 → ViewModel → Repository → DAO/Room
                        ↘ AI/天气 API ↗
                              ↓
                         DebugLog (调试)
```

推荐流程：
```
点击AI推荐 → generateDaily()
  → 读取设置 (DataStore)
  → 获取天气 (主源 → Open-Meteo回退 → 默认值)
  → 查询可用衣物 (Room)
  → 调用AI推荐 (OkHttp)
  → 解析JSON响应
  → 保存推荐+穿着记录 (Room)
  → 计算连续穿着天数
  → 更新UI状态
```

---

## 四、AI模型配置

### 4.1 支持的模型
- DeepSeek (deepseek-v4-flash / deepseek-chat)
- OpenAI (gpt-4o / gpt-4o-mini)
- 任何兼容 OpenAI Chat Completions API 的服务

### 4.2 配置示例

**DeepSeek：**
```
API地址: https://api.deepseek.com
API密钥: sk-xxxxxxxx
模型名称: deepseek-v4-flash
```

**OpenAI：**
```
API地址: https://api.openai.com/v1
API密钥: sk-xxxxxxxx
模型名称: gpt-4o
```

### 4.3 AI调用说明
- **衣物分析**：发送名称/分类/颜色/材质/厚薄/季节/风格 → 返回JSON含详细描述
- **穿搭推荐**：发送天气+已有衣物列表 → 返回JSON含推荐搭配和理由
- 单次请求 max_tokens: 2000，temperature: 0.7
- 超时设置：连接30s，读取90s

---

## 五、权限说明

| 权限 | 用途 |
|------|------|
| INTERNET | API网络请求 |
| ACCESS_COARSE_LOCATION | GPS定位获取经纬度 |
| ACCESS_FINE_LOCATION | 精确GPS定位 |
| CAMERA | 备用（当前使用系统相机） |
| POST_NOTIFICATIONS | 每日推荐推送通知 |
| RECEIVE_BOOT_COMPLETED | 开机后恢复定时任务 |

---

## 六、设计风格

- 深海军蓝主色调 + 暖琥珀辅助色 + 青色点缀
- 圆角卡片设计（16-20dp）
- 天气自适应图标/颜色（晴天橙/雨天蓝/雪天白/雷暴紫）
- 双列自适应网格（手机2列，平板自动增加）
- 语义化色彩系统（成功绿/警告橙/错误红/信息蓝）

---

## 七、已知问题与解决方向

| 问题 | 状态 | 说明 |
|------|------|------|
| DeepSeek v4偶发"image.png"幻觉 | 已加容错 | 检测到异常回复自动用简化prompt重试 |
| 和风天气API密钥未激活 | 待用户激活 | 需在和风控制台开通免费订阅 |
| 模拟器无法在本机运行 | 硬件限制 | 服务器无虚拟化支持，需真机测试 |

---

## 八、构建说明

### 环境要求
- JDK 17+
- Android SDK 34
- Gradle 8.2

### 构建命令
```bash
./gradlew assembleDebug
```

### 输出
`app/build/outputs/apk/debug/app-debug.apk`

---

## 九、版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| v5 | 2026-05-13 | GPS定位、4种天气源、AI容错、日历穿着计算、编辑衣物、衣柜搜索 |
| v4 | 2026-05-13 | UI重设计、WorkManager修复、调试日志、API测试按钮 |
| v3 | 2026-05-13 | 和风天气集成、AI prompt简化、穿搭推荐 |
| v2 | 2026-05-13 | 闪退修复、锁屏问题、UI优化 |
| v1 | 2026-05-13 | 初始版本，基础衣柜功能 |
