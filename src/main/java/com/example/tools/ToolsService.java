package com.example.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.example.device.AndroidDevice;
import com.example.vision.VisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolsService类负责管理和调用各种ADB工具
 */
public class ToolsService {
    private static final Logger logger = LoggerFactory.getLogger(ToolsService.class);
    
    private Map<String, Tool> tools;
    private AndroidDevice device;
    private VisionService visionService;
    
    /**
     * 构造函数
     * @param device Android设备实例
     */
    public ToolsService(AndroidDevice device) {
        this.device = device;
        this.tools = new HashMap<>();
        logger.info("ToolsService初始化，设备: {}", device.getDeviceId());
        
        // 注册默认工具
        registerDefaultTools();
    }
    
    /**
     * 构造函数
     * @param device Android设备实例
     * @param visionService 视觉服务实例
     */
    public ToolsService(AndroidDevice device, VisionService visionService) {
        this(device);
        this.visionService = visionService;
        logger.info("ToolsService初始化，包含视觉服务");
    }
    
    /**
     * 注册默认工具
     */
    private void registerDefaultTools() {
        logger.info("注册默认工具");
        
        // 应用管理工具
        registerTool("app_install", new AppInstallTool(device));
        registerTool("app_uninstall", new AppUninstallTool(device));
        registerTool("app_start", new AppStartTool(device));
        registerTool("app_stop", new AppStopTool(device));
        
        // 屏幕控制工具
        registerTool("screen_unlock", new ScreenUnlockTool(device));
        registerTool("screen_lock", new ScreenLockTool(device));
        
        // 输入控制工具
        registerTool("input_text", new InputTextTool(device));
        registerTool("input_key", new InputKeyTool(device));
        registerTool("tap", new TapTool(device));
        registerTool("swipe", new SwipeTool(device));
        
        // 系统信息工具
        registerTool("get_device_info", new GetDeviceInfoTool(device));
        
        // 截图工具
        registerTool("screenshot", new ScreenshotTool(device));
        
        // 视觉相关工具
        if (visionService != null) {
            registerTool("describe_screenshot", new DescribeScreenshotTool(device, visionService));
        }
        
        logger.info("默认工具注册完成，共注册 {} 个工具", tools.size());
    }
    
    /**
     * 注册工具
     * @param name 工具名称
     * @param tool 工具实例
     */
    public void registerTool(String name, Tool tool) {
        tools.put(name, tool);
        logger.info("工具注册成功: {}", name);
    }
    
    /**
     * 调用工具
     * @param name 工具名称
     * @param params 工具参数
     * @return 工具执行结果
     * @throws Exception 执行异常
     */
    public ToolResult callTool(String name, Map<String, Object> params) throws Exception {
        logger.info("调用工具: {}, 参数: {}", name, params);
        
        Tool tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("工具不存在: " + name);
        }
        
        ToolResult result = tool.execute(params);
        logger.info("工具调用完成: {}, 结果: {}", name, result.getStatus());
        return result;
    }
    
    /**
     * 获取所有注册的工具名称
     * @return 工具名称列表
     */
    public Map<String, Tool> getTools() {
        return tools;
    }
    
    /**
     * 设置视觉服务实例
     * @param visionService 视觉服务实例
     */
    public void setVisionService(VisionService visionService) {
        this.visionService = visionService;
        
        // 注册视觉相关工具
        if (!tools.containsKey("describe_screenshot")) {
            registerTool("describe_screenshot", new DescribeScreenshotTool(device, visionService));
        }
    }
    
    /**
     * 获取视觉服务实例
     * @return 视觉服务实例
     */
    public VisionService getVisionService() {
        return visionService;
    }
    
    /**
     * 获取系统信息
     * @return 系统信息映射
     */
    public Map<String, String> getSystemInfo() {
        Map<String, String> systemInfo = new HashMap<>();
        systemInfo.put("os.name", System.getProperty("os.name"));
        systemInfo.put("os.version", System.getProperty("os.version"));
        systemInfo.put("java.version", System.getProperty("java.version"));
        return systemInfo;
    }
    
    /**
     * 获取当前时间
     * @return 当前时间字符串
     */
    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}

/**
 * Tool接口定义工具的基本结构
 */
interface Tool {
    /**
     * 执行工具
     * @param params 工具参数
     * @return 执行结果
     * @throws Exception 执行异常
     */
    ToolResult execute(Map<String, Object> params) throws Exception;
    
    /**
     * 获取工具描述
     * @return 工具描述
     */
    String getDescription();
}

/**
 * ToolResult类表示工具执行结果
 */
class ToolResult {
    private String status;
    private String message;
    private Map<String, Object> data;
    
    public ToolResult() {
        this.status = "success";
        this.data = new HashMap<>();
    }
    
    public ToolResult(String status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }
}

/**
 * 应用安装工具
 */
class AppInstallTool implements Tool {
    private AndroidDevice device;
    
    public AppInstallTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String apkPath = (String) params.get("apk_path");
        if (apkPath == null) {
            throw new IllegalArgumentException("需要提供apk_path参数");
        }
        
        boolean result = device.installApp(apkPath);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("应用安装成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("应用安装失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "安装Android应用";
    }
}

/**
 * 应用卸载工具
 */
class AppUninstallTool implements Tool {
    private AndroidDevice device;
    
    public AppUninstallTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String packageName = (String) params.get("package_name");
        if (packageName == null) {
            throw new IllegalArgumentException("需要提供package_name参数");
        }
        
        boolean result = device.uninstallApp(packageName);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("应用卸载成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("应用卸载失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "卸载Android应用";
    }
}

/**
 * 应用启动工具
 */
class AppStartTool implements Tool {
    private AndroidDevice device;
    
    public AppStartTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String packageName = (String) params.get("package_name");
        String activityName = (String) params.get("activity_name");
        
        if (packageName == null || activityName == null) {
            throw new IllegalArgumentException("需要提供package_name和activity_name参数");
        }
        
        boolean result = device.startApp(packageName, activityName);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("应用启动成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("应用启动失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "启动Android应用";
    }
}

/**
 * 应用停止工具
 */
class AppStopTool implements Tool {
    private AndroidDevice device;
    
    public AppStopTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String packageName = (String) params.get("package_name");
        if (packageName == null) {
            throw new IllegalArgumentException("需要提供package_name参数");
        }
        
        boolean result = device.stopApp(packageName);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("应用停止成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("应用停止失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "停止Android应用";
    }
}

/**
 * 屏幕解锁工具
 */
class ScreenUnlockTool implements Tool {
    private AndroidDevice device;
    
    public ScreenUnlockTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        boolean result = device.unlockScreen();
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("屏幕解锁成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("屏幕解锁失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "解锁屏幕";
    }
}

/**
 * 屏幕锁定工具
 */
class ScreenLockTool implements Tool {
    private AndroidDevice device;
    
    public ScreenLockTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        boolean result = device.lockScreen();
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("屏幕锁定成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("屏幕锁定失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "锁定屏幕";
    }
}

/**
 * 输入文本工具
 */
class InputTextTool implements Tool {
    private AndroidDevice device;
    
    public InputTextTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String text = (String) params.get("text");
        if (text == null) {
            throw new IllegalArgumentException("需要提供text参数");
        }
        
        boolean result = device.inputText(text);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("文本输入成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("文本输入失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "输入文本";
    }
}

/**
 * 输入按键工具
 */
class InputKeyTool implements Tool {
    private AndroidDevice device;
    
    public InputKeyTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String keyCode = (String) params.get("key_code");
        if (keyCode == null) {
            throw new IllegalArgumentException("需要提供key_code参数");
        }
        
        boolean result = device.inputKeyEvent(keyCode);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("按键输入成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("按键输入失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "输入按键";
    }
}

/**
 * 点击屏幕工具
 */
class TapTool implements Tool {
    private AndroidDevice device;
    
    public TapTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        Integer x = (Integer) params.get("x");
        Integer y = (Integer) params.get("y");
        
        if (x == null || y == null) {
            throw new IllegalArgumentException("需要提供x和y参数");
        }
        
        boolean result = device.tap(x, y);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("屏幕点击成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("屏幕点击失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "点击屏幕";
    }
}

/**
 * 滑动屏幕工具
 */
class SwipeTool implements Tool {
    private AndroidDevice device;
    
    public SwipeTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        Integer startX = (Integer) params.get("start_x");
        Integer startY = (Integer) params.get("start_y");
        Integer endX = (Integer) params.get("end_x");
        Integer endY = (Integer) params.get("end_y");
        Integer duration = (Integer) params.getOrDefault("duration", 500);
        
        if (startX == null || startY == null || endX == null || endY == null) {
            throw new IllegalArgumentException("需要提供start_x, start_y, end_x和end_y参数");
        }
        
        boolean result = device.swipe(startX, startY, endX, endY, duration);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("屏幕滑动成功");
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("屏幕滑动失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "滑动屏幕";
    }
}

/**
 * 获取设备信息工具
 */
class GetDeviceInfoTool implements Tool {
    private AndroidDevice device;
    
    public GetDeviceInfoTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        ToolResult toolResult = new ToolResult();
        toolResult.setMessage("获取设备信息成功");
        toolResult.setData(device.getDeviceInfo());
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "获取设备信息";
    }
}

/**
 * 截图工具
 */
class ScreenshotTool implements Tool {
    private AndroidDevice device;
    
    public ScreenshotTool(AndroidDevice device) {
        this.device = device;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String outputPath = (String) params.get("output_path");
        if (outputPath == null) {
            throw new IllegalArgumentException("需要提供output_path参数");
        }
        
        boolean result = device.screenshot(outputPath);
        ToolResult toolResult = new ToolResult();
        if (result) {
            toolResult.setMessage("截图成功");
            toolResult.addData("screenshot_path", outputPath);
        } else {
            toolResult.setStatus("error");
            toolResult.setMessage("截图失败");
        }
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "截图";
    }
}

/**
 * 截图描述工具
 */
class DescribeScreenshotTool implements Tool {
    private AndroidDevice device;
    private VisionService visionService;
    
    public DescribeScreenshotTool(AndroidDevice device, VisionService visionService) {
        this.device = device;
        this.visionService = visionService;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> params) throws Exception {
        String prompt = (String) params.getOrDefault("prompt", "请详细描述截图内容");
        String outputPath = (String) params.getOrDefault("output_path", "screenshot.png");
        
        // 先截图
        boolean screenshotResult = device.screenshot(outputPath);
        if (!screenshotResult) {
            ToolResult toolResult = new ToolResult();
            toolResult.setStatus("error");
            toolResult.setMessage("截图失败");
            return toolResult;
        }
        
        // 然后生成描述
        String description = visionService.describeScreenshot(outputPath, prompt);
        
        ToolResult toolResult = new ToolResult();
        toolResult.setMessage("截图描述生成成功");
        toolResult.addData("screenshot_path", outputPath);
        toolResult.addData("description", description);
        return toolResult;
    }
    
    @Override
    public String getDescription() {
        return "生成截图描述";
    }
}