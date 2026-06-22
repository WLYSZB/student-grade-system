package com.example.gradesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gradesystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，自动拥有 CRUD 方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
