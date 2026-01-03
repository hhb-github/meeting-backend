package com.meeting.manager.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadRequest {
    
    @NotNull(message = "会议名称不能为空")
    @Size(min = 1, max = 255, message = "会议名称长度必须在1-255字符之间")
    private String meetingName;
    
    @Size(max = 500, message = "会议主题长度不能超过500字符")
    private String meetingTopic;
    
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    private String language;
    
    private String location;
    
    private List<ParticipantInfo> participants;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInfo {
        @NotNull(message = "参会人员姓名不能为空")
        private String name;
        
        private String role;
        private String department;
        private String email;
        private String phone;
        private String attendanceStatus;
    }
}