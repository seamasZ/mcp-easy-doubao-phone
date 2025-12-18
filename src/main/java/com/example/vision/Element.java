package com.example.vision;

import java.awt.Rectangle;

/**
 * Element类表示屏幕上的一个元素
 */
public class Element {
    private String label; // 元素标签
    private Rectangle rect; // 元素在屏幕上的位置和大小
    private String type; // 元素类型（如按钮、文本框等）
    private String text; // 元素上的文本内容
    private double confidence; // 识别置信度
    
    /**
     * 构造函数
     */
    public Element() {
        this.rect = new Rectangle();
    }
    
    /**
     * 构造函数
     * @param label 元素标签
     * @param rect 元素位置和大小
     */
    public Element(String label, Rectangle rect) {
        this.label = label;
        this.rect = rect;
    }
    
    /**
     * 获取元素标签
     * @return 元素标签
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * 设置元素标签
     * @param label 元素标签
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * 获取元素位置和大小
     * @return 元素位置和大小
     */
    public Rectangle getRect() {
        return rect;
    }
    
    /**
     * 设置元素位置和大小
     * @param rect 元素位置和大小
     */
    public void setRect(Rectangle rect) {
        this.rect = rect;
    }
    
    /**
     * 获取元素类型
     * @return 元素类型
     */
    public String getType() {
        return type;
    }
    
    /**
     * 设置元素类型
     * @param type 元素类型
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * 获取元素文本
     * @return 元素文本
     */
    public String getText() {
        return text;
    }
    
    /**
     * 设置元素文本
     * @param text 元素文本
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * 获取识别置信度
     * @return 识别置信度
     */
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * 设置识别置信度
     * @param confidence 识别置信度
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    @Override
    public String toString() {
        return "Element{" +
                "label='" + label + '\'' +
                ", rect=" + rect +
                ", type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}