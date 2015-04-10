package com.guster.skydb.sample.dao;

import android.content.Context;
import com.guster.skydb.Repository;
import com.guster.skydb.sample.domain.Student;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class StudentRepository extends Repository<Student> {

    public StudentRepository(Context context) {
        super(context, Student.class);
    }

    @Override
    public Student save(Student newItem) {
        return super.save(newItem);
    }
}
