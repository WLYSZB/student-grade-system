# 学生成绩管理系统 v1.0

## 技术栈
- 后端：Spring Boot 3.5.0 + MyBatis-Plus 3.5.7
- 前端：HTML5 + CSS3 + JavaScript（原生）
- 数据库：SQL Server
- 构建工具：Maven 3.9+

## 快速启动

### 1. 环境准备
- JDK 17+
- Maven 3.9+
- SQL Server（需启用 TCP/IP 协议）

### 2. 创建数据库
```bash
sqlcmd -S localhost -U sa -P "你的密码" -i src/main/resources/db/init.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`，改数据库密码：
```yaml
password: your_password  # 改为你的 SQL Server 密码
```

### 4. 启动
```bash
mvn spring-boot:run
```
或直接运行 JAR：
```bash
java -jar grade-system-1.0.0.jar --spring.datasource.password=你的密码
```

### 5. 访问
浏览器打开 `http://localhost:8088`

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | teacher1 | teacher123 |
| 学生 | student1 | student123 |

## 项目结构
```
├── pom.xml
├── src/main/java/com/example/gradesystem/
│   ├── GradeSystemApplication.java      # 启动类
│   ├── config/                          # 拦截器/跨域/分页
│   ├── common/                          # Result/ErrorCode/异常处理
│   ├── entity/                          # 实体类（8个）
│   ├── dto/                             # 请求体
│   ├── mapper/                          # MyBatis Mapper
│   ├── controller/
│   │   ├── auth/AuthController.java     # 登录认证
│   │   ├── teacher/GradeController.java # 成绩管理
│   │   ├── admin/CourseController.java  # 课程管理
│   │   └── user/UserController.java     # 用户管理
│   ├── service/                         # 业务逻辑层
│   └── util/                            # MD5/Excel/Date
├── src/main/resources/
│   ├── application.yml
│   ├── db/init.sql                      # 数据库初始化
│   └── static/                          # 前端页面
└── test_*.csv / test_*.json             # 测试数据
```

## API 接口

| 接口 | 方法 | 需登录 | 说明 |
|------|:----:|:------:|------|
| `/api/auth/login` | POST | 否 | 用户登录 |
| `/api/auth/register` | POST | 否 | 用户注册 |
| `/api/auth/me` | GET | 是 | 获取当前用户 |
| `/api/auth/change-password` | PUT | 是 | 修改密码 |
| `/api/auth/reset-password` | POST | 否 | 重置密码 |
| `/api/auth/logout` | POST | 是 | 退出登录 |
| `/api/teacher/grades/list` | GET | 是 | 成绩列表 |
| `/api/teacher/grades` | POST | 是 | 录入成绩 |
| `/api/teacher/grades/batch` | POST | 是 | 批量导入成绩 |
| `/api/teacher/grades/statistics` | GET | 是 | 成绩统计 |
| `/api/teacher/grades/ranking` | GET | 是 | 成绩排名 |
| `/api/teacher/grades/students` | GET | 是 | 学生下拉 |
| `/api/teacher/grades/courses` | GET | 是 | 课程下拉 |
| `/api/admin/courses` | GET | 是 | 课程管理 |
| `/api/admin/courses/import` | POST | 是 | CSV批量导入课程 |
| `/api/admin/users` | GET | 是 | 用户管理 |

## 统一返回格式
```json
{"code": 200, "message": "success", "data": {}}
```

## 错误码
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 1001-1006 | 用户/课程/学生相关 |
| 2001-2003 | 成绩相关 |
| 500 | 系统错误 |

## 测试数据文件
- `test_courses.csv` — 10门课程（管理员端 → 课程管理 → 批量导入）
- `test_students.csv` — 10名学生（管理员端 → 用户管理 → 批量导入）
- `test_grades.json` — 9条成绩（教师端 → 成绩管理 → 批量导入）
