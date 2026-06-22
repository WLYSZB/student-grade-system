package com.example.gradesystem.service.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.entity.Course;
import com.example.gradesystem.entity.Grade;
import com.example.gradesystem.entity.GradeModifyLog;
import com.example.gradesystem.entity.Student;
import com.example.gradesystem.mapper.CourseMapper;
import com.example.gradesystem.mapper.GradeMapper;
import com.example.gradesystem.mapper.GradeModifyLogMapper;
import com.example.gradesystem.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 教师端成绩服务 — 成员3
 * 功能：成绩录入（单条/批量）、成绩修改与日志、成绩统计与分析
 */
@Service
public class GradeService {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private GradeModifyLogMapper gradeModifyLogMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    // ==================== 成绩查询 ====================

    public IPage<Grade> listGrades(Long studentId, Long courseId, int pageNum, int pageSize) {
        Page<Grade> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(Grade::getStudentId, studentId);
        if (courseId != null) wrapper.eq(Grade::getCourseId, courseId);
        wrapper.orderByDesc(Grade::getCreateTime);
        return gradeMapper.selectPage(page, wrapper);
    }

    public Grade getGradeById(Long id) {
        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException(ErrorCode.SCORE_NOT_FOUND);
        }
        return grade;
    }

    // ==================== 成绩录入（单条） ====================

    @Transactional
    public Grade createGrade(Grade grade, Long operatorId) {
        Student student = studentMapper.selectById(grade.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        Course course = courseMapper.selectById(grade.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grade::getStudentId, grade.getStudentId())
               .eq(Grade::getCourseId, grade.getCourseId());
        if (gradeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.SCORE_DUPLICATE);
        }

        if (grade.getScore() != null) {
            if (grade.getScore().compareTo(BigDecimal.ZERO) < 0 ||
                grade.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(ErrorCode.SCORE_OUT_RANGE);
            }
            grade.setGradePoint(calcGradePoint(grade.getScore()));
        }

        grade.setRecorderId(operatorId);
        grade.setRecordTime(LocalDateTime.now());
        grade.setCreateTime(LocalDateTime.now());
        grade.setUpdateTime(LocalDateTime.now());
        gradeMapper.insert(grade);
        return grade;
    }

    // ==================== 成绩录入（批量导入） ====================

    @Transactional
    public List<Grade> batchCreateGrades(List<Grade> gradeList, Long operatorId) {
        List<Grade> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();

        for (int i = 0; i < gradeList.size(); i++) {
            Grade grade = gradeList.get(i);
            try {
                if (grade.getStudentId() == null || studentMapper.selectById(grade.getStudentId()) == null) {
                    errorList.add("第" + (i + 1) + "行：学生不存在");
                    continue;
                }
                if (grade.getCourseId() == null || courseMapper.selectById(grade.getCourseId()) == null) {
                    errorList.add("第" + (i + 1) + "行：课程不存在");
                    continue;
                }
                if (grade.getScore() != null) {
                    if (grade.getScore().compareTo(BigDecimal.ZERO) < 0 ||
                        grade.getScore().compareTo(new BigDecimal("100")) > 0) {
                        errorList.add("第" + (i + 1) + "行：成绩超出范围（0-100）");
                        continue;
                    }
                    grade.setGradePoint(calcGradePoint(grade.getScore()));
                }
                grade.setRecorderId(operatorId);
                grade.setRecordTime(LocalDateTime.now());
                grade.setCreateTime(LocalDateTime.now());
                grade.setUpdateTime(LocalDateTime.now());
                gradeMapper.insert(grade);
                successList.add(grade);
            } catch (Exception e) {
                errorList.add("第" + (i + 1) + "行：" + e.getMessage());
            }
        }

        if (successList.isEmpty() && !errorList.isEmpty()) {
            throw new BusinessException(400, "批量导入失败：\n" + String.join("\n", errorList));
        }

        return successList;
    }

    // ==================== 成绩修改 ====================

    @Transactional
    public Grade updateGrade(Long id, Grade grade, Long operatorId) {
        Grade existingGrade = getGradeById(id);

        GradeModifyLog log = new GradeModifyLog();
        log.setGradeId(id);
        log.setOldScore(existingGrade.getScore());
        log.setNewScore(grade.getScore());
        log.setReason("成绩修改");
        log.setApplicantId(operatorId);
        log.setStatus("APPROVED");
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());
        gradeModifyLogMapper.insert(log);

        existingGrade.setScore(grade.getScore());
        if (grade.getScore() != null) {
            if (grade.getScore().compareTo(BigDecimal.ZERO) < 0 ||
                grade.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(ErrorCode.SCORE_OUT_RANGE);
            }
            existingGrade.setGradePoint(calcGradePoint(grade.getScore()));
        }
        existingGrade.setUpdateTime(LocalDateTime.now());
        gradeMapper.updateById(existingGrade);
        return existingGrade;
    }

    // ==================== 成绩删除 ====================

    @Transactional
    public void deleteGrade(Long id) {
        getGradeById(id);
        gradeMapper.deleteById(id);
    }

    // ==================== 成绩统计 ====================

    public Map<String, Object> getGradeStatistics(Long courseId) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Grade::getCourseId, courseId);
        }

        List<Grade> allGrades = gradeMapper.selectList(wrapper);
        Map<String, Object> result = new HashMap<>();

        int totalCount = allGrades.size();
        int validCount = 0;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.valueOf(-1);
        BigDecimal minScore = BigDecimal.valueOf(101);

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90-100（优秀）", 0);
        distribution.put("80-89（良好）", 0);
        distribution.put("70-79（中等）", 0);
        distribution.put("60-69（及格）", 0);
        distribution.put("0-59（不及格）", 0);

        for (Grade g : allGrades) {
            if (g.getScore() != null) {
                validCount++;
                BigDecimal s = g.getScore();
                sum = sum.add(s);
                if (s.compareTo(maxScore) > 0) maxScore = s;
                if (s.compareTo(minScore) < 0) minScore = s;

                int scoreInt = s.intValue();
                if (scoreInt >= 90) distribution.put("90-100（优秀）", distribution.get("90-100（优秀）") + 1);
                else if (scoreInt >= 80) distribution.put("80-89（良好）", distribution.get("80-89（良好）") + 1);
                else if (scoreInt >= 70) distribution.put("70-79（中等）", distribution.get("70-79（中等）") + 1);
                else if (scoreInt >= 60) distribution.put("60-69（及格）", distribution.get("60-69（及格）") + 1);
                else distribution.put("0-59（不及格）", distribution.get("0-59（不及格）") + 1);
            }
        }

        result.put("totalCount", totalCount);
        result.put("validCount", validCount);
        result.put("average", validCount > 0 ? sum.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP) : "N/A");
        result.put("maxScore", validCount > 0 ? maxScore : "N/A");
        result.put("minScore", validCount > 0 ? minScore : "N/A");
        result.put("distribution", distribution);

        return result;
    }

    // ==================== 成绩排名 ====================

    public List<Map<String, Object>> getGradeRanking(Long courseId) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Grade::getCourseId, courseId);
        }
        wrapper.orderByDesc(Grade::getScore);
        List<Grade> grades = gradeMapper.selectList(wrapper);

        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 0;
        for (Grade g : grades) {
            if (g.getScore() == null) continue;
            rank++;
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank);
            item.put("gradeId", g.getId());
            item.put("studentId", g.getStudentId());
            item.put("courseId", g.getCourseId());
            item.put("score", g.getScore());
            item.put("gradePoint", g.getGradePoint());

            Student student = studentMapper.selectById(g.getStudentId());
            item.put("studentName", student != null ? student.getName() : "未知");

            Course course = courseMapper.selectById(g.getCourseId());
            item.put("courseName", course != null ? course.getName() : "未知");

            ranking.add(item);
        }
        return ranking;
    }

    // ==================== 辅助方法 ====================

    private BigDecimal calcGradePoint(BigDecimal score) {
        if (score == null) return BigDecimal.ZERO;
        double s = score.doubleValue();
        double gp;
        if (s < 60) {
            gp = 0;
        } else if (s < 70) {
            gp = 1.0 + (s - 60) / 10;
        } else if (s < 80) {
            gp = 2.0 + (s - 70) / 10;
        } else if (s < 90) {
            gp = 3.0 + (s - 80) / 10;
        } else {
            gp = 4.0;
        }
        return BigDecimal.valueOf(gp).setScale(2, RoundingMode.HALF_UP);
    }
}
