package com.example.gradesystem.controller.student;

import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.service.student.StudentGradeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 学生端控制器 — 成绩查询、成绩分析、通知查看
 * 接口前缀：/api/student/
 */
@RestController
@RequestMapping("/api/student")
public class StudentGradeController {

    @Autowired
    private StudentGradeService studentGradeService;

    /**
     * 获取当前登录学生信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getStudentInfo(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        String name = studentGradeService.getStudentNameByUserId(user.getId());
        return Result.success(Map.of(
            "userId", user.getId(),
            "username", user.getUsername(),
            "name", name
        ));
    }

    /**
     * 查询成绩列表
     */
    @GetMapping("/grades")
    public Result<List<Map<String, Object>>> queryGrades(
            @RequestParam(required = false) Long semesterId,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        List<Map<String, Object>> grades = studentGradeService.queryGrades(user.getId(), semesterId);
        return Result.success(grades);
    }

    /**
     * 获取可选学期列表
     */
    @GetMapping("/semesters")
    public Result<List<Map<String, Object>>> getSemesters(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return Result.success(studentGradeService.getAvailableSemesters(user.getId()));
    }

    /**
     * 计算GPA
     */
    @GetMapping("/gpa")
    public Result<BigDecimal> getGpa(
            @RequestParam(required = false) Long semesterId,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return Result.success(studentGradeService.calculateGPA(user.getId(), semesterId));
    }

    /**
     * 获取成绩分析数据
     */
    @GetMapping("/analysis")
    public Result<Map<String, Object>> getAnalysis(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return Result.success(studentGradeService.getAnalysisData(user.getId()));
    }

    /**
     * 获取通知公告
     */
    @GetMapping("/notices")
    public Result<List<Map<String, Object>>> getNotices(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return Result.success(studentGradeService.getNotices());
    }
}
