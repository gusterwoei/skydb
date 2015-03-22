package com.guster.sqlitecreator.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by kellylu on 3/15/15.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    public String name();
}
