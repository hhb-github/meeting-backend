package com.meeting.manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meeting.manager.dto.ChatRequest;
import com.meeting.manager.dto.StructuredMeetingSummary;
import com.meeting.manager.repository.MeetingRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 会议分析服务类
 * 使用通义千问API分析会议内容
 */
@Service
@Slf4j
public class MeetingAnalysisService {
    
    private static final String SYSTEM_PROMPT = "你是一个专业的会议纪要分析师，专门负责从会议录音或文档中提取结构化信息。\n" +
            "你需要将会议内容分析成结构化的JSON格式，包含以下部分：\n" +
            "1. 会议基本信息（会议名称、会议时间、会议地点、参会人员）\n" +
            "2. 议题讨论（分点列出关键议题及各方观点）\n" +
            "3. 决策事项（明确达成的共识、决议）\n" +
            "4. 行动项（责任人、任务描述、截止时间）\n" +
            "5. 待跟进问题（未解决的议题及后续计划）\n" +
            "请确保输出格式严格为JSON，不包含任何其他内容。";
    
    private final HttpClient httpClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private com.meeting.manager.config.HttpClientConfig httpClientConfig;
    
    @Autowired
    private MeetingRecordRepository meetingRecordRepository;
    
    @Autowired
    public MeetingAnalysisService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public StructuredMeetingSummary analyzeMeetingContent(String content) {
        try {
            log.info("开始分析会议内容，长度: {}", content.length());
            
            // 构建请求体
            ChatRequest request = new ChatRequest();
            request.setModel("qwen-max");
            
            List<ChatRequest.ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatRequest.ChatMessage("system", SYSTEM_PROMPT));
            messages.add(new ChatRequest.ChatMessage("user", content));
            
            request.setMessages(messages);
            
            // 发送API请求
            String requestBody = objectMapper.writeValueAsString(request);
            
            HttpPost httpPost = new HttpPost(httpClientConfig.getBaseUrl() + "/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + httpClientConfig.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("API请求失败: {} - {}", statusCode, EntityUtils.toString(response.getEntity()));
                return createDefaultSummary();
            }
            
            // 解析响应
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String contentText = jsonNode.get("choices").get(0).get("message").get("content").asText();
            
            // 提取JSON部分
            String jsonString = extractJsonFromText(contentText);
            
            // 将JSON转换为StructuredMeetingSummary对象
            return objectMapper.readValue(jsonString, StructuredMeetingSummary.class);
            
        } catch (Exception e) {
            log.error("分析会议内容失败", e);
            return createDefaultSummary();
        }
    }
    
    /**
     * 从文本中提取JSON部分
     */
    private String extractJsonFromText(String text) {
        // 查找JSON开始和结束位置
        int startIndex = text.indexOf('{');
        int endIndex = text.lastIndexOf('}');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1);
        }
        
        // 如果找不到JSON，直接返回原文本
        return text;
    }
    
    /**
     * 创建默认的结构化会议总结
     */
    private StructuredMeetingSummary createDefaultSummary() {
        StructuredMeetingSummary summary = new StructuredMeetingSummary();
        
        // 设置默认的会议基本信息
        StructuredMeetingSummary.MeetingBasicInfo basicInfo = new StructuredMeetingSummary.MeetingBasicInfo();
        basicInfo.setMeetingName("未命名会议");
        basicInfo.setMeetingDate("未知时间");
        basicInfo.setLocation("未知地点");
        summary.setBasicInfo(basicInfo);
        
        // 设置空的议题列表
        summary.setDiscussionPoints(new ArrayList<>());
        
        // 设置空的决策列表
        summary.setDecisions(new ArrayList<>());
        
        // 设置空的行动项列表
        summary.setActionItems(new ArrayList<>());
        
        // 设置空的待跟进问题列表
        summary.setFollowUps(new ArrayList<>());
        
        return summary;
    }
}