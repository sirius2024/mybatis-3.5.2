package com.sirius.mybatis.entity;

import java.io.Serializable;


public class Member implements Serializable {

  private String mid;
  private String name;
  private Integer age;
  private String sex;

  public String getMid() {
    return mid;
  }

  public void setMid(String mid) {
    this.mid = mid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getSex() {
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }
}
