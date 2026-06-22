package com.example.gradesystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.dto.TeacherRequest;
import com.example.gradesystem.entity.Teacher;
import com.example.gradesystem.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public Result<IPage<Teacher>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String department) {
        return Result.success(teacherService.listTeachers(pageNum, pageSize, department));
    }

    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        return Result.success(teacherService.getTeacherById(id));
    }

    @PostMapping
    public Result<Teacher> create(@RequestBody TeacherRequest request) {
        return Result.success(teacherService.createTeacher(request));
    }

    @PutMapping("/{id}")
    public Result<Teacher> update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return Result.success(teacherService.updateTeacher(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return Result.success(null);
    }
}
