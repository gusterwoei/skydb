package com.guster.sqlitecreator.sample.dao;

import android.content.Context;

import com.guster.sqlitecreator.Repository;
import com.guster.sqlitecreator.sample.domain.Lecturer;

/**
 * Created by kellylu on 3/17/15.
 */
public class LecturerRepository extends Repository<Lecturer> {

    public LecturerRepository(Context context) {
        super(context, Lecturer.class);
    }
}
