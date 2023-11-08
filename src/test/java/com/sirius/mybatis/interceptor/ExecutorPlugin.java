package com.sirius.mybatis.interceptor;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;


@Intercepts(value = {
  @Signature(args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}, method = "query",type = Executor.class),
  @Signature(args = {MappedStatement.class, Object.class}, method = "update",type = Executor.class),
}
)
public class ExecutorPlugin implements Interceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorPlugin.class);


  private String prefix;

  private String suffix;

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    LOGGER.debug("对象实例{}", invocation.getTarget().getClass().getName());
    LOGGER.debug("执行方法{}", invocation.getMethod().getName());
    LOGGER.debug("参数接收{}", Arrays.toString(invocation.getArgs()));
    Object result = invocation.proceed();
    LOGGER.debug("执行结果{}", result);
    return invocation.proceed();
  }

  @Override
  public void setProperties(Properties properties) {
    this.prefix = properties.getProperty("prefix");
    this.suffix = properties.getProperty("suffix");
  }
}
