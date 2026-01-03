package com.meeting.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload.path}")
    private String uploadPath;

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + fileExtension;

        Path uploadPath = Paths.get(this.uploadPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path targetPath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    public String extractTextFromDocument(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".txt")) {
            return new String(Files.readAllBytes(path), "UTF-8");
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return extractTextFromWord(path);
        } else if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(path);
        } else {
            throw new UnsupportedOperationException("不支持的文档格式: " + fileName);
        }
    }

    private String extractTextFromWord(Path filePath) throws IOException {
        try {
            // 这里需要集成Apache POI或类似的库来解析Word文档
            // 暂时返回占位符
            return "Word文档内容提取功能需要集成Apache POI库";
        } catch (Exception e) {
            log.error("提取Word文档失败", e);
            throw new RuntimeException("Word文档解析失败", e);
        }
    }

    private String extractTextFromPdf(Path filePath) throws IOException {
        try {
            // 这里需要集成PDF解析库如Apache PDFBox
            // 暂时返回占位符
            return "PDF文档内容提取功能需要集成PDFBox库";
        } catch (Exception e) {
            log.error("提取PDF文档失败", e);
            throw new RuntimeException("PDF文档解析失败", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
}