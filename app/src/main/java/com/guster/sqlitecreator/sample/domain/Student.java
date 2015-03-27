package com.guster.sqlitecreator.sample.domain;

import com.guster.sqlitecreator.annotation.Column;
import com.guster.sqlitecreator.annotation.Table;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class Student {
    public static final String TABLE_NAME = "students";
    public static final String COL_ID = "_id";
    public static final String COL_STUDENT_ID = "studentId";
    public static final String COL_FIRST_NAME = "fname";
    public static final String COL_LAST_NAME = "lname";
    public static final String COL_GENDER = "gender";
    public static final String COL_DOB = "dob";
    public static final String COL_FACULTY_ID = "facultyId";
    public static final String COL_GPA = "gpa";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    private long _id;
    @Column(column = COL_STUDENT_ID, primaryKey = true, notNull = true)
    private String studentId;
    @Column(column = COL_FIRST_NAME, notNull = true)
    private String firstName;
    @Column(column = COL_LAST_NAME, notNull = true)
    private String lastName;
    @Column(column = COL_GENDER, notNull = true)
    private String gender;
    @Column(column = COL_DOB)
    private String dob;
    @Column(column = COL_FACULTY_ID, notNull = true)
    private int facultyId;
    @Column(column = COL_GPA, notNull = true)
    private double gpa;
    @Column(column = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(column = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;

    @Table(name = TABLE_NAME)
    public Student() {}

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
