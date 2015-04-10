package com.guster.sqlitecreator.sample.domain;

import com.guster.sqlitecreator.annotation.Column;
import com.guster.sqlitecreator.annotation.Table;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
@Table(name = "attendances")
public class Attendance {
    public static final String TABLE_NAME = "attendances";
    public static final String COL_ID = "_id";
    public static final String COL_LECTURER_ID = "lecturerId";
    public static final String COL_STUDENT_ID = "studentId";
    public static final String COL_SUBJECT_ID = "courseId";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    @Column(name = COL_ID, primaryKey = true, autoIncrement = true)
    private long _id;
    @Column(name = COL_LECTURER_ID, notNull = true, unique = true)
    private String lecturerId;
    @Column(name = COL_STUDENT_ID, notNull = true, unique = true)
    private String studentId;
    @Column(name = COL_SUBJECT_ID, notNull = true, unique = true)
    private String subjectId;
    @Column(name = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(name = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;

    private String subjectName;
    private String studentName;
    private String lecturerName;

    public Attendance() {}

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }
}
