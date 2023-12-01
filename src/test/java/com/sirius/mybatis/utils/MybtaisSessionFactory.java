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
package com.sirius.mybatis.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * 创建连接工厂
 */
public class MybtaisSessionFactory {

  //配置文件路径
  private static final String CONFIG_FILE = "mybatis-config.xml";
  //所有的mybatis操作的连接对象都需要通过SqlSessionFactory接口实例获取
  private static SqlSessionFactory sqlSessionFactory;
  //保存每一个线程的连接对象
  private static final ThreadLocal<SqlSession> SESSION_THREAD_LOCAL = new ThreadLocal<>();

  static {
    //类加载的时候就需要获取工厂实例
    buildSqlSessionFactory();
  }

  /**
   * 构建SqlSession工厂类
   */
  public static SqlSessionFactory buildSqlSessionFactory() {
    try {
      // 1. 读取配置文件
      InputStream inputStream = Resources.getResourceAsStream(CONFIG_FILE);
      // 2. 配置解析
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sqlSessionFactory;
  }

  public static SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }

  /**
   * 获取SqlSession接口对象实例
   */
  public static SqlSession getSession() {
    SqlSession session = SESSION_THREAD_LOCAL.get();
    if (session == null) {
       session = sqlSessionFactory.openSession();
       SESSION_THREAD_LOCAL.set(session);
    }
    return session;
  }

  /**
   * 获取SqlSession接口对象实例
   */
  public static SqlSession getSession(ExecutorType executorType) {
    SqlSession session = SESSION_THREAD_LOCAL.get();
    if (session == null) {
      session = sqlSessionFactory.openSession();
      SESSION_THREAD_LOCAL.set(session);
    }
    return session;
  }

  /**
   * 关闭当前的SqlSession接口对象实例
   */
  public static void close() {
    SqlSession session = SESSION_THREAD_LOCAL.get();
    if (session != null) {
      session.close();
      SESSION_THREAD_LOCAL.remove();
    }
  }
}
