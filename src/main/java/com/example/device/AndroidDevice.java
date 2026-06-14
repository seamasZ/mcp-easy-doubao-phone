package com.example.device;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobilePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AndroidDevice类负责管理Android设备的连接和操作
 */
public class AndroidDevice {
    private static final Logger logger = LoggerFactory.getLogger(AndroidDevice.class);
    
    private AndroidDriver driver;
    private String deviceId;
    private String adbPath;
    private boolean isConnected;
    
    /**
     * 构造函数
     * @param deviceId 设备ID
     */
    public AndroidDevice(String deviceId) {
        this(deviceId, "adb"); // 默认使用系统路径中的adb
    }
    
    /**
     * 构造函数
     * @param deviceId 设备ID
     * @param adbPath ADB可执行文件路径
     */
    public AndroidDevice(String deviceId, String adbPath) {
        this.deviceId = deviceId;
        this.adbPath = adbPath;
        this.isConnected = false;
        logger.info("创建AndroidDevice实例，设备ID: {}", deviceId);
    }
    
    /**
     * 连接到设备
     * @return 是否连接成功
     */
    public boolean connect() {
        try {
            logger.info("正在连接设备: {}", deviceId);
            
            // 配置Appium驱动选项
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName(MobilePlatform.ANDROID)
                    .setAutomationName(AutomationName.ANDROID_UIAUTOMATOR2)
                    .setDeviceName(deviceId)
                    .setAppPackage("com.android.settings") // 默认包名
                    .setAppActivity(".Settings") // 默认活动
                    .setNewCommandTimeout(Duration.ofSeconds(60));
            
            // 初始化驱动
            driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), options);
            isConnected = true;
            logger.info("设备连接成功: {}", deviceId);
            return true;
        } catch (Exception e) {
            logger.error("连接设备失败: {}", deviceId, e);
            return false;
        }
    }
    
    /**
     * 断开设备连接
     */
    public void disconnect() {
        if (driver != null) {
            try {
                driver.quit();
                isConnected = false;
                logger.info("设备已断开连接: {}", deviceId);
            } catch (Exception e) {
                logger.error("断开设备连接失败: {}", deviceId, e);
            }
        }
    }
    
    /**
     * 安装应用
     * @param apkPath APK文件路径
     * @return 是否安装成功
     */
    public boolean installApp(String apkPath) {
        try {
            logger.info("正在安装应用: {}", apkPath);
            runAdbCommand("install -r " + apkPath);
            logger.info("应用安装成功: {}", apkPath);
            return true;
        } catch (Exception e) {
            logger.error("安装应用失败: {}", apkPath, e);
            return false;
        }
    }
    
    /**
     * 卸载应用
     * @param packageName 包名
     * @return 是否卸载成功
     */
    public boolean uninstallApp(String packageName) {
        try {
            logger.info("正在卸载应用: {}", packageName);
            runAdbCommand("uninstall " + packageName);
            logger.info("应用卸载成功: {}", packageName);
            return true;
        } catch (Exception e) {
            logger.error("卸载应用失败: {}", packageName, e);
            return false;
        }
    }
    
    /**
     * 启动应用
     * @param packageName 包名
     * @param activityName 活动名
     * @return 是否启动成功
     */
    public boolean startApp(String packageName, String activityName) {
        try {
            logger.info("正在启动应用: {}/{} {}", packageName, activityName, deviceId);
            runAdbCommand("shell am start -n " + packageName + "/" + activityName);
            logger.info("应用启动成功: {}/{}", packageName, activityName);
            return true;
        } catch (Exception e) {
            logger.error("启动应用失败: {}/{}", packageName, activityName, e);
            return false;
        }
    }
    
    /**
     * 终止应用
     * @param packageName 包名
     * @return 是否终止成功
     */
    public boolean stopApp(String packageName) {
        try {
            logger.info("正在终止应用: {}", packageName);
            runAdbCommand("shell am force-stop " + packageName);
            logger.info("应用终止成功: {}", packageName);
            return true;
        } catch (Exception e) {
            logger.error("终止应用失败: {}", packageName, e);
            return false;
        }
    }
    
    /**
     * 解锁屏幕
     * @return 是否解锁成功
     */
    public boolean unlockScreen() {
        try {
            logger.info("正在解锁屏幕");
            runAdbCommand("shell input keyevent KEYCODE_WAKEUP");
            // 滑动解锁（假设从下往上滑动）
            runAdbCommand("shell input swipe 300 1000 300 500");
            logger.info("屏幕解锁成功");
            return true;
        } catch (Exception e) {
            logger.error("解锁屏幕失败", e);
            return false;
        }
    }
    
    /**
     * 锁定屏幕
     * @return 是否锁定成功
     */
    public boolean lockScreen() {
        try {
            logger.info("正在锁定屏幕");
            runAdbCommand("shell input keyevent KEYCODE_POWER");
            logger.info("屏幕锁定成功");
            return true;
        } catch (Exception e) {
            logger.error("锁定屏幕失败", e);
            return false;
        }
    }
    
    /**
     * 输入文本
     * @param text 要输入的文本
     * @return 是否输入成功
     */
    public boolean inputText(String text) {
        try {
            logger.info("正在输入文本: {}", text);
            runAdbCommand("shell input text " + text);
            logger.info("文本输入成功");
            return true;
        } catch (Exception e) {
            logger.error("输入文本失败", e);
            return false;
        }
    }
    
    /**
     * 输入按键
     * @param keyCode 按键码
     * @return 是否输入成功
     */
    public boolean inputKeyEvent(String keyCode) {
        try {
            logger.info("正在输入按键: {}", keyCode);
            runAdbCommand("shell input keyevent " + keyCode);
            logger.info("按键输入成功");
            return true;
        } catch (Exception e) {
            logger.error("输入按键失败", e);
            return false;
        }
    }
    
    /**
     * 点击屏幕（带参数校验）
     * @param x X坐标
     * @param y Y坐标
     * @return 是否点击成功
     */
    public boolean tap(int x, int y) {
        if (x < 0 || y < 0) {
            logger.error("点击坐标不能为负数: ({}, {})", x, y);
            return false;
        }
        try {
            logger.info("正在点击屏幕: ({}, {})", x, y);
            runAdbCommand("shell input tap " + x + " " + y);
            logger.info("屏幕点击成功");
            return true;
        } catch (Exception e) {
            logger.error("点击屏幕失败", e);
            return false;
        }
    }
    
    /**
     * 滑动屏幕（带参数校验）
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param duration 滑动持续时间（毫秒）
     * @return 是否滑动成功
     */
    public boolean swipe(int startX, int startY, int endX, int endY, int duration) {
        if (startX < 0 || startY < 0 || endX < 0 || endY < 0) {
            logger.error("滑动坐标不能为负数");
            return false;
        }
        if (duration < 0) {
            logger.error("滑动持续时间不能为负数: {}", duration);
            return false;
        }
        try {
            logger.info("正在滑动屏幕: ({}, {}) -> ({}, {})", startX, startY, endX, endY);
            runAdbCommand("shell input swipe " + startX + " " + startY + " " + endX + " " + endY + " " + duration);
            logger.info("屏幕滑动成功");
            return true;
        } catch (Exception e) {
            logger.error("滑动屏幕失败", e);
            return false;
        }
    }
    
    /**
     * 截图（优化版：使用 exec-out 直接输出到本地，避免临时文件）
     * @param outputPath 输出路径
     * @return 是否截图成功
     */
    public boolean screenshot(String outputPath) {
        try {
            logger.info("正在截图，保存到: {}", outputPath);
            // 使用 exec-out 直接输出截图数据到本地，避免在设备上创建临时文件
            String fullCommand = adbPath + " -s " + deviceId + " exec-out screencap -p";
            ProcessBuilder pb = new ProcessBuilder(fullCommand.split(" "));
            pb.redirectOutput(new java.io.File(outputPath));
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("截图成功");
                return true;
            } else {
                throw new IOException("截图命令退出码: " + exitCode);
            }
        } catch (Exception e) {
            logger.error("截图失败", e);
            return false;
        }
    }
    
    /**
     * 获取设备信息
     * @return 设备信息映射
     */
    public Map<String, String> getDeviceInfo() {
        Map<String, String> info = new HashMap<>();
        try {
            info.put("deviceId", deviceId);
            info.put("model", runAdbCommand("shell getprop ro.product.model").trim());
            info.put("manufacturer", runAdbCommand("shell getprop ro.product.manufacturer").trim());
            info.put("androidVersion", runAdbCommand("shell getprop ro.build.version.release").trim());
            info.put("apiLevel", runAdbCommand("shell getprop ro.build.version.sdk").trim());
        } catch (Exception e) {
            logger.error("获取设备信息失败", e);
        }
        return info;
    }
    
    /**
     * 获取电池信息
     * @return 电池信息映射
     */
    public Map<String, Object> getBatteryInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            String output = runAdbCommand("shell dumpsys battery").trim();
            // 解析电池信息
            for (String line : output.split("\n")) {
                line = line.trim();
                if (line.startsWith("level:")) {
                    info.put("level", Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("scale:")) {
                    info.put("scale", Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("status:")) {
                    info.put("status", line.split(":")[1].trim());
                } else if (line.startsWith("health:")) {
                    info.put("health", line.split(":")[1].trim());
                } else if (line.startsWith("present:")) {
                    info.put("present", Boolean.parseBoolean(line.split(":")[1].trim()));
                } else if (line.startsWith("AC powered:")) {
                    info.put("acPowered", Boolean.parseBoolean(line.split(":")[1].trim()));
                } else if (line.startsWith("USB powered:")) {
                    info.put("usbPowered", Boolean.parseBoolean(line.split(":")[1].trim()));
                } else if (line.startsWith("temperature:")) {
                    info.put("temperature", Integer.parseInt(line.split(":")[1].trim()) / 10.0);
                }
            }
        } catch (Exception e) {
            logger.error("获取电池信息失败", e);
        }
        return info;
    }
    
    /**
     * 运行ADB命令（优化版：跨平台支持，直接执行 adb 而非依赖 cmd.exe）
     * @param command 命令参数
     * @return 命令输出
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    private String runAdbCommand(String command) throws IOException, InterruptedException {
        String fullCommand = adbPath + " -s " + deviceId + " " + command;
        logger.debug("运行ADB命令: {}", fullCommand);
        
        // 跨平台：直接执行 adb 命令，不依赖 Windows cmd.exe
        List<String> cmdList = new ArrayList<>();
        cmdList.add(adbPath);
        cmdList.add("-s");
        cmdList.add(deviceId);
        // 将 command 按空格拆分（简单处理，不含引号嵌套场景）
        for (String part : command.split("\\s+")) {
            if (!part.isEmpty()) {
                cmdList.add(part);
            }
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("ADB命令执行失败，退出码: " + exitCode + ", 命令: " + fullCommand);
        }
        
        logger.debug("ADB命令输出: {}", output.toString().trim());
        return output.toString();
    }
    
    /**
     * 检查设备是否连接
     * @return 是否连接
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * 获取ADB路径
     * @return ADB路径
     */
    public String getAdbPath() {
        return adbPath;
    }
    
    /**
     * 设置ADB路径
     * @param adbPath ADB路径
     */
    public void setAdbPath(String adbPath) {
        this.adbPath = adbPath;
    }
    
    /**
     * 获取Appium驱动
     * @return Appium驱动
     */
    public AndroidDriver getDriver() {
        return driver;
    }
}