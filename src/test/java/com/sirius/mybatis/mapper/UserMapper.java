package com.sirius.mybatis.mapper;


import com.sirius.mybatis.entity.User;

public interface UserMapper {

  /**
   * 根据条件（id）查询用户
   */
  User findByCondition(int id);
}
