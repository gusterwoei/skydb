import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.guster.skydb.Criteria;
import com.guster.skydb.Repository;
import com.guster.skydb.sample.DataContentProvider;
import com.guster.skydb.sample.dao.LecturerRepository;
import com.guster.skydb.sample.dao.StudentRepository;
import com.guster.skydb.sample.dao.SubjectRepository;
import com.guster.skydb.sample.domain.Lecturer;
import com.guster.skydb.sample.domain.Student;
import com.guster.skydb.sample.domain.Subject;
import com.guster.sqlbuilder.SqlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 8/11/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SkyDbTest {

    private void log(String log) {
        Log.d("SKYDB", log);
    }

    public void saveDbToSdCard() {
        String dbName = "skydb.db";
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/com.guster.skydb.sample" + "/databases/" + dbName;
                String backupDBPath = dbName;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                log("file: " + backupDB.getAbsolutePath());

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            log("saving db error: " + e.getMessage());
        }
    }

    @Before
    public void init() {
        // clear database first
        log("Clearing database for testing...");
        Repository.get(Subject.class).deleteAll();
        Repository.get(Student.class).deleteAll();
        Repository.get(Lecturer.class).deleteAll();
        log("Clear completed.");
    }

    @Test
    public void startTesting() {
        // load testing data
        log("Loading testing data into (Subject, Student, Lecturer)...");
        DataContentProvider.loadDummyData();
        log("Data loaded successfully.");

        StudentRepository studentRepository = new StudentRepository();
        final SubjectRepository subjectRepository = new SubjectRepository();
        LecturerRepository lecturerRepository = new LecturerRepository();

        // check subject table count
        assertThat("subjects should > 0", subjectRepository.size(), greaterThan(0));

        // test save
        int size = subjectRepository.size();
        subjectRepository.save(new Subject("SUBJID100", "How to attract hot ladies"));
        assertThat("subjects should be added one", subjectRepository.size() - size, equalTo(1));

        // test saveAll
        size = subjectRepository.size();
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject("SUBJID101", "How to be a sexyman"));
        subjects.add(new Subject("SUBJID102", "How to catch the rare Pokemon"));
        subjectRepository.saveAll(subjects);
        assertEquals("subjects should now have 2 more", 2, subjectRepository.size() - size);

        // test find
        Subject subject = subjectRepository.find("SUBJID100");
        assertNotNull("subject(SUBJID100) should exist", subject);

        // test findByQuery
        String query = SqlBuilder.newInstance().select("*").from(Subject.TABLE_NAME, "t").getQuery();
        subjects = subjectRepository.findByQuery(query);
        assertThat("should return all subjects", subjects.size(), greaterThan(0));

        // test findByQuery
        subjects = subjectRepository.findByQuery(query, new Repository.CursorToInstanceListener<Subject>() {
            @Override
            public Subject onEachCursor(Cursor cursor) {
                assertThat("cursor should not be null", cursor, notNullValue());
                return subjectRepository.getInstance(cursor);
            }
        });
        assertThat("should return all subjects", subjects.size(), greaterThan(0));

        // test findBy
        subjects = subjectRepository.findBy(Subject.COL_SUBJECT_ID, "SUBJID999");
        assertThat("should return empty", subjects.size(), is(0));
        subjects = subjectRepository.findBy(Subject.COL_SUBJECT_ID, "SUBJID101");
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findBy
        String[] columns = {Subject.COL_SUBJECT_ID, Subject.COL_TITLE};
        String[] values = {"SUBJID102", "How to catch the rare Pokemon"};
        subjects = subjectRepository.findBy(columns, values, null);
        assertThat("should not be empty", subjects.size(), greaterThan(0));

        // test findByGroupBy
        subjects = subjectRepository.findByGroupBy(Subject.COL_SUBJECT_ID, "SUBJID100", Subject.COL_SUBJECT_ID);
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findByOrderBy
        subjects = subjectRepository.findByOrderBy(Subject.COL_SUBJECT_ID, "SUBJID100", Subject.COL_TITLE, true);
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findByGroupByOrderBy
        subjects = subjectRepository.findByGroupByOrderBy(Subject.COL_SUBJECT_ID, "SUBJID100",
                Subject.COL_SUBJECT_ID, Subject.COL_TITLE, false);
        assertThat("should not return empty", subjects.size(), greaterThan(0));

        // test findAll
        subjects = subjectRepository.findAll();
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findAllGroupBy
        subjects = subjectRepository.findAllGroupBy(Subject.COL_SUBJECT_ID);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findAllOrderBy
        subjects = subjectRepository.findAllOrderBy(Subject.COL_SUBJECT_ID, true);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findAllGroupByOrderBy
        subjects = subjectRepository.findAllGroupByOrderBy(Subject.COL_SUBJECT_ID, Subject.COL_TITLE, true);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findByCriteria
        subjects = subjectRepository.findAllGroupBy(Subject.COL_SUBJECT_ID);
        assertThat("should be max rows", subjects.size(), greaterThan(3));

        // test findByCriteria
        Criteria criteria = new Criteria()
                .equal(Subject.COL_SUBJECT_ID, "SUBJID100")
                .equal(Subject.COL_TITLE, "How to attract hot ladies")
                .like(Subject.COL_TITLE, "%HOT LADIES%")
                .lessThan(Subject.COL_MODIFIED_DATE, System.currentTimeMillis())
                .between(Subject.COL_CREATED_DATE, 0, System.currentTimeMillis());
        subjects = subjectRepository.findByCriteria(criteria);
        assertThat("subjects should not be empty", subjects.size(), greaterThan(0));

        // test updateBy
        String newTitle = "How to teleport";
        ContentValues contentValues = new ContentValues();
        contentValues.put(Subject.COL_TITLE, newTitle);
        int rows = subjectRepository.updateBy(Subject.COL_SUBJECT_ID, "SUBJID101", contentValues);
        subject = subjectRepository.findBy(Subject.COL_SUBJECT_ID, "SUBJID101").get(0);
        assertThat("should affect 1 row", rows, greaterThan(0));
        assertThat("should not be null", subject, notNullValue());
        assertThat("should change title", subject.getTitle(), equalTo(newTitle));

        // test updateBy (criteria)
        criteria = new Criteria().equal(Subject.COL_SUBJECT_ID, "SUBJID100");
        newTitle = "How to become so awesome and sexy";
        contentValues.put(Subject.COL_TITLE, newTitle);
        rows = subjectRepository.updateBy(criteria, contentValues);
        subject = subjectRepository.find("SUBJID100");
        assertThat("should affect 1 row", rows, greaterThan(0));
        assertThat("should not be null", subject, notNullValue());
        assertThat("should change title", subject.getTitle(), equalTo(newTitle));

        // test runQuery
        // test delete
        subjectRepository.delete(subject);
        subject = subjectRepository.find("SUBJID100");
        assertThat("should be empty", subject, nullValue());

        // test deleteBy
        subjectRepository.deleteBy(Subject.COL_SUBJECT_ID, "SUBJID101");
        subject = subjectRepository.find("SUBJID101");
        assertThat("should be empty", subject, nullValue());

        // test deleteAll
        size = lecturerRepository.size();
        assertThat("should > 0", size, greaterThan(0));
        lecturerRepository.deleteAll();
        assertThat("should be 0", lecturerRepository.size(), equalTo(0));
    }
}
