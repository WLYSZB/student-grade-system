package com.example.gradesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gradesystem.entity.Grade;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {
}
