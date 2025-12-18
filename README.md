# MCP Android ADB Server - 简易版豆包手机智能助手

🔥 **AI驱动的Android设备智能管家** - 像豆包一样理解和控制你的手机！🔥

这是一个基于Java开发的智能Android ADB服务器，集成了先进的AI视觉模型，能够**看懂**手机屏幕并通过自然语言指令控制设备。它就像是一个运行在电脑上的"简易版豆包手机助手"，让你通过命令行与手机进行智能交互。

## 🚀 核心亮点

### 🤖 AI视觉理解
- **看懂屏幕内容**：集成GPT-4V/Qwen2-VL等先进AI视觉模型，能够像人一样理解手机屏幕上的文字、图片和界面元素
- **智能元素识别**：自动识别屏幕上的按钮、输入框、应用图标等UI元素，并标注其位置和功能
- **场景化描述**：根据上下文智能描述当前手机界面场景（如"正在微信聊天界面"、"显示淘宝商品详情"）

### 🎯 自然语言控制
- **语音级指令**：支持类似自然语言的命令，例如"点击微信图标"、"输入验证码123456"、"返回主屏幕"
- **多步任务执行**：能够理解和执行复杂的多步骤指令（如"打开相机，拍照，然后分享到朋友圈"）
- **智能决策**：遇到歧义时能根据上下文做出合理推断

### 📱 全功能设备管理
- **应用管家**：安装/卸载/启动/停止应用，管理应用权限和数据
- **屏幕控制**：解锁/锁定屏幕，获取分辨率，实时截图
- **输入模拟**：模拟文本输入、按键操作、点击滑动、复杂手势
- **设备监控**：获取设备信息、运行状态、电池电量等系统参数

## 🛠️ 技术架构

### AI核心引擎
- **视觉模型集成**：支持OpenAI GPT-4V、Qwen2-VL等多模型接入
- **上下文理解**：基于MCP协议的模型上下文管理
- **自然语言处理**：智能解析用户指令，生成执行计划

### 技术栈
- **Java 11**：高性能后端实现
- **Appium Java Client**：稳定的Android设备通信层
- **OpenAI API**：AI视觉模型接口
- **SLF4J/Logback**：企业级日志系统
- **Maven**：标准化项目构建

## 📋 系统要求

- **Java 11+**：运行环境
- **Android设备**：开启USB调试模式（Android 5.0+）
- **ADB工具**：Android SDK Platform Tools
- **AI API密钥**：OpenAI API密钥（用于视觉功能，可选）

## 📦 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd mcp-android-adb-server
```

### 2. 构建智能助手

```bash
mvn clean package
```

构建完成后，智能助手JAR包将位于 `target` 目录下。

### 3. 配置AI密钥（可选）

```bash
export OPENAI_API_KEY="your-openai-api-key"
export VISION_MODEL="qwen2.5-vl-7b-instruct"  # 或使用gpt-4-vision-preview
```

### 4. 启动智能助手

```bash
java -jar target/mcp-android-adb-server-1.0.0-jar-with-dependencies.jar -d <device-id>
```

## 🎮 使用示例

### 📱 基础操作

```bash
# 启动智能助手
java -jar mcp-android-adb-server.jar -d emulator-5554

# 查看当前屏幕内容
> describe-screen

# 识别并点击微信图标
> find-and-click "微信"

# 输入文本
> input-text "你好，这是豆包手机助手！"

# 滑动屏幕
> swipe 100 500 900 500
```

### 🤖 AI增强功能

```bash
# 描述当前界面
> describe-screen
# AI回复："当前显示微信聊天界面，顶部是聊天对象'张三'，中间是聊天记录，底部是输入框"

# 智能查找元素
> find-element "发送按钮"
# AI回复："找到发送按钮，位置：(900, 1700)，大小：(100x50)"

# 执行复杂任务
> execute "打开设置，找到Wi-Fi选项，连接到名为'HomeWiFi'的网络"
# 助手自动执行：解锁屏幕 → 打开设置 → 点击Wi-Fi → 选择HomeWiFi → 等待连接完成
```

## 🎨 功能列表

### 🔍 AI视觉功能
- `describe-screen`：用自然语言描述当前屏幕内容
- `annotate-screen`：标注屏幕上的所有可交互元素
- `find-element <description>`：根据描述查找特定屏幕元素
- `find-and-click <description>`：查找并点击指定元素

### 📱 设备管理
- `get-device-info`：获取设备详细信息
- `get-battery-status`：查看电池状态
- `unlock-screen`：解锁屏幕
- `lock-screen`：锁定屏幕

### 📲 应用管理
- `install-app <apk-path>`：安装应用
- `uninstall-app <package-name>`：卸载应用
- `launch-app <package-name>`：启动应用
- `stop-app <package-name>`：停止应用
- `clear-app-data <package-name>`：清除应用数据
- `get-running-apps`：获取运行中应用列表

### ⌨️ 输入控制
- `input-text <text>`：输入文本
- `press-key <keycode>`：模拟按键
- `tap <x> <y>`：模拟点击
- `swipe <x1> <y1> <x2> <y2>`：模拟滑动
- `long-press <x> <y> <duration>`：模拟长按
- `pinch <x> <y> <scale>`：模拟双指缩放

### 📸 屏幕操作
- `screenshot`：截图并保存
- `get-screen-resolution`：获取屏幕分辨率

## 🔧 配置选项

### 环境变量

| 变量名 | 描述 | 默认值 | 用途 |
|-------|------|-------|------|
| `DEVICE_ID` | 设备ID（`adb devices`获取） | 无 | 必需，指定控制的设备 |
| `ADB_PATH` | ADB工具路径 | `adb` | 可选，指定ADB位置 |
| `OPENAI_API_KEY` | AI API密钥 | 无 | 可选，启用视觉功能 |
| `VISION_MODEL` | 视觉模型名称 | `qwen2.5-vl-7b-instruct` | 可选，选择AI模型 |
| `API_BASE_URL` | API基础URL | `https://api.openai.com/v1` | 可选，自定义API地址 |

### 命令行参数

```bash
java -jar mcp-android-adb-server.jar \
  -d <device-id>      # 设备ID
  -a <adb-path>       # ADB路径
  -k <api-key>        # AI API密钥
  -m <vision-model>   # 视觉模型
  -u <api-url>        # API基础URL
```

## 🎯 应用场景

### 📱 自动化测试
- 智能识别UI元素，无需硬编码坐标
- 自然语言描述测试步骤，降低维护成本

### 🤖 辅助功能
- 为视障用户提供屏幕内容语音描述
- 简化复杂的手机操作流程

### 🔬 研究开发
- AI视觉模型在移动界面理解中的应用
- 自然语言指令与设备控制的桥梁

### 🎮 游戏辅助
- 智能识别游戏界面元素
- 自动化完成重复操作

## 🌟 未来规划

- [ ] **语音交互**：支持语音输入输出
- [ ] **多设备管理**：同时控制多台Android设备
- [ ] **更强大的AI**：支持本地部署的视觉模型
- [ ] **Web界面**：提供可视化控制面板
- [ ] **任务自动化**：支持录制和回放操作序列

## 📄 许可证

MIT License - 详见LICENSE文件

## 🤝 贡献

欢迎提交Issue和Pull Request，一起打造更强大的豆包手机助手！

---

💡 **提示**：本项目需要配合Android设备使用，确保已正确安装ADB并开启USB调试模式。视觉功能需要有效的OpenAI API密钥。

📞 **技术支持**：遇到问题？查看项目文档或提交Issue获取帮助。

