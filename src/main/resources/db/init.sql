-- ============================================
-- 学生成绩管理系统 - 数据库初始化脚本 (SQL Server)
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'grade_system')
BEGIN
    CREATE DATABASE grade_system;
END
GO

USE grade_system;
GO

-- -------------------------------------------
-- 1. 用户表（统一登录入口）
-- -------------------------------------------
IF OBJECT_ID('sys_user', 'U') IS NOT NULL DROP TABLE sys_user;
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY IDENTITY(1,1),
    username    NVARCHAR(50)  NOT NULL,
    password    NVARCHAR(100) NOT NULL,
    role        NVARCHAR(20)  NOT NULL,
    status      TINYINT       NOT NULL DEFAULT 1,
    create_time DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time DATETIME      NOT NULL DEFAULT GETDATE(),
    deleted     TINYINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_username UNIQUE (username)
);
GO

-- -------------------------------------------
-- 2. 教师表
-- -------------------------------------------
IF OBJECT_ID('teacher', 'U') IS NOT NULL DROP TABLE teacher;
CREATE TABLE teacher (
    id          BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id     BIGINT       NOT NULL,
    name        NVARCHAR(50) NOT NULL,
    department  NVARCHAR(100) DEFAULT NULL,
    phone       NVARCHAR(20)  DEFAULT NULL,
    create_time DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time DATETIME      NOT NULL DEFAULT GETDATE(),
    deleted     TINYINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_teacher_user_id UNIQUE (user_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
GO

-- -------------------------------------------
-- 3. 学生表
-- -------------------------------------------
IF OBJECT_ID('student', 'U') IS NOT NULL DROP TABLE student;
CREATE TABLE student (
    id              BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id         BIGINT       NOT NULL,
    name            NVARCHAR(50) NOT NULL,
    class_name      NVARCHAR(50)  DEFAULT NULL,
    major           NVARCHAR(100) DEFAULT NULL,
    enrollment_year INT           DEFAULT NULL,
    create_time     DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time     DATETIME      NOT NULL DEFAULT GETDATE(),
    deleted         TINYINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_student_user_id UNIQUE (user_id),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
GO

-- -------------------------------------------
-- 4. 学期表
-- -------------------------------------------
IF OBJECT_ID('semester', 'U') IS NOT NULL DROP TABLE semester;
CREATE TABLE semester (
    id          BIGINT PRIMARY KEY IDENTITY(1,1),
    name        NVARCHAR(50) NOT NULL,
    start_date  DATE         DEFAULT NULL,
    end_date    DATE         DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT GETDATE(),
    deleted     TINYINT      NOT NULL DEFAULT 0
);
GO

-- -------------------------------------------
-- 5. 课程表
-- -------------------------------------------
IF OBJECT_ID('course', 'U') IS NOT NULL DROP TABLE course;
CREATE TABLE course (
    id          BIGINT PRIMARY KEY IDENTITY(1,1),
    name        NVARCHAR(100) NOT NULL,
    credit      DECIMAL(3,1)  NOT NULL,
    teacher_id  BIGINT        DEFAULT NULL,
    semester_id BIGINT        DEFAULT NULL,
    create_time DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time DATETIME      NOT NULL DEFAULT GETDATE(),
    deleted     TINYINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id),
    CONSTRAINT fk_course_semester FOREIGN KEY (semester_id) REFERENCES semester(id)
);
GO

-- -------------------------------------------
-- 6. 成绩表
-- -------------------------------------------
IF OBJECT_ID('grade', 'U') IS NOT NULL DROP TABLE grade;
CREATE TABLE grade (
    id          BIGINT PRIMARY KEY IDENTITY(1,1),
    student_id  BIGINT        NOT NULL,
    course_id   BIGINT        NOT NULL,
    score       DECIMAL(5,2)  DEFAULT NULL,
    grade_point DECIMAL(3,2)  DEFAULT NULL,
    recorder_id BIGINT        DEFAULT NULL,
    record_time DATETIME      NOT NULL DEFAULT GETDATE(),
    create_time DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time DATETIME      NOT NULL DEFAULT GETDATE(),
    deleted     TINYINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_student_course UNIQUE (student_id, course_id),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES course(id)
);
GO

CREATE INDEX idx_grade_course_id ON grade(course_id);
GO

-- -------------------------------------------
-- 7. 成绩修改日志表
-- -------------------------------------------
IF OBJECT_ID('grade_modify_log', 'U') IS NOT NULL DROP TABLE grade_modify_log;
CREATE TABLE grade_modify_log (
    id           BIGINT PRIMARY KEY IDENTITY(1,1),
    grade_id     BIGINT        NOT NULL,
    old_score    DECIMAL(5,2)  DEFAULT NULL,
    new_score    DECIMAL(5,2)  DEFAULT NULL,
    reason       NVARCHAR(500) DEFAULT NULL,
    applicant_id BIGINT        DEFAULT NULL,
    approver_id  BIGINT        DEFAULT NULL,
    status       NVARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    create_time  DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time  DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_log_grade FOREIGN KEY (grade_id) REFERENCES grade(id)
);
GO

-- -------------------------------------------
-- 8. 通知公告表
-- -------------------------------------------
IF OBJECT_ID('notice', 'U') IS NOT NULL DROP TABLE notice;
CREATE TABLE notice (
    id           BIGINT PRIMARY KEY IDENTITY(1,1),
    title        NVARCHAR(200) NOT NULL,
    content      NVARCHAR(MAX) NOT NULL,
    publisher_id BIGINT        DEFAULT NULL,
    target_role  NVARCHAR(20)  DEFAULT 'ALL',
    status       NVARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    publish_time DATETIME      DEFAULT NULL,
    create_time  DATETIME      NOT NULL DEFAULT GETDATE(),
    update_time  DATETIME      NOT NULL DEFAULT GETDATE(),
    deleted      TINYINT       NOT NULL DEFAULT 0
);
GO

CREATE INDEX idx_notice_target_role ON notice(target_role);
GO

-- ============================================
-- 初始数据
-- ============================================

-- 默认管理员账号（密码: admin123，MD5加密）
INSERT INTO sys_user (username, password, role, status) VALUES
('admin', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'ADMIN', 1);
GO

-- 默认学期
INSERT INTO semester (name, start_date, end_date) VALUES
('2025-2026学年第一学期', '2025-09-01', '2026-01-15'),
('2025-2026学年第二学期', '2026-02-20', '2026-07-10');
GO
