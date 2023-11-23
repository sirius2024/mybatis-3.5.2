package com.sirius.mybatis.test;

import com.mysql.jdbc.Driver;

import java.sql.*;

/**
 * 1）SqlSession采用了门面模式方便来让我们使用。他不能跨线程使用，一级缓存生命周期和它一致；
 * 基本功能：增删改查基本的操作功能；
 * 辅助功能：提交、关闭会话；
 * 门面模式：提供一个统一的门面接口API，使得系统更加容易使用。
 * 2）Executor
 * 基本功能：改、查、维护缓存；
 * 辅助功能：提交、关闭执行器、批处理刷新；
 * Executor 主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，它会将数据库相关操作委托给 StatementHandler完成。
 * 3）StatementHandler
 * 经过执行器处理后交给了StatementHandler（声明处理器）；
 * 主要作用就是：参数处理、结果处理；
 * StatementHandler 首先通过 ParameterHandler 完成 SQL 语句的实参绑定，然后通过 java.sql.Statement 对象执行 SQL 语句并得到结果集，
 * 最后通过 ResultSetHandler 完成结果集的映射，得到结果对象并返回。
 *
 *
 Mybatis给我们提供了三种执行器，分别是 :
 SimpleExecutor(简单执行器)、
 ResuseExecutor(可重用执行器)、
 BathExecutor(批处理执行器）
 这三个执行器继承了一个BaseExecutor（基础执行器），而这个基础执行器实现了Executor接口，其中简单执行器是默认的执行器。
 其实还有一种执行器CachingExecutor(二级缓存执行器)你开启二级缓存则会实例化它，在 BaseExecutor 的基础上，实现二级缓存功能。
 （注意: BaseExecutor 的本地缓存，就是一级缓存。）
 */
public class JDBCTest {

  /**
   * JDBC有三种执行器分别是Statement（简单执行器）、PreparedStatement（预处理执行器）、CallableStatement（存储过程执行器）
   * Statement：基本功能：执行静态SQL
   * PreparedStatement：设置预编译，防止SQL注入
   * CallableStatement：设置出参、读取参数（用于执行存储过程）
   */
  public static void main(String[] args) throws Exception {
    // 1、注册驱动
    DriverManager.registerDriver(new Driver());
    // 2、建立连接
    Connection con = DriverManager.getConnection("jdbc:mysql://mysql.sirius.com:33306/test??useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT", "root", "root");
    // 3、编写sql，进行预编译
    String sql = " select * from user;";
    PreparedStatement ps = con.prepareStatement(sql);
    // 4、执行查询，得到结果集
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
      String bname = rs.getString("name");
      System.out.println("====> name=" + bname);
    }
    //5、关闭事务
    rs.close();
    ps.close();
    con.close();
  }
}
