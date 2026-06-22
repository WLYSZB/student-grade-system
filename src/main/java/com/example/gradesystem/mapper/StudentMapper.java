package com.example.gradesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gradesystem.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
