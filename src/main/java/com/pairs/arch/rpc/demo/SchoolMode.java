package com.pairs.arch.rpc.demo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by hupeng on 2017/3/31.
 */
public class SchoolMode {

    private String  schoolName;
    private BigDecimal total;
    private List<StudentModel> studentModels;

    public SchoolMode(String schoolName, BigDecimal total, List<StudentModel> studentModels) {
        this.schoolName = schoolName;
        this.total = total;
        this.studentModels = studentModels;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<StudentModel> getStudentModels() {
        return studentModels;
    }

    public void setStudentModels(List<StudentModel> studentModels) {
        this.studentModels = studentModels;
    }
}
