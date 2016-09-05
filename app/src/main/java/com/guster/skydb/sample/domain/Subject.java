package com.guster.skydb.sample.domain;

import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
@Table(name = "subjects")
public class Subject {
    public static final String TABLE_NAME = "subjects";
    public static final String COL_ID = "_id";
    public static final String COL_SUBJECT_ID = "subjectId";
    public static final String COL_TITLE = "title";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    //@DbField(column = COL_ID, primaryKey = true, autoIncrement = true)
    private long _id;
    @Column(name = COL_SUBJECT_ID, primaryKey = true, notNull = true)
    private String subjectId;
    @Column(name = COL_TITLE, notNull = true)
    private String title;
    @Column(name = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(name = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;
    @Column(name = "dummy1")
    private String dummy1;
    @Column(name = "dummy2")
    private String dummy2;
    @Column(name = "dummy3")
    private String dummy3;
    @Column(name = "dummy4")
    private String dummy4;
    @Column(name = "dummy5")
    private String dummy5;

    public Subject() {}

    public Subject(String subjectId, String title) {
        this.subjectId = subjectId;
        this.title = title;

        long time = System.currentTimeMillis();
        this.createdDate = time;
        this.modifiedDate = time;
    }

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
