package com.company;

import java.io.Serializable;

public class Person implements Serializable {
    String name;
    String position;
    String positionDescription;
    String doorNumber;
    String phoneNumber;
    String email;
    String section;
    String departmentAbb;
    String departmentName;

    public Person(String name, String position, String positionDescription, String doorNumber, String phoneNumber, String email, String section, String departmentAbb, String departmentName) {
        this.name = name;
        this.position = position;
        this.positionDescription = positionDescription;
        this.doorNumber = doorNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.section = section;
        this.departmentAbb = departmentAbb;
        this.departmentName = departmentName;
    }
}
