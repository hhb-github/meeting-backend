package com.meeting.manager.dto;

import java.util.List;

/**
 * 通义千问API请求DTO
 */
public class ChatRequest {
    
    private String model;
    private List<ChatMessage> messages;
    private double temperature = 0.2;
    private int max_tokens = 4000;
    
    // Getters and setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public int getMax_tokens() {
        return max_tokens;
    }
    
    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }
    
    /**
     * 聊天消息模型
     */
    public static class ChatMessage {
        private String role;
        private String content;
        
        public ChatMessage() {}
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}