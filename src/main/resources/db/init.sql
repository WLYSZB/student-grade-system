-- ============================================
-- 学生成绩管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS grade_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE grade_system;

-- -------------------------------------------
-- 1. 用户表（统一登录入口）
-- -------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（MD5加密）',
    role        VARCHAR(20)  NOT NULL COMMENT '角色：ADMIN/TEACHER/STUDENT',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='用户表';

-- -------------------------------------------
-- 2. 教师表
-- -------------------------------------------
DROP TABLE IF EXISTS teacher;
CREATE TABLE teacher (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    user_id     BIGINT       NOT NULL COMMENT '关联用户ID',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    department  VARCHAR(100) DEFAULT NULL COMMENT '院系',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB COMMENT='教师表';

-- -------------------------------------------
-- 3. 学生表
-- -------------------------------------------
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    user_id         BIGINT       NOT NULL COMMENT '关联用户ID',
    name            VARCHAR(50)  NOT NULL COMMENT '姓名',
    class_name      VARCHAR(50)  DEFAULT NULL COMMENT '班级',
    major           VARCHAR(100) DEFAULT NULL COMMENT '专业',
    enrollment_year INT          DEFAULT NULL COMMENT '入学年份',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB COMMENT='学生表';

-- -------------------------------------------
-- 4. 学期表
-- -------------------------------------------
DROP TABLE IF EXISTS semester;
CREATE TABLE semester (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学期ID',
    name       VARCHAR(50) NOT NULL COMMENT '学期名称，如 2025-2026学年第一学期',
    start_date DATE        DEFAULT NULL COMMENT '开始日期',
    end_date   DATE        DEFAULT NULL COMMENT '结束日期',
    create_time DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    TINYINT     NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='学期表';

-- -------------------------------------------
-- 5. 课程表
-- -------------------------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    name        VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit      DECIMAL(3,1) NOT NULL COMMENT '学分',
    teacher_id  BIGINT       DEFAULT NULL COMMENT '授课教师ID',
    semester_id BIGINT       DEFAULT NULL COMMENT '所属学期ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id),
    CONSTRAINT fk_course_semester FOREIGN KEY (semester_id) REFERENCES semester(id)
) ENGINE=InnoDB COMMENT='课程表';

-- -------------------------------------------
-- 6. 成绩表
-- -------------------------------------------
DROP TABLE IF EXISTS grade;
CREATE TABLE grade (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    student_id  BIGINT       NOT NULL COMMENT '学生ID',
    course_id   BIGINT       NOT NULL COMMENT '课程ID',
    score       DECIMAL(5,2) DEFAULT NULL COMMENT '成绩（0-100）',
    grade_point DECIMAL(3,2) DEFAULT NULL COMMENT '绩点',
    recorder_id BIGINT       DEFAULT NULL COMMENT '录入人ID',
    record_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_student_course (student_id, course_id),
    KEY idx_course_id (course_id),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB COMMENT='成绩表';

-- -------------------------------------------
-- 7. 成绩修改日志表
-- -------------------------------------------
DROP TABLE IF EXISTS grade_modify_log;
CREATE TABLE grade_modify_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    grade_id    BIGINT       NOT NULL COMMENT '成绩ID',
    old_score   DECIMAL(5,2) DEFAULT NULL COMMENT '原成绩',
    new_score   DECIMAL(5,2) DEFAULT NULL COMMENT '新成绩',
    reason      VARCHAR(500) DEFAULT NULL COMMENT '修改原因',
    applicant_id BIGINT      DEFAULT NULL COMMENT '申请人ID',
    approver_id BIGINT       DEFAULT NULL COMMENT '审批人ID',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_grade FOREIGN KEY (grade_id) REFERENCES grade(id)
) ENGINE=InnoDB COMMENT='成绩修改日志表';

-- -------------------------------------------
-- 8. 通知公告表
-- -------------------------------------------
DROP TABLE IF EXISTS notice;
CREATE TABLE notice (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    title        VARCHAR(200) NOT NULL COMMENT '标题',
    content      TEXT         NOT NULL COMMENT '内容',
    publisher_id BIGINT       DEFAULT NULL COMMENT '发布者ID',
    target_role  VARCHAR(20)  DEFAULT 'ALL' COMMENT '目标角色：ALL/TEACHER/STUDENT',
    status       VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT/PUBLISHED/WITHDRAWN',
    publish_time DATETIME     DEFAULT NULL COMMENT '发布时间',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    KEY idx_target_role (target_role)
) ENGINE=InnoDB COMMENT='通知公告表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认管理员账号（密码: admin123，MD5加密）
INSERT INTO sys_user (username, password, role, status) VALUES
('admin', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'ADMIN', 1);

-- 默认学期
INSERT INTO semester (name, start_date, end_date) VALUES
('2025-2026学年第一学期', '2025-09-01', '2026-01-15'),
('2025-2026学年第二学期', '2026-02-20', '2026-07-10');
