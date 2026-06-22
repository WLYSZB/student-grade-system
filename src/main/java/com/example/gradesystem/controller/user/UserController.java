package com.example.gradesystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.dto.UserRequest;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {
        return Result.success(userService.listUsers(pageNum, pageSize, role));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    public Result<User> create(@RequestBody UserRequest request) {
        return Result.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody UserRequest request) {
        return Result.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<User> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(userService.updateUserStatus(id, status));
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success(null);
    }
}