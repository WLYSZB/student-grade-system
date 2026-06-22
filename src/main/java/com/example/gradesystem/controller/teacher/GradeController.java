package com.example.gradesystem.controller.teacher;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.dto.GradeRequest;
import com.example.gradesystem.entity.Course;
import com.example.gradesystem.entity.Grade;
import com.example.gradesystem.entity.Student;
import com.example.gradesystem.entity.User;
import com.example.gradesystem.mapper.CourseMapper;
import com.example.gradesystem.mapper.StudentMapper;
import com.example.gradesystem.service.teacher.GradeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 教师端成绩管理控制器 — 成员3
 * 路径前缀：/api/teacher/grades
 */
@RestController
@RequestMapping("/api/teacher/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    /** 分页查询成绩 */
    @GetMapping("/list")
    public Result<IPage<Grade>> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return Result.error(401, "未登录");
        return Result.success(gradeService.listGrades(studentId, courseId, pageNum, pageSize));
    }

    /** 获取成绩详情 */
    @GetMapping("/{id}")
    public Result<Grade> getById(@PathVariable Long id) {
        return Result.success(gradeService.getGradeById(id));
    }

    /** 单条录入成绩 */
    @PostMapping
    public Result<Grade> create(@RequestBody Grade grade, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return Result.error(401, "未登录");
        return Result.success(gradeService.createGrade(grade, user.getId()));
    }

    /** 修改成绩（含日志记录） */
    @PutMapping("/{id}")
    public Result<Grade> update(@PathVariable Long id, @RequestBody Grade grade, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return Result.error(401, "未登录");
        return Result.success(gradeService.updateGrade(id, grade, user.getId()));
    }

    /** 删除成绩 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return Result.error(401, "未登录");
        gradeService.deleteGrade(id);
        return Result.success(null);
    }

    /** 批量导入成绩 */
    @PostMapping("/batch")
    public Result<List<Grade>> batchCreate(@RequestBody List<GradeRequest> requests, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return Result.error(401, "未登录");

        List<Grade> gradeList = new ArrayList<>();
        for (GradeRequest req : requests) {
            Grade grade = new Grade();
            grade.setStudentId(req.getStudentId());
            grade.setCourseId(req.getCourseId());
            grade.setScore(req.getScore());
            grade.setGradePoint(req.getGradePoint());
            gradeList.add(grade);
        }

        return Result.success(gradeService.batchCreateGrades(gradeList, user.getId()));
    }

    /** 成绩统计 */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @RequestParam(required = false) Long courseId) {
        return Result.success(gradeService.getGradeStatistics(courseId));
    }

    /** 成绩排名 */
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking(
            @RequestParam(required = false) Long courseId) {
        return Result.success(gradeService.getGradeRanking(courseId));
    }

    /** 获取所有学生（供下拉选择） */
    @GetMapping("/students")
    public Result<List<Student>> allStudents() {
        return Result.success(studentMapper.selectList(null));
    }

    /** 获取所有课程（供下拉选择） */
    @GetMapping("/courses")
    public Result<List<Course>> allCourses() {
        return Result.success(courseMapper.selectList(null));
    }
}
