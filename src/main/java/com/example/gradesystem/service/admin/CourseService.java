package com.example.gradesystem.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.entity.Course;
import com.example.gradesystem.mapper.CourseMapper;
import com.example.gradesystem.util.ExcelUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程管理服务
 * 功能：课程增删查、CSV批量导入
 */
@Service
public class CourseService {

    private final CourseMapper courseMapper;

    public CourseService(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    public IPage<Course> listCourses(int pageNum, int pageSize, String name) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Course::getName, name);
        }
        wrapper.orderByAsc(Course::getId);
        return courseMapper.selectPage(page, wrapper);
    }

    public List<Course> allCourses() {
        return courseMapper.selectList(null);
    }

    public Course getById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return course;
    }

    public Course create(Course course) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getName, course.getName());
        if (courseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "课程「" + course.getName() + "」已存在");
        }
        courseMapper.insert(course);
        return course;
    }

    public Course update(Long id, Course course) {
        getById(id);
        course.setId(id);
        courseMapper.updateById(course);
        return course;
    }

    public void delete(Long id) {
        getById(id);
        courseMapper.deleteById(id);
    }

    /**
     * CSV批量导入课程
     * CSV格式：name,credit,teacherId,semesterId
     */
    @Transactional
    public int importCourses(MultipartFile file) {
        int count = 0;
        try (InputStream is = file.getInputStream()) {
            List<String[]> rows = ExcelUtil.readExcel(is);
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 4) continue;
                String name = row[0].trim();
                BigDecimal credit = new BigDecimal(row[1].trim());
                Long teacherId = Long.parseLong(row[2].trim());
                Long semesterId = Long.parseLong(row[3].trim());

                // 检查重复
                LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Course::getName, name);
                if (courseMapper.selectCount(wrapper) > 0) continue;

                Course course = new Course();
                course.setName(name);
                course.setCredit(credit);
                course.setTeacherId(teacherId);
                course.setSemesterId(semesterId);
                courseMapper.insert(course);
                count++;
            }
        } catch (Exception e) {
            throw new BusinessException(400, "导入失败: " + e.getMessage());
        }
        return count;
    }
}
