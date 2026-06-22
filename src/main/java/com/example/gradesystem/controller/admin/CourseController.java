package com.example.gradesystem.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.entity.Course;
import com.example.gradesystem.service.admin.CourseService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 课程管理控制器 - 管理员端
 * 路径前缀：/api/admin/courses
 */
@RestController
@RequestMapping("/api/admin/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** 分页查询 */
    @GetMapping
    public Result<IPage<Course>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(courseService.listCourses(pageNum, pageSize, name));
    }

    /** 获取全部课程（下拉用） */
    @GetMapping("/all")
    public Result<List<Course>> all() {
        return Result.success(courseService.allCourses());
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    /** 新增课程 */
    @PostMapping
    public Result<Course> create(@RequestBody Course course) {
        return Result.success(courseService.create(course));
    }

    /** 修改课程 */
    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course course) {
        return Result.success(courseService.update(id, course));
    }

    /** 删除课程 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success(null);
    }

    /** CSV批量导入课程 */
    @PostMapping("/import")
    public Result<String> importCourses(@RequestParam("file") MultipartFile file) {
        int count = courseService.importCourses(file);
        return Result.success("成功导入 " + count + " 门课程");
    }
}
