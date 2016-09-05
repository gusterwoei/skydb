package com.guster.skydb.sample.dao;

import com.guster.skydb.Repository;

/**
 * Created by Gusterwoei on 8/11/16.
 */
public abstract class BaseRepository<T> extends Repository<T> {

    public BaseRepository(Class<T> type) {
        super(type);
    }
}
