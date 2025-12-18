# MCP Easy Doubao Phone AI Assistant

🔥 **AI-Driven Android Device Intelligent Manager** - Understand and control your phone like Doubao! 🔥

This is an AI-powered Android ADB server developed in Java, integrated with advanced AI vision models. It can **see** your phone screen and control the device through natural language commands. It's like a "simplified Doubao phone assistant" running on your computer, enabling intelligent interaction with your phone via command line.

## 🚀 Core Highlights

### 🤖 AI Visual Understanding
- **Screen Content Recognition**: Integrates advanced AI vision models like GPT-4V/Qwen2-VL to understand text, images, and interface elements on phone screens like humans do
- **Intelligent Element Identification**: Automatically recognizes UI elements such as buttons, input fields, and app icons, and labels their positions and functions
- **Contextual Description**: Intelligently describes current phone interface scenes based on context (e.g., "WeChat chat interface", "Taobao product details page")

### 🎯 Natural Language Control
- **Voice-Level Commands**: Supports natural language commands like "tap WeChat icon", "input verification code 123456", "return to home screen"
- **Multi-Step Task Execution**: Understands and executes complex multi-step commands (e.g., "open camera, take a photo, then share to Moments")
- **Intelligent Decision Making**: Makes reasonable inferences based on context when encountering ambiguities

### 📱 Full-Featured Device Management
- **App Manager**: Install/uninstall/launch/stop apps, manage app permissions and data
- **Screen Control**: Unlock/lock screen, get resolution, real-time screenshots
- **Input Simulation**: Simulate text input, key presses, taps, swipes, and complex gestures
- **Device Monitoring**: Get device information, running status, battery level, and other system parameters

## 🛠️ Technical Architecture

### AI Core Engine
- **Vision Model Integration**: Supports multiple AI vision models including OpenAI GPT-4V, Qwen2-VL
- **Context Understanding**: Model context management based on MCP protocol
- **Natural Language Processing**: Intelligently parses user commands and generates execution plans

### Technology Stack
- **Java 11**: High-performance backend implementation
- **Appium Java Client**: Stable Android device communication layer
- **OpenAI API**: AI vision model interface
- **SLF4J/Logback**: Enterprise-level logging system
- **Maven**: Standardized project build

## 📋 System Requirements

- **Java 11+**: Runtime environment
- **Android Device**: USB debugging enabled (Android 5.0+)
- **ADB Tool**: Android SDK Platform Tools
- **AI API Key**: OpenAI API key (for vision features, optional)

## 📦 Quick Start

### 1. Clone the Project

```bash
git clone <repository-url>
cd mcp-easy-doubao-phone
```

### 2. Build the AI Assistant

```bash
mvn clean package
```

After successful build, the AI assistant JAR package will be located in the `target` directory.

### 3. Configure AI API Key (Optional)

```bash
export OPENAI_API_KEY="your-openai-api-key"
export VISION_MODEL="qwen2.5-vl-7b-instruct"  # Or use gpt-4-vision-preview
```

### 4. Start the AI Assistant

```bash
java -jar target/mcp-easy-doubao-phone-1.0.0-jar-with-dependencies.jar -d <device-id>
```

## 🎮 Usage Examples

### 📱 Basic Operations

```bash
# Start the AI assistant
java -jar target/mcp-easy-doubao-phone-1.0.0-jar-with-dependencies.jar -d emulator-5554

# View current screen content
> describe-screen

# Identify and click WeChat icon
> find-and-click "WeChat"

# Input text
> input-text "Hello, this is Doubao Phone Assistant!"

# Swipe screen
> swipe 100 500 900 500
```

### 🤖 AI Enhanced Features

```bash
# Describe current interface
> describe-screen
# AI Response: "Currently displaying WeChat chat interface, top shows chat contact 'Zhang San', middle shows chat history, bottom shows input field"

# Intelligently find elements
> find-element "send button"
# AI Response: "Found send button, position: (900, 1700), size: (100x50)"

# Execute complex tasks
> execute "open settings, find Wi-Fi option, connect to network named 'HomeWiFi'"
# Assistant automatically executes: unlock screen → open settings → tap Wi-Fi → select HomeWiFi → wait for connection
```

## 🎨 Feature List

### 🔍 AI Vision Features
- `describe-screen`: Describe current screen content in natural language
- `annotate-screen`: Annotate all interactive elements on the screen
- `find-element <description>`: Find specific screen elements based on description
- `find-and-click <description>`: Find and click specified elements

### 📱 Device Management
- `get-device-info`: Get detailed device information
- `get-battery-status`: Check battery status
- `unlock-screen`: Unlock the screen
- `lock-screen`: Lock the screen

### 📲 App Management
- `install-app <apk-path>`: Install applications
- `uninstall-app <package-name>`: Uninstall applications
- `launch-app <package-name>`: Launch applications
- `stop-app <package-name>`: Stop applications
- `clear-app-data <package-name>`: Clear application data
- `get-running-apps`: Get list of running applications

### ⌨️ Input Control
- `input-text <text>`: Input text
- `press-key <keycode>`: Simulate key presses
- `tap <x> <y>`: Simulate taps
- `swipe <x1> <y1> <x2> <y2>`: Simulate swipes
- `long-press <x> <y> <duration>`: Simulate long presses
- `pinch <x> <y> <scale>`: Simulate pinch gestures

### 📸 Screen Operations
- `screenshot`: Take and save screenshots
- `get-screen-resolution`: Get screen resolution

## 🔧 Configuration Options

### Environment Variables

| Variable Name | Description | Default Value | Purpose |
|--------------|-------------|---------------|---------|
| `DEVICE_ID` | Device ID (obtained from `adb devices`) | None | Required, specifies the device to control |
| `ADB_PATH` | ADB tool path | `adb` | Optional, specifies ADB location |
| `OPENAI_API_KEY` | AI API key | None | Optional, enables vision features |
| `VISION_MODEL` | Vision model name | `qwen2.5-vl-7b-instruct` | Optional, selects AI model |
| `API_BASE_URL` | API base URL | `https://api.openai.com/v1` | Optional, customizes API address |

### Command Line Parameters

```bash
java -jar mcp-easy-doubao-phone-1.0.0-jar-with-dependencies.jar \
  -d <device-id>      # Device ID
  -a <adb-path>       # ADB path
  -k <api-key>        # AI API key
  -m <vision-model>   # Vision model
  -u <api-url>        # API base URL
```

## 🎯 Application Scenarios

### 📱 Automated Testing
- Intelligently identifies UI elements without hardcoding coordinates
- Describes test steps in natural language, reducing maintenance costs

### 🤖 Assistive Functions
- Provides voice descriptions of screen content for visually impaired users
- Simplifies complex phone operation processes

### 🔬 Research and Development
- Application of AI vision models in mobile interface understanding
- Bridge between natural language commands and device control

### 🎮 Game Assistance
- Intelligently recognizes game interface elements
- Automatically completes repetitive operations

## 🌟 Future Plans

- [ ] **Voice Interaction**: Support voice input and output
- [ ] **Multi-Device Management**: Control multiple Android devices simultaneously
- [ ] **More Powerful AI**: Support locally deployed vision models
- [ ] **Web Interface**: Provide visual control panel
- [ ] **Task Automation**: Support recording and playback of operation sequences

## 📄 License

MIT License - See LICENSE file for details

## 🤝 Contributing

Welcome to submit Issues and Pull Requests to build a more powerful Doubao phone assistant together!

---

💡 **Tip**: This project requires Android device. Ensure ADB is correctly installed and USB debugging is enabled. Vision features require a valid OpenAI API key.

📞 **Technical Support**: Encounter problems? Check project documentation or submit an Issue for help.

[Switch to Chinese Version](README.zh-CN.md)

