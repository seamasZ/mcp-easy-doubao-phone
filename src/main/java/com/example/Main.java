package com.example;

import com.example.device.AndroidDevice;
import com.example.tools.ToolsService;
import com.example.vision.VisionService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main类是应用程序的入口点
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("MCP Android ADB Server 启动");
        
        // 解析命令行参数
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        
        // 定义命令行选项
        options.addOption("d", "device-id", true, "Android设备ID");
        options.addOption("a", "adb-path", true, "ADB可执行文件路径");
        options.addOption("k", "api-key", true, "OpenAI API密钥");
        options.addOption("m", "model-name", true, "视觉模型名称");
        options.addOption("u", "api-base-url", true, "API基础URL");
        options.addOption("h", "help", false, "显示帮助信息");
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            // 显示帮助信息
            if (cmd.hasOption("help")) {
                printHelp(options);
                return;
            }
            
            // 获取环境变量和命令行参数
            String deviceId = getEnvOrCmdOption(cmd, "DEVICE_ID", "device-id");
            String adbPath = getEnvOrCmdOption(cmd, "ADB_PATH", "adb-path", "adb");
            String apiKey = getEnvOrCmdOption(cmd, "OPENAI_API_KEY", "api-key");
            String modelName = getEnvOrCmdOption(cmd, "VISION_MODEL", "model-name", "qwen2.5-vl-7b-instruct");
            String apiBaseUrl = getEnvOrCmdOption(cmd, "API_BASE_URL", "api-base-url", "https://api.openai.com/v1");
            
            // 检查必要参数
            if (deviceId == null || deviceId.isEmpty()) {
                logger.error("必须提供设备ID，请使用 -d 参数或设置DEVICE_ID环境变量");
                printHelp(options);
                System.exit(1);
            }
            
            // 初始化设备
            logger.info("初始化设备连接，设备ID: {}", deviceId);
            AndroidDevice device = new AndroidDevice(deviceId, adbPath);
            if (!device.connect()) {
                logger.error("设备连接失败");
                System.exit(1);
            }
            
            // 初始化视觉服务（如果提供了API密钥）
            VisionService visionService = null;
            if (apiKey != null && !apiKey.isEmpty()) {
                logger.info("初始化视觉服务，模型: {}", modelName);
                visionService = new VisionService(apiKey, modelName, apiBaseUrl);
            }
            
            // 初始化工具服务
            logger.info("初始化工具服务");
            ToolsService toolsService = new ToolsService(device, visionService);
            
            // 启动命令行界面
            startCommandLineInterface(toolsService);
            
        } catch (ParseException e) {
            logger.error("命令行参数解析错误", e);
            printHelp(options);
            System.exit(1);
        } catch (Exception e) {
            logger.error("应用程序启动失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 获取环境变量或命令行参数
     * @param cmd 命令行解析结果
     * @param envName 环境变量名称
     * @param optionName 命令行选项名称
     * @return 值
     */
    private static String getEnvOrCmdOption(CommandLine cmd, String envName, String optionName) {
        return getEnvOrCmdOption(cmd, envName, optionName, null);
    }
    
    /**
     * 获取环境变量或命令行参数
     * @param cmd 命令行解析结果
     * @param envName 环境变量名称
     * @param optionName 命令行选项名称
     * @param defaultValue 默认值
     * @return 值
     */
    private static String getEnvOrCmdOption(CommandLine cmd, String envName, String optionName, String defaultValue) {
        // 优先使用命令行参数
        if (cmd.hasOption(optionName)) {
            return cmd.getOptionValue(optionName);
        }
        
        // 然后使用环境变量
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        
        // 最后使用默认值
        return defaultValue;
    }
    
    /**
     * 打印帮助信息
     * @param options 命令行选项
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar mcp-android-adb-server.jar [options]", options);
    }
    
    /**
     * 启动命令行界面
     * @param toolsService 工具服务实例
     */
    private static void startCommandLineInterface(ToolsService toolsService) {
        logger.info("命令行界面启动，输入 'help' 查看可用命令");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("adb-tool > ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            // 处理退出命令
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                logger.info("应用程序退出");
                break;
            }
            
            // 处理帮助命令
            if (input.equalsIgnoreCase("help")) {
                printAvailableTools(toolsService);
                continue;
            }
            
            // 解析命令
            String[] parts = input.split("\\s+");
            if (parts.length < 1) {
                System.out.println("无效命令，请输入 'help' 查看可用命令");
                continue;
            }
            
            String toolName = parts[0];
            
            // 解析参数
            Map<String, Object> params = new HashMap<>();
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (part.startsWith("--")) {
                    String[] keyValue = part.substring(2).split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    } else if (keyValue.length == 1) {
                        params.put(keyValue[0], true);
                    }
                }
            }
            
            // 执行工具
            try {
                logger.info("执行工具: {}, 参数: {}", toolName, params);
                
                com.example.tools.ToolsService.ToolResult result = toolsService.callTool(toolName, params);
                
                if (result.getStatus().equals("success")) {
                    System.out.println("成功: " + result.getMessage());
                    if (result.getData() != null && !result.getData().isEmpty()) {
                        System.out.println("结果:");
                        for (Map.Entry<String, Object> entry : result.getData().entrySet()) {
                            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
                        }
                    }
                } else {
                    System.out.println("失败: " + result.getMessage());
                }
            } catch (Exception e) {
                logger.error("执行工具失败", e);
                System.out.println("错误: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * 打印可用工具列表
     * @param toolsService 工具服务实例
     */
    private static void printAvailableTools(ToolsService toolsService) {
        System.out.println("可用工具:");
        toolsService.getTools().forEach((name, tool) -> {
            System.out.printf("  %-20s %s%n", name, tool.getDescription());
        });
        System.out.println();
        System.out.println("使用方法: <tool_name> --param1=value1 --param2=value2");
        System.out.println("示例: app_install --apk_path=myapp.apk");
        System.out.println();
        System.out.println("输入 'exit' 或 'quit' 退出程序");
    }
}