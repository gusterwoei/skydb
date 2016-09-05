package com.guster.skydb.sample.dao;

import com.guster.skydb.Repository;
import com.guster.skydb.sample.domain.Subject;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class SubjectRepository extends Repository<Subject> {

    public SubjectRepository() {
        super(Subject.class);
    }

}
