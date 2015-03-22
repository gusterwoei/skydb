package com.guster.sqlitecreator.sample;

import android.content.Context;
import com.guster.sqlitecreator.sample.dao.LecturerRepository;
import com.guster.sqlitecreator.sample.dao.StudentRepository;
import com.guster.sqlitecreator.sample.dao.SubjectRepository;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class DataContentProvider {
    public static void loadDummyData(Context context) {
        long time = System.currentTimeMillis();

        // Subject
        SubjectRepository subjectRepository = new SubjectRepository(context);
        String[] titles = {"Artificial Intelligence", "Project Management"};
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
        String[] studentFnames = {"Koby", "Main"};
        String[] studentLnames = {"Ryan", "Keith"};
        for(int i=0; i<studentFnames.length; i++) {
            Student student = new Student();
            student.setStudentId("UAMSTU0" + i);
            student.setFacultyId(i);
            student.setFirstName(studentFnames[i]);
            student.setLastName(studentLnames[i]);
            student.setGender("M");
            student.setGpa(2.01 + i);
            student.setCreatedDate(time);
            student.setModifiedDate(time);
            studentRepository.save(student);
        }

        // lecturers
        LecturerRepository lecturerRepository = new LecturerRepository(context);
        String[] lecturerFnames = {"Lim", "Core"};
        String[] lecturerLnames = {"Pricelle", "Simon"};
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
}
