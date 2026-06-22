package com.example.gradesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 修改密码 / 重置密码请求 DTO
 */
public class ChangePasswordRequest {

    /** 用户名（重置密码时必填，修改密码时不需要） */
    private String username;

    /** 旧密码（修改密码时必填，重置密码时可不填） */
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^.{6,32}$", message = "密码长度需在6-32位之间")
    private String newPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
