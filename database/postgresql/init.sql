-- PostgreSQL 辅助数据库初始化脚本
-- 用于存储代码执行、性能分析等数据

CREATE DATABASE programming_platform_analytics;
\c programming_platform_analytics;

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- 代码提交表
CREATE TABLE code_submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    code_content TEXT NOT NULL,
    submission_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    execution_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, RUNNING, SUCCESS, ERROR, TIMEOUT
    execution_time_ms INTEGER,
    memory_usage_kb INTEGER,
    output TEXT,
    error_message TEXT,
    test_cases_passed INTEGER DEFAULT 0,
    test_cases_total INTEGER DEFAULT 0,
    score DECIMAL(5,2) DEFAULT 0.00,
    
    CONSTRAINT chk_execution_status CHECK (execution_status IN ('PENDING', 'RUNNING', 'SUCCESS', 'ERROR', 'TIMEOUT'))
);

-- 创建索引
CREATE INDEX idx_code_submissions_user_id ON code_submissions(user_id);
CREATE INDEX idx_code_submissions_lesson_id ON code_submissions(lesson_id);
CREATE INDEX idx_code_submissions_course_id ON code_submissions(course_id);
CREATE INDEX idx_code_submissions_language ON code_submissions(language);
CREATE INDEX idx_code_submissions_submission_time ON code_submissions(submission_time);
CREATE INDEX idx_code_submissions_status ON code_submissions(execution_status);

-- 代码执行结果详情表
CREATE TABLE execution_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id UUID NOT NULL REFERENCES code_submissions(id) ON DELETE CASCADE,
    test_case_id VARCHAR(100),
    test_case_name VARCHAR(200),
    input_data TEXT,
    expected_output TEXT,
    actual_output TEXT,
    execution_time_ms INTEGER,
    memory_usage_kb INTEGER,
    is_passed BOOLEAN DEFAULT FALSE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_execution_results_submission_id ON execution_results(submission_id);
CREATE INDEX idx_execution_results_test_case_id ON execution_results(test_case_id);
CREATE INDEX idx_execution_results_is_passed ON execution_results(is_passed);

-- 性能分析表
CREATE TABLE performance_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT,
    language VARCHAR(50) NOT NULL,
    metric_type VARCHAR(50) NOT NULL, -- EXECUTION_TIME, MEMORY_USAGE, CODE_QUALITY, COMPLETION_RATE
    metric_value DECIMAL(10,4) NOT NULL,
    metric_unit VARCHAR(20), -- ms, kb, percentage, score
    analysis_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_metric_type CHECK (metric_type IN ('EXECUTION_TIME', 'MEMORY_USAGE', 'CODE_QUALITY', 'COMPLETION_RATE'))
);

CREATE INDEX idx_performance_analytics_user_id ON performance_analytics(user_id);
CREATE INDEX idx_performance_analytics_course_id ON performance_analytics(course_id);
CREATE INDEX idx_performance_analytics_lesson_id ON performance_analytics(lesson_id);
CREATE INDEX idx_performance_analytics_language ON performance_analytics(language);
CREATE INDEX idx_performance_analytics_metric_type ON performance_analytics(metric_type);
CREATE INDEX idx_performance_analytics_analysis_date ON performance_analytics(analysis_date);

-- 代码质量分析表
CREATE TABLE code_quality_analysis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id UUID NOT NULL REFERENCES code_submissions(id) ON DELETE CASCADE,
    complexity_score INTEGER, -- 圈复杂度
    maintainability_index DECIMAL(5,2), -- 可维护性指数
    lines_of_code INTEGER,
    code_duplication_percentage DECIMAL(5,2),
    security_issues_count INTEGER DEFAULT 0,
    performance_issues_count INTEGER DEFAULT 0,
    style_issues_count INTEGER DEFAULT 0,
    overall_grade CHAR(1), -- A, B, C, D, F
    analysis_details JSONB, -- 详细分析结果
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_code_quality_submission_id ON code_quality_analysis(submission_id);
CREATE INDEX idx_code_quality_overall_grade ON code_quality_analysis(overall_grade);
CREATE INDEX idx_code_quality_complexity ON code_quality_analysis(complexity_score);

-- 学习行为分析表
CREATE TABLE learning_behavior_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100),
    course_id BIGINT,
    lesson_id BIGINT,
    action_type VARCHAR(50) NOT NULL, -- VIEW, SUBMIT, COMPLETE, PAUSE, RESUME, SKIP
    action_details JSONB,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    duration_seconds INTEGER,
    device_type VARCHAR(50), -- DESKTOP, MOBILE, TABLET
    browser VARCHAR(100),
    ip_address INET,
    
    CONSTRAINT chk_action_type CHECK (action_type IN ('VIEW', 'SUBMIT', 'COMPLETE', 'PAUSE', 'RESUME', 'SKIP'))
);

CREATE INDEX idx_learning_behavior_user_id ON learning_behavior_analytics(user_id);
CREATE INDEX idx_learning_behavior_session_id ON learning_behavior_analytics(session_id);
CREATE INDEX idx_learning_behavior_course_id ON learning_behavior_analytics(course_id);
CREATE INDEX idx_learning_behavior_lesson_id ON learning_behavior_analytics(lesson_id);
CREATE INDEX idx_learning_behavior_action_type ON learning_behavior_analytics(action_type);
CREATE INDEX idx_learning_behavior_timestamp ON learning_behavior_analytics(timestamp);

-- 系统性能监控表
CREATE TABLE system_performance_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(20),
    service_name VARCHAR(100), -- backend, frontend, database, redis, etc.
    instance_id VARCHAR(100),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    tags JSONB -- 额外的标签信息
);

CREATE INDEX idx_system_performance_metric_name ON system_performance_metrics(metric_name);
CREATE INDEX idx_system_performance_service_name ON system_performance_metrics(service_name);
CREATE INDEX idx_system_performance_timestamp ON system_performance_metrics(timestamp);

-- 创建分区表（按月分区）
CREATE TABLE code_submissions_partitioned (
    LIKE code_submissions INCLUDING ALL
) PARTITION BY RANGE (submission_time);

-- 创建当前月份的分区
CREATE TABLE code_submissions_y2024m01 PARTITION OF code_submissions_partitioned
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE code_submissions_y2024m02 PARTITION OF code_submissions_partitioned
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- 创建视图用于常用查询
CREATE VIEW user_performance_summary AS
SELECT 
    user_id,
    language,
    COUNT(*) as total_submissions,
    AVG(execution_time_ms) as avg_execution_time,
    AVG(memory_usage_kb) as avg_memory_usage,
    AVG(score) as avg_score,
    SUM(CASE WHEN execution_status = 'SUCCESS' THEN 1 ELSE 0 END)::DECIMAL / COUNT(*) * 100 as success_rate
FROM code_submissions
GROUP BY user_id, language;

CREATE VIEW daily_activity_summary AS
SELECT 
    DATE(submission_time) as activity_date,
    language,
    COUNT(*) as total_submissions,
    COUNT(DISTINCT user_id) as active_users,
    AVG(execution_time_ms) as avg_execution_time,
    SUM(CASE WHEN execution_status = 'SUCCESS' THEN 1 ELSE 0 END)::DECIMAL / COUNT(*) * 100 as success_rate
FROM code_submissions
GROUP BY DATE(submission_time), language
ORDER BY activity_date DESC;

-- 创建函数用于清理旧数据
CREATE OR REPLACE FUNCTION cleanup_old_data(days_to_keep INTEGER DEFAULT 90)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM code_submissions 
    WHERE submission_time < CURRENT_TIMESTAMP - INTERVAL '1 day' * days_to_keep;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    DELETE FROM learning_behavior_analytics 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '1 day' * days_to_keep;
    
    DELETE FROM system_performance_metrics 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '1 day' * days_to_keep;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- 创建定时清理任务（需要pg_cron扩展）
-- SELECT cron.schedule('cleanup-old-data', '0 2 * * *', 'SELECT cleanup_old_data(90);');

COMMENT ON DATABASE programming_platform_analytics IS '编程学习平台分析数据库';
COMMENT ON TABLE code_submissions IS '代码提交记录表';
COMMENT ON TABLE execution_results IS '代码执行结果详情表';
COMMENT ON TABLE performance_analytics IS '性能分析数据表';
COMMENT ON TABLE code_quality_analysis IS '代码质量分析表';
COMMENT ON TABLE learning_behavior_analytics IS '学习行为分析表';
COMMENT ON TABLE system_performance_metrics IS '系统性能监控表';
