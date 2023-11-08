package com.sirius.mybatis.entity;

import java.io.Serializable;

/**
 * @author 604138
 * @date 2023/11/8
 */
public class Employee extends Member implements Serializable {


  private Double salary;

  private String dept;

  public Double getSalary() {
    return salary;
  }

  public void setSalary(Double salary) {
    this.salary = salary;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }
}
