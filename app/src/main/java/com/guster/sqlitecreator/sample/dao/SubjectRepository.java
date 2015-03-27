package com.guster.sqlitecreator.sample.dao;

import android.content.Context;
import android.util.Log;

import com.guster.sqlitecreator.Repository;
import com.guster.sqlitecreator.SqlBuilder;
import com.guster.sqlitecreator.sample.domain.Subject;

import java.util.List;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class SubjectRepository extends Repository<Subject> {

    public SubjectRepository(Context context) {
        super(context, Subject.class);
    }

    public void saveAll(List<Subject> subjects) {
        int maxRowsPerInsert = 25;
        int size = subjects.size();
        int numOfInserts = (size / maxRowsPerInsert);
        int remainInserts = (size % maxRowsPerInsert);
        int count = 0;

        // threshold inserts
        for(int k=0; k<numOfInserts; k++) {
            // one insert statement
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<maxRowsPerInsert; i++) {
                Subject subject = subjects.get(count);
                sb.append(SqlBuilder.getInsertStmnt(
                        subject.getTitle(), subject.getSubjectId(), "abc", "abc", "abc", "abc", "abc",
                        subject.getCreatedDate(), subject.getModifiedDate()));

                if(i < maxRowsPerInsert- 1) {
                    sb.append(", ");
                }
                count++;
            }
            String sql = "INSERT OR REPLACE INTO " + Subject.TABLE_NAME + " VALUES " + sb.toString();
            runQuery(sql);
        }

        // remaining inserts
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<remainInserts; i++) {
            Subject subject = subjects.get(count);
            sb.append(SqlBuilder.getInsertStmnt(
                    subject.getTitle(), subject.getSubjectId(), "abc", "abc", "abc", "abc", "abc",
                    subject.getCreatedDate(), subject.getModifiedDate()));

            if(i < remainInserts - 1) {
                sb.append(", ");
            }
            count++;
        }
        if(remainInserts > 0) {
            String sql = "INSERT OR REPLACE INTO " + Subject.TABLE_NAME + " VALUES " + sb.toString();
            runQuery(sql);
        }
    }
}
