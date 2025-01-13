package com.example.smartattendancesystem.HelperClass;

public class User {
    public String id, name, section, number, dob, age, sex, email;
    public boolean verified;

    public User() {}

    public User(String id, String name, String section, String number, String dob, String age, String sex, String email) {
        this.id = id;
        this.name = name;
        this.section = section;
        this.number = number;
        this.dob = dob;
        this.age = age;
        this.sex = sex;
        this.email = email;
    }
        

    public User(String id, String name, String email, String section, String number, String dob, String age, String sex, boolean b) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.section = section;
        this.number = number;
        this.dob = dob;
        this.age = age;
        this.sex = sex;
        this.verified = b;
    }
}