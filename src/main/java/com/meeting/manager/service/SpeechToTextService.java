package com.meeting.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class SpeechToTextService {

    @Value("${langchain4j.bailian.api-key:sk-ad82ed8f9a4c4b27a641a8a3c24cb4a3}")
    private String apiKey;

    @Value("${langchain4j.bailian.base-url:https://dashscope.aliyuncs.com/bailian/v1}")
    private String baseUrl;

    public String convertSpeechToText(String filePath) throws Exception {
        log.info("开始语音转文字处理: {}", filePath);

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("音频文件不存在: " + filePath);
        }

        byte[] audioData = Files.readAllBytes(path);
        String audioBase64 = Base64.getEncoder().encodeToString(audioData);

        String prompt = "请将这段音频转录为中文文字，保持原始格式和内容。";

        return callBailianSpeechAPI(audioBase64, prompt);
    }

    private String callBailianSpeechAPI(String audioBase64, String prompt) throws IOException {
        String url = baseUrl + "/audio/asr";
        
        String requestBody = buildBailianRequestBody(audioBase64, prompt);
        
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return parseBailianResponse(connection);
        } else {
            String error = readErrorResponse(connection);
            log.error("语音转文字API调用失败: {} - {}", responseCode, error);
            throw new RuntimeException("语音转文字失败: " + error);
        }
    }

    private String buildBailianRequestBody(String audioBase64, String prompt) {
        return String.format(
            "{\n" +
            "  \"audio\": \"%s\",\n" +
            "  \"prompt\": \"%s\",\n" +
            "  \"language\": \"zh\",\n" +
            "  \"response_format\": \"json\"\n" +
            "}",
            audioBase64, prompt
        );
    }

    private String parseBailianResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String responseStr = response.toString();
        // 解析百炼平台语音转文字API响应
        if (responseStr.contains("\"result\":")) {
            int start = responseStr.indexOf("\"result\":\"") + 10;
            int end = responseStr.indexOf("\"", start);
            if (start > 9 && end > start) {
                return responseStr.substring(start, end);
            }
        }
        
        return responseStr;
    }

    private String readErrorResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        StringBuilder error = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            error.append(line);
        }
        reader.close();
        
        return error.toString();
    }
}