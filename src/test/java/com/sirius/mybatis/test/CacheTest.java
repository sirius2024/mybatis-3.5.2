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

import com.sirius.mybatis.entity.User;
import com.sirius.mybatis.utils.MybtaisSessionFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一级缓存与二级缓存的区别
 * 1.作用范围
 * 一级缓存的作用范围是 SqlSession 的生命周期，二级缓存的作用范围是整个应用程序的生命周期。
 *
 * 2.实现方式
 * 一级缓存是通过 SqlSession 内置的一个 HashMap 来实现的，而二级缓存则是通过配置 Cache 接口来实现的。
 *
 * 3.共享机制
 * 一级缓存只能在同一个 SqlSession 内部共享，二级缓存可以在多个 SqlSession 之间共享。
 *
 * 4.缓存规则
 * 一级缓存默认开启并且无法关闭；二级缓存需要手动开启并进行配置。
 *
 * MyBatis缓存原理
 * MyBatis 的缓存本质上是一个 HashMap，它的键是查询语句和参数的组合，值是查询结果。当我们执行一个查询时，MyBatis 会先从缓存中查找对应的查询结果，如果缓存中存在，则直接返回缓存的结果；如果缓存中不存在，则从数据库中查询数据，并将查询结果存入缓存中。
 *
 * 每个 SqlSession 都有自己的缓存，因此在不同的 SqlSession 中执行同一个查询语句，它们所使用的缓存是不同的。同时，MyBatis 的缓存是基于引用计数的机制实现的，当查询语句被多次引用时，缓存的引用计数会加 1，只有当引用计数为 0 时，缓存才会被真正的清除。
 */
public class CacheTest {

  /**
   * 缓存的使用场景
   * 缓存的使用场景主要包括以下两种情况：
   *
   * 1.某些数据被经常访问
   * 如果某些数据经常被访问，那么将这些数据缓存起来，可以减少数据库交互次数，提高系统性能。
   *
   * 2.数据存储较大，查询耗时较长
   * 如果某些数据存储较大，且查询这些数据的效率比较低，那么可以将查询结果缓存下来，下次需要查询时，直接从缓存中读取。
   */

  /**
   * 测试一级缓存
   * 在同一个 SqlSession 中，同样的 SQL 查询只会执行一次，并把查询结果缓存到 SqlSession 内置的 HashMap 中。下面是示例代码：
   */
  @Test
  public void firstLevelCacheTest() throws IOException {
    SqlSession sqlSession = MybtaisSessionFactory.getSqlSessionFactory().openSession();
    // 发起第一次查询，查询ID为1000的用户
    User user1 = sqlSession.selectOne("com.sirius.mybatis.mapper.UserMapper.findById", 1000);

    // 更新操作
//    User user = new User();
//    user.setId(1);
//    user.setUsername("tom");
//    sqlSession.update("com.sirius.mybatis.mapper.UserMapper.update",user);
//    sqlSession.commit();

    // 发起第二次查询，查询ID为1的用户
    User user2 = sqlSession.selectOne("com.sirius.mybatis.mapper.UserMapper.findById", 1000);

    System.out.println(user1 == user2);
    System.out.println(user1);
    System.out.println(user2);

    MybtaisSessionFactory.close();
  }


  /**
   * 测试二级缓存
   */
  @Test
  public void secondLevelCacheTest() throws IOException {

    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");

    // 2. (1)解析了配置文件，封装configuration对象 （2）创建了DefaultSqlSessionFactory工厂对象
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);

    // 3.问题：openSession()执行逻辑是什么？
    // 3. (1)创建事务对象 （2）创建了执行器对象cachingExecutor (3)创建了DefaultSqlSession对象
    SqlSession sqlSession1 = sqlSessionFactory.openSession();
    SqlSession sqlSession2 = sqlSessionFactory.openSession();

    // 发起第一次查询，查询ID为1的用户
    User user1 = sqlSession1.selectOne("com.sirius.mybatis.mapper.UserMapper.findById", 1);

    // **必须要调用sqlSession的commit方法或者close方法，才能让二级缓存生效
    sqlSession1.commit();

    // 更新操作
    SqlSession sqlSession3 = sqlSessionFactory.openSession();
    User user = new User();
    user.setId(1);
    user.setUsername("tom");

    sqlSession3.update("com.sirius.mybatis.mapper.UserMapper.updateUser",user);
    sqlSession3.commit();

    // 第二次查询
    User user2 = sqlSession2.selectOne("com.sirius.mybatis.mapper.UserMapper.findById", 1);

    System.out.println(user1==user2);
    System.out.println(user1);
    System.out.println(user2);

    sqlSession1.close();
  }
}
