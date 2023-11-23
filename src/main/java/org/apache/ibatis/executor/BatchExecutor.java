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
package org.apache.ibatis.executor;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Jeff Butler
 * 批处理只对增删改SQL有效（没有select，JDBC批处理不支持select）；
 * 将所有增删改sql都添加到批处理中（addBatch()），等待统一执行（executeBatch()），它缓存了多个Statement对象，每个Statement对象都是addBatch()完毕后，等待逐一执行executeBatch()批处理的；
 *
 * 执行sql需要满足三个条件才能使用同一个Statement(使用同一个Statement是为了压缩体积、减少SQL预处理)
 * 1.sql相同
 * 2.同一个MappedStatement（sql标签的配置都在这里面）
 * 3.执行的顺序必须是连续的
 */
public class BatchExecutor extends BaseExecutor {

  public static final int BATCH_UPDATE_RETURN_VALUE = Integer.MIN_VALUE + 1002;
  /**
   * Statement 数组
   */
  private final List<Statement> statementList = new ArrayList<>();
  /**
   * BatchResult 数组
   *
   * 每一个 BatchResult 元素，对应一个 {@link #statementList} 的 Statement 元素
   */
  private final List<BatchResult> batchResultList = new ArrayList<>();
  /**
   * 当前 SQL
   */
  private String currentSql;
  /**
   * 当前 MappedStatement 对象
   */
  private MappedStatement currentStatement;

  public BatchExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  /**
   * doUpdate代码上if (sql.equals(currentSql) && ms.equals(currentStatement))可以看出上一个添加的是否是这个sql（currentSql）并且是同一个MappedStatement currentStatement（映射语句）;
   * 满足条件就将参数放到当前这个BatchResult对象中的参数。属性是（parameterObject）【batchResult.addParameterObject(parameterObject);】
   * 不满足条件则获取一个Statement实例 再实例化BatchResult对象。最后放到list中去，再给 current和MappedStatement currentStatement赋值
   * 批处理提交必须执行flushStatements才会生效(会将一个个的Statement提交)可以减少与数据库交互次数
   * @param ms
   * @param parameterObject
   * @return
   * @throws SQLException
   */
  @Override
  public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
    final Configuration configuration = ms.getConfiguration();
    // <1> 创建 StatementHandler 对象
    final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
    final BoundSql boundSql = handler.getBoundSql();
    final String sql = boundSql.getSql();
    final Statement stmt;
    // <2> 如果匹配最后一次 currentSql 和 currentStatement ，则聚合到 BatchResult 中
    if (sql.equals(currentSql) && ms.equals(currentStatement)) {
      // <2.1> 获得最后一次的 Statement 对象
      int last = statementList.size() - 1;
      stmt = statementList.get(last);
      // <2.2> 设置事务超时时间
      applyTransactionTimeout(stmt);
      // <2.3> 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
      handler.parameterize(stmt);//fix Issues 322
      // <2.4> 获得最后一次的 BatchResult 对象，并添加参数到其中
      BatchResult batchResult = batchResultList.get(last);
      batchResult.addParameterObject(parameterObject);
      // <3> 如果不匹配最后一次 currentSql 和 currentStatement ，则新建 BatchResult 对象
    } else {
      // <3.1> 获得 Connection
      Connection connection = getConnection(ms.getStatementLog());
      // <3.2> 创建 Statement 或 PrepareStatement 对象
      stmt = handler.prepare(connection, transaction.getTimeout());
      // <3.3> 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
      handler.parameterize(stmt);    //fix Issues 322
      // <3.4> 重新设置 currentSql 和 currentStatement
      currentSql = sql;
      currentStatement = ms;
      // <3.5> 添加 Statement 到 statementList 中
      statementList.add(stmt);
      // <3.6> 创建 BatchResult 对象，并添加到 batchResultList 中
      batchResultList.add(new BatchResult(ms, sql, parameterObject));
    }
    // <4> 批处理
    handler.batch(stmt);
    return BATCH_UPDATE_RETURN_VALUE;
  }

  //doQuery
  //和 SimpleExecutor 的该方法，逻辑差不多。区别在发生查询之前，先调用 #flushStatements() 方法，刷入批处理语句。
  @Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
      throws SQLException {
    Statement stmt = null;
    try {
      // *******************刷入批处理语句
      flushStatements();
      Configuration configuration = ms.getConfiguration();
      // 创建 StatementHandler 对象
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameterObject, rowBounds, resultHandler, boundSql);
      // 获得 Connection 对象
      Connection connection = getConnection(ms.getStatementLog());
      // 创建 Statement 或 PrepareStatement 对象
      stmt = handler.prepare(connection, transaction.getTimeout());
      // 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
      handler.parameterize(stmt);
      // 执行 StatementHandler  ，进行读操作
      return handler.query(stmt, resultHandler);
    } finally {
      // 关闭 StatementHandler 对象
      closeStatement(stmt);
    }
  }

  @Override
  protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
    flushStatements();
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
    Connection connection = getConnection(ms.getStatementLog());
    Statement stmt = handler.prepare(connection, transaction.getTimeout());
    stmt.closeOnCompletion();
    handler.parameterize(stmt);
    return handler.queryCursor(stmt);
  }

  @Override
  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    try {
      List<BatchResult> results = new ArrayList<>();
      if (isRollback) {
        return Collections.emptyList();
      }
      for (int i = 0, n = statementList.size(); i < n; i++) {
        Statement stmt = statementList.get(i);
        applyTransactionTimeout(stmt);
        BatchResult batchResult = batchResultList.get(i);
        try {
          batchResult.setUpdateCounts(stmt.executeBatch());
          MappedStatement ms = batchResult.getMappedStatement();
          List<Object> parameterObjects = batchResult.getParameterObjects();
          KeyGenerator keyGenerator = ms.getKeyGenerator();
          if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
            Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
            jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
          } else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) { //issue #141
            for (Object parameter : parameterObjects) {
              keyGenerator.processAfter(this, ms, stmt, parameter);
            }
          }
          // Close statement to close cursor #1109
          closeStatement(stmt);
        } catch (BatchUpdateException e) {
          StringBuilder message = new StringBuilder();
          message.append(batchResult.getMappedStatement().getId())
              .append(" (batch index #")
              .append(i + 1)
              .append(")")
              .append(" failed.");
          if (i > 0) {
            message.append(" ")
                .append(i)
                .append(" prior sub executor(s) completed successfully, but will be rolled back.");
          }
          throw new BatchExecutorException(message.toString(), e, results, batchResult);
        }
        results.add(batchResult);
      }
      return results;
    } finally {
      for (Statement stmt : statementList) {
        closeStatement(stmt);
      }
      currentSql = null;
      statementList.clear();
      batchResultList.clear();
    }
  }

}
