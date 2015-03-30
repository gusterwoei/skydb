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

}
