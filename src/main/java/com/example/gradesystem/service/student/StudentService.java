package com.example.gradesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.dto.StudentRequest;
import com.example.gradesystem.entity.Student;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.mapper.StudentMapper;
import com.example.gradesystem.mapper.UserMapper;
import com.example.gradesystem.util.ExcelUtil;
import com.example.gradesystem.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    public IPage<Student> listStudents(int pageNum, int pageSize, String className, String major) {
        Page<Student> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            wrapper.like(Student::getClassName, className);
        }
        if (major != null && !major.isEmpty()) {
            wrapper.like(Student::getMajor, major);
        }
        wrapper.orderByDesc(Student::getCreateTime);
        return studentMapper.selectPage(page, wrapper);
    }

    public Student getStudentById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return student;
    }

    public Student getStudentByUserId(Long userId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        return studentMapper.selectOne(wrapper);
    }

    @Transactional
    public Student createStudent(StudentRequest request) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(MD5Util.md5(request.getPassword() != null ? request.getPassword() : "123456"));
        user.setRole("STUDENT");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        Student student = new Student();
        student.setUserId(user.getId());
        student.setName(request.getName());
        student.setClassName(request.getClassName());
        student.setMajor(request.getMajor());
        student.setEnrollmentYear(request.getEnrollmentYear());
        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());
        studentMapper.insert(student);

        return student;
    }

    @Transactional
    public Student updateStudent(Long id, StudentRequest request) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userMapper.selectById(student.getUserId());
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
            student.setName(request.getName());
        }
        if (request.getClassName() != null) {
            student.setClassName(request.getClassName());
        }
        if (request.getMajor() != null) {
            student.setMajor(request.getMajor());
        }
        if (request.getEnrollmentYear() != null) {
            student.setEnrollmentYear(request.getEnrollmentYear());
        }
        student.setUpdateTime(LocalDateTime.now());
        studentMapper.updateById(student);

        return student;
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        userMapper.deleteById(student.getUserId());
        studentMapper.deleteById(id);
    }

    @Transactional
    public int importStudents(MultipartFile file) {
        int count = 0;
        try (InputStream is = file.getInputStream()) {
            List<String[]> rows = ExcelUtil.readExcel(is);
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 4) {
                    String username = row[0];
                    String name = row[1];
                    String className = row[2];
                    String major = row[3];
                    Integer enrollmentYear = row.length > 4 ? Integer.parseInt(row[4]) : LocalDateTime.now().getYear();

                    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(User::getUsername, username);
                    if (userMapper.selectCount(wrapper) == 0) {
                        User user = new User();
                        user.setUsername(username);
                        user.setPassword(MD5Util.md5("123456"));
                        user.setRole("STUDENT");
                        user.setStatus(1);
                        user.setCreateTime(LocalDateTime.now());
                        user.setUpdateTime(LocalDateTime.now());
                        userMapper.insert(user);

                        Student student = new Student();
                        student.setUserId(user.getId());
                        student.setName(name);
                        student.setClassName(className);
                        student.setMajor(major);
                        student.setEnrollmentYear(enrollmentYear);
                        student.setCreateTime(LocalDateTime.now());
                        student.setUpdateTime(LocalDateTime.now());
                        studentMapper.insert(student);

                        count++;
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "导入失败: " + e.getMessage());
        }
        return count;
    }

    public List<String> getAllClasses() {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>())
                .stream()
                .map(Student::getClassName)
                .distinct()
                .toList();
    }
}