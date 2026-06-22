package com.example.gradesystem.controller.auth;

import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.dto.ChangePasswordRequest;
import com.example.gradesystem.dto.LoginRequest;
import com.example.gradesystem.dto.RegisterRequest;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.service.auth.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 — 处理登录、注册、密码管理等 HTTP 请求
 *
 * 所有接口返回统一格式 Result<T>
 * 登录成功后将用户信息存入 HttpSession
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     * POST /api/auth/login
     *
     * 登录成功后将用户信息存入 Session，
     * 后续请求由 LoginInterceptor 自动校验 Session。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                              HttpSession session) {
        User user = authService.login(
                request.getUsername(),
                request.getPassword(),
                request.getRole()
        );

        // 将用户信息存入 Session（供 LoginInterceptor 校验）
        session.setAttribute("currentUser", user);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());

        log.info("POST /api/auth/login → 200 (user={})", user.getUsername());
        return Result.success(data);
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getRole(),
                request.getRealName()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());

        log.info("POST /api/auth/register → 200 (user={})", user.getUsername());
        return Result.success(data);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/auth/me
     *
     * 需要登录（通过 LoginInterceptor）
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }

        // 重新从数据库加载，获取最新数据
        User freshUser = authService.getUserById(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("id", freshUser.getId());
        data.put("username", freshUser.getUsername());
        data.put("role", freshUser.getRole());
        data.put("status", freshUser.getStatus());
        data.put("createTime", freshUser.getCreateTime());

        return Result.success(data);
    }

    /**
     * 修改密码（需登录）
     * PUT /api/auth/change-password
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                        HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }

        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        // 清除 Session，要求重新登录
        session.invalidate();

        log.info("PUT /api/auth/change-password → 200 (user={})", user.getUsername());
        return Result.success(null);
    }

    /**
     * 忘记密码 - 重置密码
     * POST /api/auth/reset-password
     *
     * 通过用户名验证身份后重置密码（课程设计简化实现）
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ChangePasswordRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Result.error(ErrorCode.PARAM_ERROR.getCode(), "用户名不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return Result.error(ErrorCode.PARAM_ERROR.getCode(), "新密码不能为空");
        }
        authService.resetPassword(request.getUsername().trim(), request.getNewPassword());

        log.info("POST /api/auth/reset-password → 200 (user={})", request.getUsername());
        return Result.success(null);
    }

    /**
     * 退出登录
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success(null);
    }
}
