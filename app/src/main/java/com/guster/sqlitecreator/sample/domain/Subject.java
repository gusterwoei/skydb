package com.guster.sqlitecreator.sample.domain;

import com.guster.sqlitecreator.annotation.Column;
import com.guster.sqlitecreator.annotation.Table;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class Subject {
    public static final String TABLE_NAME = "subjects";
    public static final String COL_ID = "_id";
    public static final String COL_SUBJECT_ID = "subjectId";
    public static final String COL_TITLE = "title";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    //@DbField(column = COL_ID, primaryKey = true, autoIncrement = true)
    private long _id;
    @Column(column = COL_SUBJECT_ID, primaryKey = true, notNull = true)
    private String subjectId;
    @Column(column = COL_TITLE, notNull = true)
    private String title;
    @Column(column = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(column = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;
    // extra dummy fields
    @Column(column = "dummy1")
    private String dummy1;
    @Column(column = "dummy2")
    private String dummy2;
    @Column(column = "dummy3")
    private String dummy3;
    @Column(column = "dummy4")
    private String dummy4;
    @Column(column = "dummy5")
    private String dummy5;

    @Table(name = TABLE_NAME)
    public Subject() {}

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
