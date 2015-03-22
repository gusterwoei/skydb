package com.guster.sqlitecreator.sample.dao;

import android.content.Context;
import com.guster.sqlitecreator.Repository;
import com.guster.sqlitecreator.sample.MySqliteHelper;
import com.guster.sqlitecreator.sample.domain.Subject;

/**
 * Created by kellylu on 3/17/15.
 */
public class SubjectRepository extends Repository<Subject> {

    public SubjectRepository(Context context) {
        super(context, MySqliteHelper.getInstance(context), Subject.class);
    }
}
