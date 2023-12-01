package com.sirius.mybatis.test.executor;


import cn.hutool.db.sql.SqlBuilder;
import com.sirius.mybatis.entity.User;
import com.sirius.mybatis.utils.MybtaisSessionFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExecutorType_Batch {


  /**
   * 154865
   */
  @Test
  public void batch(){
    SqlSession sqlSession = MybtaisSessionFactory.getSession(ExecutorType.BATCH);
    Long t1 = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
      String sql = String.format("insert into user(id, username) values(%s, 'admin%s')", i, i);
      sqlSession.selectOne("com.sirius.mybatis.mapper.CommonMapper.selectOne", sql);
    }

    sqlSession.commit();
    sqlSession.close();
    System.out.println(System.currentTimeMillis() - t1);
  }

  @Test
  public void simple(){
    SqlSession sqlSession = MybtaisSessionFactory.getSession();
    Long t1 = System.currentTimeMillis();

    List<User> userList = new ArrayList<>();
    for (int i = 1000; i < 2000; i++) {
      User user = new User();
      user.setId(i);
      user.setUsername("admin" + i);
      userList.add(user);
    }
    sqlSession.selectOne("com.sirius.mybatis.mapper.CommonMapper.saveUsers", userList);

    sqlSession.commit();
    sqlSession.close();
    System.out.println(System.currentTimeMillis() - t1);
  }
}
