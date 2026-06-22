package com.example.gradesystem.service.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.entity.*;
import com.example.gradesystem.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生端业务逻辑 — 成绩查询、成绩分析（含GPA计算）、通知查看
 */
@Service
public class StudentGradeService {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 根据系统用户ID获取对应的学生ID
     */
    public Long getStudentIdByUserId(Long userId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(wrapper);
        if (student == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        return student.getId();
    }

    /**
     * 根据系统用户ID获取学生姓名
     */
    public String getStudentNameByUserId(Long userId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(wrapper);
        return student != null ? student.getName() : "未知";
    }

    // ==================== 成绩查询 ====================

    /**
     * 查询成绩列表（支持按学期筛选）
     */
    public List<Map<String, Object>> queryGrades(Long userId, Long semesterId) {
        Long studentId = getStudentIdByUserId(userId);

        Map<Long, String> courseMap = getAllCourses();
        Map<Long, String> semesterMap = getAllSemesters();

        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grade::getStudentId, studentId);

        // 如果指定了学期，先查出该学期下的所有课程ID
        if (semesterId != null && semesterId > 0) {
            LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
            courseWrapper.eq(Course::getSemesterId, semesterId);
            List<Course> semesterCourses = courseMapper.selectList(courseWrapper);
            if (!semesterCourses.isEmpty()) {
                Set<Long> courseIds = semesterCourses.stream().map(Course::getId).collect(Collectors.toSet());
                wrapper.in(Grade::getCourseId, courseIds);
            }
        }
        wrapper.orderByDesc(Grade::getRecordTime);

        List<Grade> grades = gradeMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Grade g : grades) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("courseId", g.getCourseId());
            item.put("courseName", courseMap.getOrDefault(g.getCourseId(), "未知课程"));
            item.put("score", g.getScore());
            item.put("gradePoint", g.getGradePoint());
            item.put("recordTime", g.getRecordTime());

            // 查出课程所属学期
            Course course = courseMapper.selectById(g.getCourseId());
            if (course != null) {
                item.put("semesterName", semesterMap.getOrDefault(course.getSemesterId(), "未知学期"));
                item.put("semesterId", course.getSemesterId());
                item.put("credit", course.getCredit());
            } else {
                item.put("semesterName", "未知学期");
                item.put("credit", BigDecimal.ZERO);
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 获取学生可选学期列表（有成绩的学期）
     */
    public List<Map<String, Object>> getAvailableSemesters(Long userId) {
        Long studentId = getStudentIdByUserId(userId);

        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grade::getStudentId, studentId);
        List<Grade> grades = gradeMapper.selectList(wrapper);

        Map<Long, String> semesterMap = getAllSemesters();
        Set<Long> semesterIds = new HashSet<>();

        for (Grade g : grades) {
            Course course = courseMapper.selectById(g.getCourseId());
            if (course != null && course.getSemesterId() != null) {
                semesterIds.add(course.getSemesterId());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long sid : semesterIds) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", sid);
            item.put("name", semesterMap.getOrDefault(sid, "未知学期"));
            result.add(item);
        }
        // 按学期名称排序
        result.sort((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))));
        return result;
    }

    // ==================== GPA 计算 ====================

    /**
     * 计算GPA（加权平均绩点）
     * GPA = Σ(学分 × 绩点) / Σ(学分)
     */
    public BigDecimal calculateGPA(Long userId, Long semesterId) {
        List<Map<String, Object>> grades = queryGrades(userId, semesterId);
        BigDecimal totalWeighted = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Map<String, Object> g : grades) {
            BigDecimal credit = (BigDecimal) g.get("credit");
            BigDecimal gradePoint = (BigDecimal) g.get("gradePoint");
            if (credit != null && gradePoint != null) {
                totalWeighted = totalWeighted.add(credit.multiply(gradePoint));
                totalCredits = totalCredits.add(credit);
            }
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalWeighted.divide(totalCredits, 2, RoundingMode.HALF_UP);
    }

    // ==================== 成绩分析 ====================

    /**
     * 获取成绩分析数据
     */
    public Map<String, Object> getAnalysisData(Long userId) {
        List<Map<String, Object>> allGrades = queryGrades(userId, null);
        Map<String, Object> result = new HashMap<>();

        // 1. 总体统计
        int totalCount = allGrades.size();
        int validCount = 0;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.valueOf(-1);
        BigDecimal minScore = BigDecimal.valueOf(101);
        int passCount = 0;

        for (Map<String, Object> g : allGrades) {
            BigDecimal score = (BigDecimal) g.get("score");
            if (score != null) {
                validCount++;
                sum = sum.add(score);
                if (score.compareTo(maxScore) > 0) maxScore = score;
                if (score.compareTo(minScore) < 0) minScore = score;
                if (score.compareTo(new BigDecimal("60")) >= 0) passCount++;
            }
        }

        BigDecimal average = validCount > 0 ? sum.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal passRate = validCount > 0 ? BigDecimal.valueOf(passCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(validCount), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        result.put("totalCount", totalCount);
        result.put("validCount", validCount);
        result.put("average", average);
        result.put("maxScore", validCount > 0 ? maxScore : BigDecimal.ZERO);
        result.put("minScore", validCount > 0 ? minScore : BigDecimal.ZERO);
        result.put("passRate", passRate);
        result.put("passCount", passCount);
        result.put("failCount", validCount - passCount);
        result.put("gpa", calculateGPA(userId, null));

        // 2. 趋势数据（按学期）
        List<Map<String, Object>> trendData = getTrendData(allGrades);
        result.put("trendData", trendData);

        // 3. 各科对比数据（最新成绩）
        List<Map<String, Object>> subjectData = getSubjectComparison(allGrades);
        result.put("subjectData", subjectData);

        // 4. 分数段分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90-100", 0);
        distribution.put("80-89", 0);
        distribution.put("70-79", 0);
        distribution.put("60-69", 0);
        distribution.put("0-59", 0);

        for (Map<String, Object> g : allGrades) {
            BigDecimal score = (BigDecimal) g.get("score");
            if (score != null) {
                int s = score.intValue();
                if (s >= 90) distribution.put("90-100", distribution.get("90-100") + 1);
                else if (s >= 80) distribution.put("80-89", distribution.get("80-89") + 1);
                else if (s >= 70) distribution.put("70-79", distribution.get("70-79") + 1);
                else if (s >= 60) distribution.put("60-69", distribution.get("60-69") + 1);
                else distribution.put("0-59", distribution.get("0-59") + 1);
            }
        }
        result.put("distribution", distribution);

        return result;
    }

    /**
     * 获取趋势数据（按学期平均分）
     */
    private List<Map<String, Object>> getTrendData(List<Map<String, Object>> allGrades) {
        Map<String, List<BigDecimal>> semesterScores = new LinkedHashMap<>();
        Map<String, String> semesterNames = new LinkedHashMap<>();

        for (Map<String, Object> g : allGrades) {
            String semesterName = (String) g.get("semesterName");
            BigDecimal score = (BigDecimal) g.get("score");
            if (semesterName != null && score != null) {
                semesterScores.computeIfAbsent(semesterName, k -> new ArrayList<>()).add(score);
                semesterNames.put(semesterName, semesterName);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<BigDecimal>> entry : semesterScores.entrySet()) {
            List<BigDecimal> scores = entry.getValue();
            BigDecimal avg = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            Map<String, Object> item = new HashMap<>();
            item.put("semester", entry.getKey());
            item.put("average", avg);
            result.add(item);
        }
        return result;
    }

    /**
     * 获取各科对比数据
     */
    private List<Map<String, Object>> getSubjectComparison(List<Map<String, Object>> allGrades) {
        // 取每门课程的最新成绩
        Map<Long, Map<String, Object>> latestByCourse = new HashMap<>();
        for (Map<String, Object> g : allGrades) {
            Long courseId = (Long) g.get("courseId");
            if (!latestByCourse.containsKey(courseId)) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("courseName", g.get("courseName"));
                entry.put("score", g.get("score"));
                entry.put("credit", g.get("credit"));
                latestByCourse.put(courseId, entry);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(latestByCourse.values());
        result.sort((a, b) -> {
            BigDecimal sa = (BigDecimal) a.get("score");
            BigDecimal sb = (BigDecimal) b.get("score");
            if (sa == null && sb == null) return 0;
            if (sa == null) return 1;
            if (sb == null) return -1;
            return sb.compareTo(sa);
        });
        return result;
    }

    // ==================== 通知查看 ====================

    /**
     * 获取学生可见的通知公告
     */
    public List<Map<String, Object>> getNotices() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, "PUBLISHED");
        wrapper.and(w -> w.eq(Notice::getTargetRole, "ALL").or().eq(Notice::getTargetRole, "STUDENT"));
        wrapper.orderByDesc(Notice::getPublishTime);
        List<Notice> notices = noticeMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Notice n : notices) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", n.getId());
            item.put("title", n.getTitle());
            item.put("content", n.getContent());
            item.put("publishTime", n.getPublishTime());
            result.add(item);
        }
        return result;
    }

    // ==================== 辅助方法 ====================

    private Map<Long, String> getAllCourses() {
        List<Course> courses = courseMapper.selectList(null);
        return courses.stream().collect(Collectors.toMap(Course::getId, Course::getName, (a, b) -> a));
    }

    private Map<Long, String> getAllSemesters() {
        List<Semester> semesters = semesterMapper.selectList(null);
        return semesters.stream().collect(Collectors.toMap(Semester::getId, Semester::getName, (a, b) -> a));
    }
}
