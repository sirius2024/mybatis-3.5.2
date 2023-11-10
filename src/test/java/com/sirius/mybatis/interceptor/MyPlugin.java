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
package com.sirius.mybatis.interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.Properties;

@Intercepts({
    @Signature(type = StatementHandler.class,
               method = "prepare",
               args = {Connection.class,Integer.class})
})
public class MyPlugin implements Interceptor {

  /**
   * 拦截方法：每次执行目标方法时，都会进入到intercept方法中
   * @param invocation ：多个参数的封装类
   * @return
   * @throws Throwable
   */
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 增强逻辑:将执行的sql进行记录（打印）
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql.getSql();

    System.out.println("拦截方法，记录Sql：" + sql);

    return invocation.proceed();
  }

  /**
   * 将目标对象生成代理对象，添加到拦截器链中
   * @param target ：目标对象
   * @return
   */
  @Override
  public Object plugin(Object target) {
    // wrap 将目标对象，基于JDK动态代理生成代理对象
    return Plugin.wrap(target,this);
  }

  /**
   * 设置属性
   * @param properties 插件初始化的时候，会设置的一些值的属性集合
   */
  @Override
  public void setProperties(Properties properties) {
    System.out.println("插件配置的初始化参数：" + properties);

  }
}
