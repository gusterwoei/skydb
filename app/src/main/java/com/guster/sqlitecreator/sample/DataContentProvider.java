package com.guster.sqlitecreator.sample;

import android.content.Context;
import android.util.Log;

import com.guster.sqlitecreator.Repository;
import com.guster.sqlitecreator.sample.dao.LecturerRepository;
import com.guster.sqlitecreator.sample.dao.StudentRepository;
import com.guster.sqlitecreator.sample.dao.SubjectRepository;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class DataContentProvider {
    public static void loadDummyData(Context context) {
        long time = System.currentTimeMillis();

        // Subject
        SubjectRepository subjectRepository = new SubjectRepository(context);
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
        StudentRepository studentRepository = new StudentRepository(context);
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
            studentRepository.save(student);
        }

        // lecturers
        LecturerRepository lecturerRepository = new LecturerRepository(context);
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

    public static void testDbInsertPerformance(Context context) {
        // test insert performance with single insert row against multiple insert row
        SubjectRepository subjectRepository = new SubjectRepository(context);
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
}
