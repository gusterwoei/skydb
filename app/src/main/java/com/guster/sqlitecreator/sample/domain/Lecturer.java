package com.guster.sqlitecreator.sample.domain;

import com.guster.sqlitecreator.annotation.Column;
import com.guster.sqlitecreator.annotation.Table;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class Lecturer {
    public static final String TABLE_NAME = "lecturers";
    public static final String COL_ID = "_id";
    public static final String COL_FIRST_NAME = "fname";
    public static final String COL_LAST_NAME = "lname";
    public static final String COL_LECTURER_ID = "lecturerId";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    //@DbField(column = COL_ID, primaryKey = true, autoIncrement = true)
    private long _id;
    @Column(column = COL_FIRST_NAME, notNull = true)
    private String firstName;
    @Column(column = COL_LAST_NAME, notNull = true)
    private String lastName;
    @Column(column = COL_LECTURER_ID, primaryKey = true, notNull = true)
    private String lecturerId;
    @Column(column = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(column = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;

    @Table(name = TABLE_NAME)
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
}
