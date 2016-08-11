package com.guster.skydb.sample;

import android.content.Context;
import android.util.Log;

import com.guster.skydb.Criteria;
import com.guster.skydb.sample.dao.AttendanceRepository;
import com.guster.skydb.sample.dao.LecturerRepository;
import com.guster.skydb.sample.dao.StudentRepository;
import com.guster.skydb.sample.dao.SubjectRepository;
import com.guster.skydb.sample.domain.Attendance;
import com.guster.skydb.sample.domain.Lecturer;
import com.guster.skydb.sample.domain.Student;
import com.guster.skydb.sample.domain.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class DataContentProvider {
    public static void loadDummyData() {
        long time = System.currentTimeMillis();

        // Subject
        SubjectRepository subjectRepository = new SubjectRepository();
        String[] titles = {"Artificial Intelligence", "Project Management",
                "Art of Communication", "Theory of Computation", "Foundation Engineering", "Political Mind"};
        for(int i=0; i<titles.length; i++) {
            Subject subject = new Subject();
            subject.setTitle(titles[i]);
            subject.setSubjectId("SUBJID0" + i);
            subject.setCreatedDate(time);
            subject.setModifiedDate(time);
            subjectRepository.save(subject);
        }

        // students
        StudentRepository studentRepository = new StudentRepository();
        String[] studentFnames = {"Koby", "Main", "Eric", "Charlie", "Brett", "Jimmy"};
        String[] studentLnames = {"Ryan", "Keith", "Claptin", "Factory", "Jackson", "Light"};
        for(int i=0; i<studentFnames.length; i++) {
            Student student = new Student();
            student.setStudentId("UAMSTU0" + i);
            student.setFacultyId(i);
            student.setFirstName(studentFnames[i]);
            student.setLastName(studentLnames[i]);
            student.setGender("M");
            student.setGpa(2.01 + 0.1);
            student.setCreatedDate(time);
            student.setModifiedDate(time);
            student.setIsActive(i % 2 == 0);
            studentRepository.save(student);
        }

        // lecturers
        LecturerRepository lecturerRepository = new LecturerRepository();
        String[] lecturerFnames = {"Lim", "Core", "Tora", "Shanks", "Mike", "Navi Shan'h"};
        String[] lecturerLnames = {"Pricelle", "Simon", "Yamato", "Yes", "Chang", "Carl"};
        for(int i=0; i<lecturerFnames.length; i++) {
            Lecturer lecturer = new Lecturer();
            lecturer.setLecturerId("LEC0" + i);
            lecturer.setFirstName(lecturerFnames[i]);
            lecturer.setLastName(lecturerLnames[i]);
            lecturer.setCreatedDate(time);
            lecturer.setModifiedDate(time);
            lecturerRepository.save(lecturer);
        }
    }

    public static void testDbInsertPerformance() {
        // test insert performance with single insert row against multiple insert row
        SubjectRepository subjectRepository = new SubjectRepository();
        int maxItems = 20000;
        long time = System.currentTimeMillis();
        List<Subject> subjects = new ArrayList<>();
        Log.d("ABC", "loading " + maxItems + " subjects...");
        for(int i=0; i<maxItems; i++) {
            Subject subject = new Subject();
            subject.setTitle("Subject title " + (i+1));
            subject.setSubjectId("SUBJID0" + (i+1));
            subject.setCreatedDate(time);
            subject.setModifiedDate(time);
            subjects.add(subject);
        }
        Log.d("ABC", "loading complete\n");

        // test single
        /*Log.d("ABC", "executing single-row insert...");
        long singleCurrentTime1 = System.currentTimeMillis();
        Repository.beginTransaction();
        for(int i=0; i<subjects.size(); i++) {
            Subject subject = subjects.get(i);
            subjectRepository.save(subject);
        }
        Repository.endTransaction();
        long singleCurrentTime2 = System.currentTimeMillis();
        Log.d("ABC", "SINGLE-ROW INSERT TIME - " + (singleCurrentTime2 - singleCurrentTime1));*/

        // test multiple
        Log.d("ABC", "executing multiple-row insert...");
        long multipleCurrentTime1 = System.currentTimeMillis();
        subjectRepository.saveAll(subjects);
        long multipleCurrentTime2 = System.currentTimeMillis();
        Log.d("ABC", "MULTIPLE-ROW INSERT TIME - " + (multipleCurrentTime2 - multipleCurrentTime1));
    }

    public static void testQueryBuilder(Context context) {
        AttendanceRepository attendanceRepository = new AttendanceRepository(context);
        attendanceRepository.findBy(Attendance.COL_STUDENT_ID, "okman");
        attendanceRepository.findBy(
                new String[]{Attendance.COL_STUDENT_ID, Attendance.COL_LECTURER_ID},
                new String[]{"stu0001", "lec0001"}, null);
        attendanceRepository.findByOrderBy(Attendance.COL_STUDENT_ID, "stu0001", Attendance.COL_LECTURER_ID, true);
        attendanceRepository.findAllOrderBy(Attendance.COL_LECTURER_ID, true);
        attendanceRepository.findByGroupBy(Attendance.COL_STUDENT_ID, "stu0001", Attendance.COL_STUDENT_ID);
        attendanceRepository.findAllGroupBy(Attendance.COL_STUDENT_ID);
        attendanceRepository.findAllGroupByOrderBy(Attendance.COL_LECTURER_ID, Attendance.COL_CREATED_DATE, true);
        attendanceRepository.findByGroupByOrderBy(Attendance.COL_STUDENT_ID, "stu0001", Attendance.COL_LECTURER_ID, Attendance.COL_CREATED_DATE, true);
        attendanceRepository.findAll();

        Attendance at = new Attendance();
        at.setStudentId("stu0001");
        at.setLecturerId("lec0001");

        Criteria criteria = new Criteria()
                .equal(Attendance.COL_STUDENT_ID, "stu0001")
                .equal(Attendance.COL_LECTURER_ID, "lec0001");
        attendanceRepository.findByCriteria(criteria);
        //attendanceRepository.findUnique(at);
    }
}
