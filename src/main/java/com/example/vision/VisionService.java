package com.example.vision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    
    /**
     * 构造函数
     * @param apiKey OpenAI API密钥
     * @param modelName 模型名称
     * @param apiBaseUrl API基础URL
     */
    public VisionService(String apiKey, String modelName, String apiBaseUrl) {
        this.modelName = modelName;
        this.apiBaseUrl = apiBaseUrl;
        this.objectMapper = new ObjectMapper();
        
        // 初始化OpenAI服务
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey);
            logger.info("视觉服务已初始化，模型: {}", modelName);
        } else {
            logger.warn("未提供API密钥，视觉服务功能将不可用");
        }
    }
    
    /**
     * 将图像文件转换为Base64编码
     * @param imagePath 图像文件路径
     * @return Base64编码的图像数据
     * @throws IOException IO异常
     */
    public String imageToBase64(String imagePath) throws IOException {
        logger.info("将图像转换为Base64: {}", imagePath);
        
        byte[] imageBytes = Files.readAllBytes(new File(imagePath).toPath());
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        
        logger.debug("图像Base64编码完成，长度: {}", base64.length());
        return base64;
    }
    
    /**
     * 生成截图描述
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
        
        // 发送请求
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(messages)
                .maxTokens(1000)
                .temperature(0.7)
                .build();
        
        String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
        
        logger.info("截图描述生成完成");
        return response;
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