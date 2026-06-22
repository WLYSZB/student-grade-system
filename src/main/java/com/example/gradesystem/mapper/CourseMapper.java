package com.example.gradesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gradesystem.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
