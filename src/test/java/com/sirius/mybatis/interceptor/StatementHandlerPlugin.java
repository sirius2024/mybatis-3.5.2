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


import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.util.Properties;


@Intercepts(value = {
  @Signature(args = {java.sql.Connection.class, Integer.class}, method = "prepare",type = StatementHandler.class)
})
public class StatementHandlerPlugin implements Interceptor {


  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object sh = invocation.getTarget();
    MetaObject mh = SystemMetaObject.forObject(sh);
    MappedStatement ms = (MappedStatement) mh.getValue("delegate.mappedStatement");
    String msid = ms.getId();
    //判断方法是否以ByPage为后缀
//    if (msid.endsWith("ByPage")) {
//      mh.getValue("delegate.parameterHandler");
//      String sql =
//        //在原来的SQL语句上拼接上分页条件语句
//        mh.getValue("delegate.boundSql.sql").toString().trim() + String.format(" limit % d, % d ", currpage * pagesize - pagesize, pagesize);
//      mh.setValue("delegate.boundSql.sql", sql);
//    }
//    return invocation.proceed();

    System.out.println(invocation.getArgs()[0]);
    if(invocation.getTarget() instanceof RoutingStatementHandler){
      RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();
      Field field = handler.getClass().getDeclaredField("delegate");
      field.setAccessible(true);
      PreparedStatementHandler delegate = (PreparedStatementHandler) field.get(handler);
      BoundSql boundSql = delegate.getBoundSql();
      System.out.println("SQL命令:" + boundSql.getSql());
      System.out.println("参数映射:" + boundSql.getParameterMappings());
      System.out.println("参数内容:" + boundSql.getParameterObject());
    }
    return invocation.proceed();
  }

  @Override
  public Object plugin(Object target) {
//固定的写法，第二个参数表示要代理的对象 this
    return Plugin.wrap(target, this);
  }

  /**
   * 要传给这个插件的配置信息
   *
   * @param p
   */
  @Override
  public void setProperties(Properties p) {
    pagesize = Integer.parseInt(p.getOrDefault("pagesize", 15).toString());
    currpage = Integer.parseInt(p.getOrDefault("currpage", 1).toString());
  }

  public static void startPage(int c, int p) {
    currpage = c;
    pagesize = p;

  }

  public static void startPage(int c) {
    currpage = c;
  }

  public static int currpage = 1;
  public static int pagesize = 10;
  public static int recordcount = 0;
  public static int pagecount = 1;
}
