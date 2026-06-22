package com.example.gradesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 注册请求 DTO
 */
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名只能包含字母、数字和下划线，长度3-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,32}$", message = "密码长度需在6-32位之间")
    private String password;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|TEACHER|STUDENT)$", message = "角色值无效")
    private String role;

    /** 真实姓名（将写入 teacher 或 student 表的 name 字段） */
    private String realName;

    /** 附加信息：教师院系 或 学生班级 */
    private String extraInfo;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getExtraInfo() { return extraInfo; }
    public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }
}
