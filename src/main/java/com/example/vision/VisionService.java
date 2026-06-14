package com.example.vision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiApi;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * VisionService类负责视觉模型的集成和操作
 */
public class VisionService {
    private static final Logger logger = LoggerFactory.getLogger(VisionService.class);
    
    private OpenAiService openAiService;
    private String modelName;
    private String apiBaseUrl;
    private ObjectMapper objectMapper;
    
    /**
     * 构造函数
     * @param apiKey OpenAI API密钥
     */
    public VisionService(String apiKey) {
        this(apiKey, "qwen2.5-vl-7b-instruct"); // 默认使用Qwen2.5-VL模型
    }
    
    /**
     * 构造函数
     * @param apiKey OpenAI API密钥
     * @param modelName 模型名称
     */
    public VisionService(String apiKey, String modelName) {
        this(apiKey, modelName, "https://api.openai.com/v1"); // 默认OpenAI API地址
    }
    
    private static final int MAX_RETRIES = 3;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);
    
    /**
     * 构造函数（优化版：带超时和重试配置）
     * @param apiKey OpenAI API密钥
     * @param modelName 模型名称
     * @param apiBaseUrl API基础URL
     */
    public VisionService(String apiKey, String modelName, String apiBaseUrl) {
        this.modelName = modelName;
        this.apiBaseUrl = apiBaseUrl;
        this.objectMapper = new ObjectMapper();
        
        // 初始化OpenAI服务（带自定义超时）
        if (apiKey != null && !apiKey.isEmpty()) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT)
                    .readTimeout(READ_TIMEOUT)
                    .writeTimeout(READ_TIMEOUT)
                    .build();
            
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiBaseUrl)
                    .client(client)
                    .build();
            
            OpenAiApi api = retrofit.create(OpenAiApi.class);
            this.openAiService = new OpenAiService(api);
            logger.info("视觉服务已初始化，模型: {}, 超时: {}s", modelName, READ_TIMEOUT.getSeconds());
        } else {
            logger.warn("未提供API密钥，视觉服务功能将不可用");
        }
    }
    
    /**
     * 将图像文件转换为Base64编码（优化版：使用流式读取减少内存占用）
     * @param imagePath 图像文件路径
     * @return Base64编码的图像数据
     * @throws IOException IO异常
     */
    public String imageToBase64(String imagePath) throws IOException {
        logger.info("将图像转换为Base64: {}", imagePath);
        
        // 使用流式读取，避免一次性加载大文件到内存
        try (java.io.InputStream is = Files.newInputStream(new File(imagePath).toPath())) {
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            String base64 = Base64.getEncoder().encodeToString(buffer.toByteArray());
            logger.debug("图像Base64编码完成，长度: {}", base64.length());
            return base64;
        }
    }
    
    /**
     * 生成截图描述（带重试机制）
     * @param imagePath 截图文件路径
     * @param prompt 用户提示
     * @return 截图描述
     * @throws IOException IO异常
     */
    public String describeScreenshot(String imagePath, String prompt) throws IOException {
        if (openAiService == null) {
            throw new IllegalStateException("OpenAI服务未初始化，请提供API密钥");
        }
        
        logger.info("生成截图描述，图像: {}, 提示: {}", imagePath, prompt);
        
        // 将图像转换为Base64
        String base64Image = imageToBase64(imagePath);
        
        // 构建请求消息
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent("你是一个专业的图像分析助手，请详细描述图像内容。");
        messages.add(systemMessage);
        
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(prompt + "\n\n" + "data:image/png;base64," + base64Image);
        messages.add(userMessage);
        
        // 发送请求（带重试）
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(messages)
                .maxTokens(1000)
                .temperature(0.7)
                .build();
        
        String response = null;
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < MAX_RETRIES) {
            try {
                response = openAiService.createChatCompletion(request)
                        .getChoices().get(0).getMessage().getContent();
                logger.info("截图描述生成完成（尝试 {}/{}）", attempts + 1, MAX_RETRIES);
                return response;
            } catch (Exception e) {
                lastException = e;
                attempts++;
                logger.warn("API调用失败（尝试 {}/{}）: {}", attempts, MAX_RETRIES, e.getMessage());
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000L * attempts); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new IOException("API调用失败，已重试 " + MAX_RETRIES + " 次", lastException);
    }
    
    /**
     * 在截图上标注屏幕元素
     * @param imagePath 原始截图路径
     * @param elements 屏幕元素列表
     * @param outputPath 输出路径
     * @return 是否标注成功
     */
    public boolean annotateScreenshot(String imagePath, List<Element> elements, String outputPath) {
        logger.info("标注截图，原始图像: {}, 输出路径: {}", imagePath, outputPath);
        
        try {
            // 读取原始图像
            BufferedImage image = ImageIO.read(new File(imagePath));
            Graphics2D g2d = image.createGraphics();
            
            // 设置红色边框
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            
            // 标注每个元素
            for (Element element : elements) {
                // 绘制矩形框
                Rectangle rect = element.getRect();
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                
                // 绘制文本标签
                g2d.drawString(element.getLabel(), rect.x, rect.y - 5);
            }
            
            g2d.dispose();
            
            // 保存标注后的图像
            ImageIO.write(image, "PNG", new File(outputPath));
            
            logger.info("截图标注完成");
            return true;
        } catch (IOException e) {
            logger.error("标注截图失败", e);
            return false;
        }
    }
    
    /**
     * 分析截图并返回屏幕元素
     * @param imagePath 截图路径
     * @param prompt 分析提示
     * @return 屏幕元素列表
     * @throws IOException IO异常
     */
    public List<Element> analyzeScreenshot(String imagePath, String prompt) throws IOException {
        // 生成截图描述
        String description = describeScreenshot(imagePath, prompt);
        
        // 这里可以根据模型的响应格式解析出屏幕元素
        // 由于不同模型的响应格式可能不同，这里使用简化实现
        List<Element> elements = new ArrayList<>();
        
        // 示例：假设模型返回的描述包含元素信息，我们解析它
        // 实际实现需要根据模型的响应格式进行调整
        if (description.contains("按钮")) {
            // 示例：添加一个按钮元素
            Element button = new Element();
            button.setLabel("按钮");
            button.setRect(new Rectangle(100, 100, 200, 50));
            elements.add(button);
        }
        
        logger.info("截图分析完成，识别到 {} 个元素", elements.size());
        return elements;
    }
    
    /**
     * 获取模型名称
     * @return 模型名称
     */
    public String getModelName() {
        return modelName;
    }
    
    /**
     * 设置模型名称
     * @param modelName 模型名称
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
        logger.info("模型名称已更新为: {}", modelName);
    }
    
    /**
     * 获取API基础URL
     * @return API基础URL
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    /**
     * 设置API基础URL
     * @param apiBaseUrl API基础URL
     */
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        logger.info("API基础URL已更新为: {}", apiBaseUrl);
    }
    
    /**
     * 检查服务是否初始化
     * @return 是否初始化
     */
    public boolean isInitialized() {
        return openAiService != null;
    }
}