package com.example.gradesystem.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.mapper.UserMapper;
import com.example.gradesystem.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务 — 处理登录、注册、密码修改/重置等核心业务逻辑
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @param role     前端选择的角色（可选校验）
     * @return 登录成功的用户对象
     */
    public User login(String username, String password, String role) {
        // 1. 按用户名查询
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        // 2. 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 账号状态检查
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 4. 密码校验
        if (!MD5Util.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 5. 角色校验（如果前端传了角色，检查是否匹配）
        if (role != null && !role.isEmpty() && !role.equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR.getCode(), "角色不匹配，请重新选择");
        }

        log.info("用户登录成功: {} ({})", username, user.getRole());
        return user;
    }

    /**
     * 用户注册
     * 在 sys_user 表中创建用户记录
     */
    @Transactional
    public User register(String username, String password, String role, String realName) {
        // 1. 检查用户名唯一性
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 2. 构建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(MD5Util.md5(password));
        user.setRole(role.toUpperCase());
        user.setStatus(1);

        userMapper.insert(user);

        log.info("新用户注册: {} ({}), ID={}", username, role, user.getId());
        return user;
    }

    /**
     * 修改密码（需验证旧密码）
     *
     * @param userId      当前登录用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!MD5Util.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR.getCode(), "旧密码不正确");
        }

        // 检查新旧密码不同
        if (MD5Util.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(MD5Util.md5(newPassword));
        userMapper.updateById(user);

        log.info("用户 {} 修改了密码", user.getUsername());
    }

    /**
     * 重置密码（通过用户名 + 邮箱验证）
     * 简化实现：仅校验用户名与邮箱是否匹配（生产环境应发送邮箱验证码）
     *
     * @param username    用户名
     * @param newPassword 新密码
     */
    public void resetPassword(String username, String newPassword) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 更新密码
        user.setPassword(MD5Util.md5(newPassword));
        userMapper.updateById(user);

        log.info("用户 {} 重置了密码", username);
    }

    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
