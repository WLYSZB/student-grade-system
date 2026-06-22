# 学生成绩管理系统

## 技术栈
- 后端：Spring Boot 3.5.0 + MyBatis-Plus 3.5.7
- 前端：HTML5 + CSS3 + JavaScript（原生）
- 数据库：SQL Server
- 构建工具：Maven 3.9+

## 快速启动

### 1. 创建数据库
在 SQL Server Management Studio (SSMS) 或命令行中执行：
```bash
sqlcmd -S localhost -U sa -P "你的密码" -i src/main/resources/db/init.sql
```

### 2. 修改配置
编辑 `src/main/resources/application.yml`，修改数据库密码：
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=grade_system;encrypt=false;trustServerCertificate=true
    username: sa
    password: 你的SQL Server密码
```

### 3. 编译运行
```bash
mvn spring-boot:run
```

### 4. 访问系统
浏览器打开 `http://localhost:8088`

### 测试账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | teacher1 | teacher123 |
| 学生 | student1 | student123 |

## 项目结构
```
├── pom.xml                              # Maven配置
├── src/main/java/com/example/gradesystem/
│   ├── GradeSystemApplication.java      # 启动类
│   ├── config/
│   │   ├── WebMvcConfig.java            # 拦截器/跨域/静态资源
│   │   └── MyBatisPlusConfig.java       # 分页/自动填充
│   ├── common/
│   │   ├── Result.java                  # 统一返回结果
│   │   ├── PageResult.java              # 分页结果
│   │   ├── ErrorCode.java               # 错误码枚举
│   │   ├── BusinessException.java       # 业务异常
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   ├── LoginInterceptor.java        # 登录拦截器
│   │   └── RoleInterceptor.java         # 角色权限拦截器
│   ├── entity/                          # 实体类（8个）
│   │   ├── User.java / Teacher.java / Student.java
│   │   ├── Course.java / Semester.java
│   │   ├── Grade.java / GradeModifyLog.java / Notice.java
│   ├── controller/auth/
│   │   └── AuthController.java          # 认证接口（成员2 ✅）
│   ├── service/auth/
│   │   └── AuthService.java             # 认证业务逻辑（成员2 ✅）
│   ├── mapper/
│   │   └── UserMapper.java              # 用户数据访问（成员2 ✅）
│   ├── dto/                             # 请求体（成员2 ✅）
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── ChangePasswordRequest.java
│   └── util/
│       ├── MD5Util.java                 # MD5加密（成员2 ✅）
│       ├── ExcelUtil.java               # Excel导入导出
│       └── DateUtil.java                # 日期工具
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   ├── db/init.sql                      # 数据库初始化脚本
│   └── static/
│       ├── index.html                   # 入口页面
│       ├── css/style.css                # 全局样式
│       └── pages/
│           ├── login.html               # 登录页（成员2 ✅）
│           ├── register.html            # 注册页（成员2 ✅）
│           ├── admin.html               # 管理员端（待成员5开发）
│           ├── teacher.html             # 教师端（待成员3开发）
│           └── student.html             # 学生端（待成员4开发）
```

## 开发进度

| 模块 | 成员 | 状态 |
|------|:----:|:----:|
| 项目骨架 + 实体类 + 公共模块 | 成员1 | ✅ |
| 登录认证（AuthController/AuthService） | 成员2 | ✅ |
| 教师端（成绩录入/修改/统计） | 成员3 | 🔲 |
| 学生端（成绩查询/分析/通知） | 成员4 | 🔲 |
| 管理员端（用户管理/通知/帮助） | 成员5 | 🔲 |
| 测试 + 文档整合 + 答辩 | 成员6 | 🔲 |

## API 规范

### 统一返回格式
```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

### 已实现接口（成员2）

| 接口 | 方法 | 需登录 | 说明 |
|------|:----:|:------:|------|
| `/api/auth/login` | POST | 否 | 用户登录 |
| `/api/auth/register` | POST | 否 | 用户注册 |
| `/api/auth/me` | GET | 是 | 获取当前用户 |
| `/api/auth/change-password` | PUT | 是 | 修改密码 |
| `/api/auth/reset-password` | POST | 否 | 忘记密码重置 |
| `/api/auth/logout` | POST | 是 | 退出登录 |

### 错误码
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 1001 | 用户不存在 |
| 1002 | 用户名已存在 |
| 1003 | 密码错误 |
| 1004 | 账号已禁用 |
| 500 | 系统错误 |
