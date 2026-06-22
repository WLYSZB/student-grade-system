# 学生成绩管理系统

## 技术栈
- 后端：Spring Boot 3.3.5 + MyBatis-Plus 3.5.7
- 前端：HTML5 + CSS3 + JavaScript（原生）
- 数据库：SQL Server
- 构建工具：Maven 3.9+

## 快速启动

### 1. 创建数据库
在 SQL Server Management Studio (SSMS) 中执行：
```sql
CREATE DATABASE grade_system;
GO
USE grade_system;
-- 然后执行 src/main/resources/db/init.sql 中的全部SQL
```

### 2. 修改配置
编辑 `src/main/resources/application.yml`，修改数据库密码：
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=grade_system;encrypt=false;trustServerCertificate=true
    username: sa
    password: your_password  # 改为你的SQL Server密码
```

### 3. 编译运行
```bash
mvn spring-boot:run
```

### 4. 访问系统
浏览器打开 `http://localhost:8080`

### 默认账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 项目结构
```
student-grade-system/
├── pom.xml                          # Maven配置
├── src/main/java/com/example/gradesystem/
│   ├── GradeSystemApplication.java  # 启动类
│   ├── config/                      # 配置类
│   │   ├── WebMvcConfig.java        # 拦截器/跨域/静态资源
│   │   └── MyBatisPlusConfig.java   # 分页/自动填充
│   ├── common/                      # 公共模块
│   │   ├── Result.java              # 统一返回结果
│   │   ├── PageResult.java          # 分页结果
│   │   ├── ErrorCode.java           # 错误码枚举
│   │   ├── BusinessException.java   # 业务异常
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── LoginInterceptor.java    # 登录拦截器
│   │   └── RoleInterceptor.java     # 角色权限拦截器
│   ├── entity/                      # 实体类
│   │   ├── User.java                # 用户
│   │   ├── Teacher.java             # 教师
│   │   ├── Student.java             # 学生
│   │   ├── Course.java              # 课程
│   │   ├── Semester.java            # 学期
│   │   ├── Grade.java               # 成绩
│   │   ├── GradeModifyLog.java      # 成绩修改日志
│   │   └── Notice.java              # 通知公告
│   ├── mapper/                      # MyBatis Mapper接口（待开发）
│   ├── service/                     # 业务逻辑层（待开发）
│   ├── controller/                  # 控制器层（待开发）
│   └── util/                        # 工具类
│       ├── ExcelUtil.java           # Excel导入导出
│       └── DateUtil.java            # 日期工具
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   ├── db/init.sql                  # 数据库初始化脚本
│   └── static/                      # 前端静态资源
│       ├── index.html               # 入口页面
│       ├── css/style.css            # 全局样式
│       └── pages/
│           ├── login.html           # 登录页
│           ├── admin.html           # 管理员端（骨架）
│           ├── teacher.html         # 教师端（骨架）
│           └── student.html         # 学生端（骨架）
└── target/                          # 编译输出
```

## 成员1已完成（成员2-5可在此基础上开发）

| 模块 | 状态 | 说明 |
|------|------|------|
| 项目骨架 | ✅ 完成 | Spring Boot项目结构、pom.xml依赖 |
| 数据库脚本 | ✅ 完成 | 8张表 + 索引 + 初始数据 |
| 实体类 | ✅ 完成 | 全部8个实体类 |
| 公共模块 | ✅ 完成 | Result/PageResult/ErrorCode/异常处理 |
| 登录拦截器 | ✅ 完成 | Session校验 + 角色权限校验 |
| 前端骨架 | ✅ 完成 | 登录页 + 三端骨架页面 |
| CSS样式 | ✅ 完成 | 全局统一样式 |

## 各成员开发指引

### 成员2（后端核心）
在 `controller/auth/` 和 `service/auth/` 下开发登录认证模块

### 成员3（教师端）
在 `controller/teacher/` 和 `service/teacher/` 下开发成绩录入/修改/统计

### 成员4（学生端）
在 `controller/student/` 和 `service/student/` 下开发成绩查询/分析/通知

### 成员5（管理员端）
在 `controller/admin/` 和 `service/admin/` 下开发用户管理/通知/帮助

## API规范

### 统一返回格式
```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

### 错误码
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 1001-1004 | 用户相关错误 |
| 2001-2003 | 成绩相关错误 |
| 500 | 系统错误 |

### URL命名规范
- `/api/auth/login` - 登录
- `/api/auth/register` - 注册
- `/api/teacher/grades` - 教师成绩管理
- `/api/student/grades` - 学生成绩查询
- `/api/admin/users` - 管理员用户管理
- `/api/notice/list` - 通知列表
