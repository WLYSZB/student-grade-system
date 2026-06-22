package com.example.gradesystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.dto.StudentRequest;
import com.example.gradesystem.entity.Student;
import com.example.gradesystem.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public Result<IPage<Student>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String major) {
        return Result.success(studentService.listStudents(pageNum, pageSize, className, major));
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.success(studentService.getStudentById(id));
    }

    @PostMapping
    public Result<Student> create(@RequestBody StudentRequest request) {
        return Result.success(studentService.createStudent(request));
    }

    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @RequestBody StudentRequest request) {
        return Result.success(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success(null);
    }

    @PostMapping("/import")
    public Result<Integer> importStudents(@RequestParam("file") MultipartFile file) {
        int count = studentService.importStudents(file);
        return Result.success(count);
    }

    @GetMapping("/classes")
    public Result<List<String>> getClasses() {
        return Result.success(studentService.getAllClasses());
    }
}