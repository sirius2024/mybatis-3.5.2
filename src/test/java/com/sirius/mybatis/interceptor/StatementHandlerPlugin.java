package com.sirius.mybatis.interceptor;


import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Field;


@Intercepts(value = {
  @Signature(args = {java.sql.Connection.class, Integer.class}, method = "prepare",type = StatementHandler.class)
})
public class StatementHandlerPlugin implements Interceptor {


  @Override
  public Object intercept(Invocation invocation) throws Throwable {
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
}
