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
package org.apache.ibatis.mapping;

/**
 * Represents the content of a mapped statement read from an XML file or an annotation.
 * It creates the SQL that will be passed to the database out of the input parameter received from the user.
 *
 * @author Clinton Begin
 */
public interface SqlSource {

  /**
   * SqlSource 接口只有一个方法，就是获取BoundSql对象，SqlSource接口的设计满足单一职责原则。
   * SqlSource有五个实现类：ProviderSqlSource，DynamicSqlSource，RawSqlSource，StaticSqlSource，StaticSqlSource
   * 其中比较常用的就是DynamicSqlSource，RawSqlSource和StaticSqlSource。如果sql中只包含#{}参数，不包含${}或者其它动态标签，
   * 那么创建SqlSource对象时则会创建RawSqlSource，否则创建DynamicSqlSource对象。
   *
   * @param parameterObject
   * @return
   */
  BoundSql getBoundSql(Object parameterObject);

}
