package com.pairs.arch.rpc.demo;

import java.math.BigDecimal;

/**
 * Created by hupeng on 2017/3/31.
 */
public class StudentModel {

    private Integer age;
    private String name ;
    private BigDecimal scord;

    public StudentModel(Integer age, String name, BigDecimal scord) {
        this.age = age;
        this.name = name;
        this.scord = scord;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getScord() {
        return scord;
    }

    public void setScord(BigDecimal scord) {
        this.scord = scord;
    }
}
