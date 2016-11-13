import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.guster.skydb.Criteria;
import com.guster.skydb.Repository;
import com.guster.skydb.sample.DataContentProvider;
import com.guster.skydb.sample.dao.LecturerRepository;
import com.guster.skydb.sample.dao.StudentRepository;
import com.guster.skydb.sample.dao.SubjectRepository;
import com.guster.skydb.sample.domain.Student;
import com.guster.skydb.sample.domain.Subject;
import com.guster.sqlbuilder.SqlBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 8/11/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SkyDbTest {

    StudentRepository studentRepo = new StudentRepository();
    LecturerRepository lecturerRepo = new LecturerRepository();
    SubjectRepository subjectRepo = new SubjectRepository();

    @Before
    public void init() {
        // clear database first
        TestUtil.log("Clearing database for testing...");
        subjectRepo.deleteAll();
        studentRepo.deleteAll();
        lecturerRepo.deleteAll();
        TestUtil.log("Clear completed.");
    }

    @Test
    public void startTesting() {
        int size = 0;

        // load testing data
        loadTestingData();

        // test subject table count
        TestUtil.log("test subject table count");
        assertThat("subjects should > 0", subjectRepo.size(), greaterThan(0));

        // test unique keyword
        TestUtil.log("test unique keyword");
        assertThat("lecturers size should be 2", lecturerRepo.size(), equalTo(6));

        // test save()
        TestUtil.log("test save()");
        size = subjectRepo.size();
        subjectRepo.save(new Subject("SUBJID100", "How to attract hot ladies"));
        assertThat("subjects should be added one", subjectRepo.size() - size, equalTo(1));

        // test saveAll()
        TestUtil.log("test saveAll()");
        size = subjectRepo.size();
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject("SUBJID101", "How to be a sexyman"));
        subjects.add(new Subject("SUBJID102", "How to catch the rare Pokemon"));
        subjectRepo.saveAll(subjects);
        assertEquals("subjects should now have 2 more", 2, subjectRepo.size() - size);

        // test find()
        TestUtil.log("test find()");
        Subject subject = subjectRepo.find("SUBJID100");
        assertNotNull("subject(SUBJID100) should exist", subject);

        // test findByQuery()
        TestUtil.log("test findByQuery()");
        String query = SqlBuilder.newInstance().select("*").from(Subject.TABLE_NAME, "t").getQuery();
        subjects = subjectRepo.findByQuery(query);
        assertThat("should return all subjects", subjects.size(), greaterThan(0));

        // test findByQuery()
        TestUtil.log("test findByQuery()");
        subjects = subjectRepo.findByQuery(query, new Repository.CursorToInstanceListener<Subject>() {
            @Override
            public Subject onEachCursor(Cursor cursor) {
                assertThat("cursor should not be null", cursor, notNullValue());
                return subjectRepo.getInstance(cursor);
            }
        });
        assertThat("should return all subjects", subjects.size(), greaterThan(0));

        // test findBy()
        TestUtil.log("test findBy()");
        subjects = subjectRepo.findBy(Subject.COL_SUBJECT_ID, "SUBJID999");
        assertThat("should return empty", subjects.size(), is(0));
        subjects = subjectRepo.findBy(Subject.COL_SUBJECT_ID, "SUBJID101");
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findBy()
        TestUtil.log("test findBy()");
        String[] columns = {Subject.COL_SUBJECT_ID, Subject.COL_TITLE};
        String[] values = {"SUBJID102", "How to catch the rare Pokemon"};
        subjects = subjectRepo.findBy(columns, values, null);
        assertThat("should not be empty", subjects.size(), greaterThan(0));

        // test findByGroupBy()
        TestUtil.log("test findByGroupBy()");
        subjects = subjectRepo.findByGroupBy(Subject.COL_SUBJECT_ID, "SUBJID100", Subject.COL_SUBJECT_ID);
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findByOrderBy()
        TestUtil.log("test findByOrderBy()");
        subjects = subjectRepo.findByOrderBy(Subject.COL_SUBJECT_ID, "SUBJID100", Subject.COL_TITLE, true);
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findByGroupByOrderBy()
        TestUtil.log("test findByGroupByOrderBy()");
        subjects = subjectRepo.findByGroupByOrderBy(Subject.COL_SUBJECT_ID, "SUBJID100",
                Subject.COL_SUBJECT_ID, Subject.COL_TITLE, false);
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findAll()
        TestUtil.log("test findAll()");
        subjects = subjectRepo.findAll();
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findAllGroupBy()
        TestUtil.log("test findAllGroupBy()");
        subjects = subjectRepo.findAllGroupBy(Subject.COL_SUBJECT_ID);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findAllOrderBy()
        TestUtil.log("test findAllOrderBy()");
        subjects = subjectRepo.findAllOrderBy(Subject.COL_SUBJECT_ID, true);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findAllGroupByOrderBy()
        TestUtil.log("test findAllGroupByOrderBy()");
        subjects = subjectRepo.findAllGroupByOrderBy(Subject.COL_SUBJECT_ID, Subject.COL_TITLE, true);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findByCriteria()
        TestUtil.log("test findByCriteria()");
        subjects = subjectRepo.findAllGroupBy(Subject.COL_SUBJECT_ID);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findByCriteria()
        TestUtil.log("test findByCriteria()");
        Criteria criteria = new Criteria()
                .equal(Subject.COL_SUBJECT_ID, "SUBJID100")
                .equal(Subject.COL_TITLE, "How to attract hot ladies")
                .like(Subject.COL_TITLE, "%HOT LADIES%")
                .lessThan(Subject.COL_MODIFIED_DATE, System.currentTimeMillis())
                .between(Subject.COL_CREATED_DATE, 0, System.currentTimeMillis());
        subjects = subjectRepo.findByCriteria(criteria);
        assertThat("subjects should not be empty", subjects.size(), greaterThan(0));

        // test updateBy()
        TestUtil.log("test updateBy()");
        String newTitle = "How to teleport";
        ContentValues contentValues = new ContentValues();
        contentValues.put(Subject.COL_TITLE, newTitle);
        int rows = subjectRepo.updateBy(Subject.COL_SUBJECT_ID, "SUBJID101", contentValues);
        subject = subjectRepo.findBy(Subject.COL_SUBJECT_ID, "SUBJID101").get(0);
        assertThat("should affect 1 row", rows, greaterThan(0));
        assertThat("should not be null", subject, notNullValue());
        assertThat("should change title", subject.getTitle(), equalTo(newTitle));

        // test updateBy(criteria)
        TestUtil.log("test updateBy(criteria)");
        criteria = new Criteria().equal(Subject.COL_SUBJECT_ID, "SUBJID100");
        newTitle = "How to become so awesome and sexy";
        contentValues.put(Subject.COL_TITLE, newTitle);
        rows = subjectRepo.updateBy(criteria, contentValues);
        subject = subjectRepo.find("SUBJID100");
        assertThat("should affect 1 row", rows, greaterThan(0));
        assertThat("should not be null", subject, notNullValue());
        assertThat("should change title", subject.getTitle(), equalTo(newTitle));

        // test delete()
        TestUtil.log("test delete()");
        subjectRepo.delete(subject);
        subject = subjectRepo.find("SUBJID100");
        assertThat("should be empty", subject, nullValue());

        // test deleteBy()
        TestUtil.log("test deleteBy()");
        subjectRepo.deleteBy(Subject.COL_SUBJECT_ID, "SUBJID101");
        subject = subjectRepo.find("SUBJID101");
        assertThat("should be empty", subject, nullValue());

        // test deleteAll()
        TestUtil.log("test deleteAll()");
        size = lecturerRepo.size();
        assertThat("should > 0", size, greaterThan(0));
        lecturerRepo.deleteAll();
        assertThat("should be 0", lecturerRepo.size(), equalTo(0));

        // test retrieve records by boolean value
        TestUtil.log("test retrieve records by boolean value");
        query = SqlBuilder.newInstance().select("*").from(Student.TABLE_NAME).where(Student.COL_IS_FOREIGN + " = :val")
                .bindValue("val", true)
                .getQuery();
        size = studentRepo.findByQuery(query).size();
        assertThat("should be 3", size, equalTo(3));

        // test retrieve records by boolean object value
        TestUtil.log("test retrieve records by boolean object value");
        query = SqlBuilder.newInstance().select("*").from(Student.TABLE_NAME).where(Student.COL_IS_ACTIVE + " = :val")
                .bindValue("val", true)
                .getQuery();
        size = studentRepo.findByQuery(query).size();
        assertThat("should be 3", size, equalTo(3));
    }

    @After
    public void done() {
        TestUtil.saveDbToSdCard();
    }

    private void loadTestingData() {
        TestUtil.log("Loading testing data into (Subject, Student, Lecturer)...");
        DataContentProvider.loadDummyData();
        TestUtil.log("Data loaded successfully.");
    }
}
