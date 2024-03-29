/**
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.sirius.mybatis.test;

import cn.hutool.db.meta.Table;
import cn.hutool.db.sql.SqlBuilder;
import com.sirius.mybatis.entity.User;
import com.sirius.mybatis.mapper.UserMapper;
import com.sirius.mybatis.utils.MybtaisSessionFactory;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * https://github.com/mybatis
 */
public class MybatisTest {


  /**
   * 传统方式
   */
  @Test
  public void test1() throws Exception{
    SqlSession sqlSession = MybtaisSessionFactory.getSqlSessionFactory().openSession();
    User user = sqlSession.selectOne("com.sirius.mybatis.mapper.UserMapper.findById", 1000);
    System.out.println(user);
    MybtaisSessionFactory.close();
  }

  @Test
  public void test2() throws IOException {
    SqlSession sqlSession = MybtaisSessionFactory.getSqlSessionFactory().openSession();
    // 4. JDK动态代理生成代理对象
    UserMapper mapperProxy = sqlSession.getMapper(UserMapper.class);

    // 5.代理对象调用方法
    User user = mapperProxy.findById(1000);
    User user2 = mapperProxy.findById(1);

    System.out.println(user);
    System.out.println("MyBatis源码环境搭建成功....");
    MybtaisSessionFactory.close();
  }

  @Test
  public void test4() throws IOException {
    SqlSession sqlSession = MybtaisSessionFactory.getSqlSessionFactory().openSession();
    // 4. JDK动态代理生成代理对象
    UserMapper mapperProxy = sqlSession.getMapper(UserMapper.class);

    // 5.代理对象调用方法
    User user = new User();
    user.setId(1000);
    user.setUsername(String.valueOf(System.currentTimeMillis()));
    mapperProxy.update(user);
    sqlSession.commit();
    MybtaisSessionFactory.close();
  }

  @Test
  public void test3() throws Exception{
    SqlSession sqlSession = MybtaisSessionFactory.getSqlSessionFactory().openSession();
    String sql = SqlBuilder.create().insertPreFragment("insert into user(id, username) values(1, 'admin')").build();
    sqlSession.selectOne("com.sirius.mybatis.mapper.CommonMapper.selectOne", sql);
    sqlSession.commit();

    String sql2 = SqlBuilder.create().select("username").from("user").where("id = 1000").build();
    Object user = sqlSession.selectOne("com.sirius.mybatis.mapper.CommonMapper.selectOne", sql2);
    System.out.println(user);
    MybtaisSessionFactory.close();
  }


  @Test
  public void update(){
    SqlSession sqlSession = MybtaisSessionFactory.getSqlSessionFactory().openSession();
  }
}
