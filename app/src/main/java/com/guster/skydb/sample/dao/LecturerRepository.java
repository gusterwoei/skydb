package com.guster.skydb.sample.dao;

import android.content.Context;

import com.guster.skydb.Repository;
import com.guster.skydb.SqlBuilder;
import com.guster.skydb.sample.domain.Lecturer;

import java.util.List;

/**
 * Created by kellylu on 3/17/15.
 */
public class LecturerRepository extends Repository<Lecturer> {

    public LecturerRepository(Context context) {
        super(context, Lecturer.class);
    }
}
