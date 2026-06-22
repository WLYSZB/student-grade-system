package com.example.gradesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.dto.TeacherRequest;
import com.example.gradesystem.entity.Teacher;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.mapper.TeacherMapper;
import com.example.gradesystem.mapper.UserMapper;
import com.example.gradesystem.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    public IPage<Teacher> listTeachers(int pageNum, int pageSize, String department) {
        Page<Teacher> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (department != null && !department.isEmpty()) {
            wrapper.like(Teacher::getDepartment, department);
        }
        wrapper.orderByDesc(Teacher::getCreateTime);
        return teacherMapper.selectPage(page, wrapper);
    }

    public Teacher getTeacherById(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return teacher;
    }

    public Teacher getTeacherByUserId(Long userId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getUserId, userId);
        return teacherMapper.selectOne(wrapper);
    }

    @Transactional
    public Teacher createTeacher(TeacherRequest request) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(MD5Util.md5(request.getPassword() != null ? request.getPassword() : "123456"));
        user.setRole("TEACHER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        Teacher teacher = new Teacher();
        teacher.setUserId(user.getId());
        teacher.setName(request.getName());
        teacher.setDepartment(request.getDepartment());
        teacher.setPhone(request.getPhone());
        teacher.setCreateTime(LocalDateTime.now());
        teacher.setUpdateTime(LocalDateTime.now());
        teacherMapper.insert(teacher);

        return teacher;
    }

    @Transactional
    public Teacher updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userMapper.selectById(teacher.getUserId());
        if (user != null) {
            if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(User::getUsername, request.getUsername());
                if (userMapper.selectCount(wrapper) > 0) {
                    throw new BusinessException(ErrorCode.USERNAME_EXISTS);
                }
                user.setUsername(request.getUsername());
            }

            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(MD5Util.md5(request.getPassword()));
            }

            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        if (request.getName() != null) {
            teacher.setName(request.getName());
        }
        if (request.getDepartment() != null) {
            teacher.setDepartment(request.getDepartment());
        }
        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }
        teacher.setUpdateTime(LocalDateTime.now());
        teacherMapper.updateById(teacher);

        return teacher;
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        userMapper.deleteById(teacher.getUserId());
        teacherMapper.deleteById(id);
    }
}