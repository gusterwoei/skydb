package com.guster.skydb.sample.domain;

import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
@Table(name = "lecturers")
public class Lecturer {
    public static final String TABLE_NAME = "lecturers";
    public static final String COL_ID = "_id";
    public static final String COL_FIRST_NAME = "fname";
    public static final String COL_LAST_NAME = "lname";
    public static final String COL_LECTURER_ID = "lecturerId";
    private static final String COL_TYPE = "type";
    private static final String COL_SUBJECT_HEAD = "subjectHead";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    private long _id;
    @Column(name = COL_LECTURER_ID, primaryKey = true, notNull = true)
    private String lecturerId;
    @Column(name = COL_FIRST_NAME, notNull = true)
    private String firstName;
    @Column(name = COL_LAST_NAME, notNull = true)
    private String lastName;
    @Column(name = COL_TYPE, notNull = false, uniqueGroup = false)
    private String type;
    @Column(name = COL_SUBJECT_HEAD, notNull = false, uniqueGroup = false)
    private String supervisor;
    @Column(name = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(name = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;

    public Lecturer() {}

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
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

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }
}
