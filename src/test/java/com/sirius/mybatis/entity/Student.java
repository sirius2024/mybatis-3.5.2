package com.sirius.mybatis.entity;


import java.io.Serializable;

public class Student extends Member implements Serializable {

  private Double score;

  private String major;

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public String getMajor() {
    return major;
  }

  public void setMajor(String major) {
    this.major = major;
  }
}
