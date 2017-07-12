package com.pairs.arch.rpc.demo;

import com.pairs.arch.rpc.server.annotation.HrpcServer;
import org.springframework.stereotype.Service;

/**
 * Created by hupeng on 2017/3/28.
 */
@HrpcServer(value = HelloServer.class)
@Service
public class HelloServerImpl implements HelloServer {
    @Override
    public String getName(String name) {
        return name+"call success";
    }

    @Override
    public SchoolMode descSchool(SchoolMode schoolMode) {

        System.out.println("school name :"+schoolMode.getSchoolName());
        System.out.println("total person :"+schoolMode.getTotal().intValue());
        for(StudentModel studentModel:schoolMode.getStudentModels()){
            System.out.println("--------------------------------");
            System.out.print(String.format("student name is : %s , age is : %s , scord is : %s ",studentModel.getName(),studentModel.getAge().toString(),String.valueOf(studentModel.getScord().floatValue())));
            System.out.println("--------------------------------");
        }
        return new SchoolMode("新学校",null,null);
    }

}
