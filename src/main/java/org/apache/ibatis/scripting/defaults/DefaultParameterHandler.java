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
package org.apache.ibatis.scripting.defaults;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class DefaultParameterHandler implements ParameterHandler {

  /**
   * 管理Mybatis中的全部TypeHandler对象
   */
  private final TypeHandlerRegistry typeHandlerRegistry;
  /**
   * SQL节点
   */
  private final MappedStatement mappedStatement;
  /**
   * 用户传入的实参对象
   */
  private final Object parameterObject;
  private final BoundSql boundSql;
  private final Configuration configuration;

  public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    this.mappedStatement = mappedStatement;
    this.configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
    this.parameterObject = parameterObject;
    this.boundSql = boundSql;
  }

  @Override
  public Object getParameterObject() {
    return parameterObject;
  }

  @Override
  public void setParameters(PreparedStatement ps) {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    // // 取出参数映射列表
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
      // 遍历参数
      //遍历parameterMappings集合中记录的ParameterMapping对象，并根据其中记录的参数名称找到对应的实参，再与SQL绑定
      for (int i = 0; i < parameterMappings.size(); i++) {
        // 1.2. 只有入参类型才会设置PreparedStatement
        ParameterMapping parameterMapping = parameterMappings.get(i);
        // OUT是存储过程中的输出参数，这里需要过滤掉这些参数
        if (parameterMapping.getMode() != ParameterMode.OUT) {
          Object value;
          // 获取参数名称
          String propertyName = parameterMapping.getProperty();
          // 获取对应的实参值
          if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
            value = boundSql.getAdditionalParameter(propertyName);
          } else if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            // 实参可以直接通过TypeHandler转换成jdbcType
            value = parameterObject;
          } else {
            // 1.3. 这一步的工作就是从当前实际传入的参数中获取到指定key（'id'）的value值，比如是'15800000000'
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            value = metaObject.getValue(propertyName);
          }
          // 获取parameterMapping中设置的TypeHandler对象
          // 2. 获取该参数对应的typeHandler
          TypeHandler typeHandler = parameterMapping.getTypeHandler();
          // 2.1. 获取该参数对应的jdbcType
          JdbcType jdbcType = parameterMapping.getJdbcType();
          if (value == null && jdbcType == null) {
            jdbcType = configuration.getJdbcTypeForNull();
          }
          try {
            // 为语句绑定实参
            // 3. 重点是调用每个参数对应的typeHandler的setParameter方法为该ps设置正确的参数值
            typeHandler.setParameter(ps, i + 1, value, jdbcType);
          } catch (TypeException | SQLException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          }
        }
      }
    }
  }
}
