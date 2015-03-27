package com.guster.sqlitecreator.sample.dao;

import android.content.Context;
import android.database.Cursor;

import com.guster.sqlitecreator.Repository;
import com.guster.sqlitecreator.SqlBuilder;
import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

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
}
