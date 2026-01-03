-- 会议文件转换管理系统数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS meeting_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE meeting_manager;

-- 创建用户（如果不存在）
CREATE USER IF NOT EXISTS 'meeting_user'@'%' IDENTIFIED BY 'meeting_password';
GRANT ALL PRIVILEGES ON meeting_manager.* TO 'meeting_user'@'%';
FLUSH PRIVILEGES;

-- 创建表结构
-- 会议记录表
CREATE TABLE IF NOT EXISTS meeting_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_name VARCHAR(255) NOT NULL COMMENT '会议名称',
    meeting_topic VARCHAR(500) COMMENT '会议主题',
    meeting_date DATETIME COMMENT '会议日期',
    location VARCHAR(255) COMMENT '会议地点',
    source_type VARCHAR(20) NOT NULL COMMENT '文件来源类型',
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
    original_file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_path VARCHAR(500) COMMENT '文件路径',
    meeting_transcript TEXT COMMENT '会议转录文本',
    summary TEXT COMMENT '会议摘要',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_meeting_date (meeting_date),
    INDEX idx_processing_status (processing_status),
    INDEX idx_source_type (source_type),
    INDEX idx_created_at (created_at),
    INDEX idx_meeting_name (meeting_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议记录表';

-- 会议参与者表
CREATE TABLE IF NOT EXISTS meeting_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_record_id BIGINT NOT NULL COMMENT '关联的会议记录ID',
    name VARCHAR(100) NOT NULL COMMENT '参与者姓名',
    role VARCHAR(50) COMMENT '职位/角色',
    department VARCHAR(100) COMMENT '部门',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (meeting_record_id) REFERENCES meeting_records(id) ON DELETE CASCADE,
    INDEX idx_meeting_record_id (meeting_record_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议参与者表';

-- 会议话题表
CREATE TABLE IF NOT EXISTS meeting_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_record_id BIGINT NOT NULL COMMENT '关联的会议记录ID',
    topic_title VARCHAR(255) NOT NULL COMMENT '话题标题',
    description TEXT COMMENT '话题描述',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    duration_minutes INT COMMENT '持续时间（分钟）',
    speaker VARCHAR(100) COMMENT '主要发言人',
    key_points TEXT COMMENT '关键要点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (meeting_record_id) REFERENCES meeting_records(id) ON DELETE CASCADE,
    INDEX idx_meeting_record_id (meeting_record_id),
    INDEX idx_topic_title (topic_title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议话题表';

-- 会议决定表
CREATE TABLE IF NOT EXISTS meeting_decisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_record_id BIGINT NOT NULL COMMENT '关联的会议记录ID',
    decision_content TEXT NOT NULL COMMENT '决定内容',
    decision_type VARCHAR(50) COMMENT '决定类型',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    responsible_person VARCHAR(100) COMMENT '负责人',
    due_date DATE COMMENT '截止日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (meeting_record_id) REFERENCES meeting_records(id) ON DELETE CASCADE,
    INDEX idx_meeting_record_id (meeting_record_id),
    INDEX idx_decision_type (decision_type),
    INDEX idx_priority (priority),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议决定表';

-- 会议行动项表
CREATE TABLE IF NOT EXISTS meeting_action_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_record_id BIGINT NOT NULL COMMENT '关联的会议记录ID',
    action_content TEXT NOT NULL COMMENT '行动项内容',
    assignee VARCHAR(100) NOT NULL COMMENT '执行人',
    assignee_email VARCHAR(100) COMMENT '执行人邮箱',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    due_date DATE COMMENT '截止日期',
    estimated_hours DECIMAL(5,2) COMMENT '预估工时',
    actual_hours DECIMAL(5,2) COMMENT '实际工时',
    completion_notes TEXT COMMENT '完成说明',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (meeting_record_id) REFERENCES meeting_records(id) ON DELETE CASCADE,
    INDEX idx_meeting_record_id (meeting_record_id),
    INDEX idx_assignee (assignee),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议行动项表';

-- 会议跟进表
CREATE TABLE IF NOT EXISTS meeting_follow_ups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_record_id BIGINT NOT NULL COMMENT '关联的会议记录ID',
    follow_up_content TEXT NOT NULL COMMENT '跟进内容',
    follow_up_type VARCHAR(50) COMMENT '跟进类型',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    status VARCHAR(20) DEFAULT 'OPEN' COMMENT '状态',
    responsible_person VARCHAR(100) COMMENT '负责人',
    due_date DATE COMMENT '截止日期',
    follow_up_date DATE COMMENT '跟进日期',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (meeting_record_id) REFERENCES meeting_records(id) ON DELETE CASCADE,
    INDEX idx_meeting_record_id (meeting_record_id),
    INDEX idx_target_type_id (target_type, target_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议跟进表';

-- 创建视图用于报表统计
CREATE OR REPLACE VIEW meeting_statistics AS
SELECT 
    COUNT(*) as total_meetings,
    SUM(CASE WHEN processing_status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_meetings,
    SUM(CASE WHEN processing_status = 'PROCESSING' THEN 1 ELSE 0 END) as processing_meetings,
    SUM(CASE WHEN processing_status = 'FAILED' THEN 1 ELSE 0 END) as failed_meetings,
    COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) as today_meetings,
    COUNT(CASE WHEN YEARWEEK(created_at, 1) = YEARWEEK(CURDATE(), 1) THEN 1 END) as this_week_meetings,
    COUNT(CASE WHEN MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE()) THEN 1 END) as this_month_meetings
FROM meeting_records;

-- 创建触发器用于自动更新统计信息
DELIMITER $$

CREATE TRIGGER update_meeting_statistics AFTER INSERT ON meeting_records
FOR EACH ROW
BEGIN
    -- 可以在这里添加统计信息更新的逻辑
    -- 例如：更新缓存表、发送通知等
END$$

CREATE TRIGGER update_meeting_statistics_update AFTER UPDATE ON meeting_records
FOR EACH ROW
BEGIN
    -- 处理状态变更时的逻辑
    IF NEW.processing_status != OLD.processing_status THEN
        -- 发送通知或更新统计信息
    END IF;
END$$

DELIMITER ;

-- 插入一些测试数据（可选）
INSERT IGNORE INTO meeting_records (meeting_name, meeting_topic, meeting_date, location, source_type, processing_status, original_file_name) VALUES
('周例会', '项目进度汇报', NOW(), '会议室A', 'AUDIO', 'COMPLETED', 'weekly_meeting.wav'),
('需求评审会', '新产品功能讨论', NOW(), '会议室B', 'DOCUMENT', 'PROCESSING', 'requirements.docx'),
('技术方案讨论', '系统架构优化', NOW(), '会议室C', 'AUDIO', 'PENDING', 'tech_discussion.mp3');

-- 插入对应的参与者数据
INSERT IGNORE INTO meeting_participants (meeting_record_id, name, role, department) VALUES
(1, '张三', '项目经理', '产品部'),
(1, '李四', '技术负责人', '技术部'),
(2, '王五', '产品经理', '产品部'),
(2, '赵六', 'UI设计师', '设计部');

-- 插入对应的行动项数据
INSERT IGNORE INTO meeting_action_items (meeting_record_id, action_content, assignee, priority, due_date) VALUES
(1, '完成项目文档更新', '张三', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
(1, '准备下周演示材料', '李四', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(2, '整理用户反馈', '王五', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 3 DAY));

-- 创建索引以优化查询性能
CREATE INDEX idx_meeting_record_status_date ON meeting_records(processing_status, created_at);
CREATE INDEX idx_action_item_assignee_status ON meeting_action_items(assignee, status);
CREATE INDEX idx_decision_status_priority ON meeting_decisions(status, priority);

-- 显示表创建成功信息
SELECT 'Database initialization completed successfully!' as message;