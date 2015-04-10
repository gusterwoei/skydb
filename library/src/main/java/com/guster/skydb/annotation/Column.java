package com.guster.skydb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Gusterwoei on 12/29/14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    public String name();
    public boolean autoIncrement() default false;
    public boolean primaryKey() default false;
    public boolean unique() default false;
    public boolean notNull() default false;
}
