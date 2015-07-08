package com.guster.skydb.sample.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.guster.skydb.Repository;
import com.guster.skydb.SqlBuilder;
import com.guster.skydb.sample.domain.Attendance;
import com.guster.skydb.sample.domain.Lecturer;
import com.guster.skydb.sample.domain.Student;
import com.guster.skydb.sample.domain.Subject;

import java.util.List;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class AttendanceRepository extends Repository<Attendance> {

    public AttendanceRepository(Context context) {
        super(context, Attendance.class);
    }

    @Override
    public List<Attendance> findAll() {
        SqlBuilder query = SqlBuilder.newInstance()
                .select("a._id", "sub.subjectId", "sub.title", "l.lecturerId", "l.fname", "l.lname", "stu.studentId", "stu.fname", "stu.lname")
                .addSelect("a.createdDate", "a.modifiedDate")
                .from(Attendance.TABLE_NAME, "a")
                .innerJoin("a", Lecturer.TABLE_NAME, "l", "a.lecturerId = l.lecturerId")
                .innerJoin("a", Student.TABLE_NAME, "stu", "a.studentId = stu.studentId")
                .innerJoin("a", Subject.TABLE_NAME, "sub", "a." + Attendance.COL_SUBJECT_ID + " = sub.subjectId")
                .where("sub.title LIKE :subjectTitle")
                .bindValue("subjectTitle", "artificial intelligence")
                .build();
        return cursorToList(query.getQuery(), new CursorToInstanceListener<Attendance>() {
            @Override
            public Attendance onEachCursor(Cursor cursor) {
                Attendance attendance = new Attendance();
                attendance.set_id(cursor.getLong(0));
                attendance.setSubjectId(cursor.getString(1));
                attendance.setSubjectName(cursor.getString(2));
                attendance.setLecturerId(cursor.getString(3));
                attendance.setLecturerName(cursor.getString(4) + " " + cursor.getString(5));
                attendance.setStudentId(cursor.getString(6));
                attendance.setStudentName(cursor.getString(7) + cursor.getString(8));
                attendance.setCreatedDate(cursor.getLong(9));
                attendance.setModifiedDate(cursor.getLong(10));
                return attendance;
            }
        });
    }

    public List<Attendance> findHalf() {
        SqlBuilder query = SqlBuilder.newInstance()
                .select("*")
                .from(Attendance.TABLE_NAME, "a")
                .orderBy(Attendance.COL_STUDENT_ID)
                .limit(1, size() / 2)
                .build();
        return cursorToList(query.getQuery());
    }
}
