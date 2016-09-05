package com.guster.skydb;

import android.database.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 8/11/16.
 */
public class Criteria {
    private List<String> items = new ArrayList<>();

    public Criteria equal(String column, Object value) {
        items.add(column + " = " + getValue(value));
        return this;
    }

    public Criteria notEqual(String column, Object value) {
        items.add(column + " != " + getValue(value));
        return this;
    }

    public Criteria greaterThan(String column, Object value) {
        items.add(column + " > " + getValue(value));
        return this;
    }

    public Criteria greaterEqualThan(String column, Object value) {
        items.add(column + " >= " + getValue(value));
        return this;
    }

    public Criteria lessThan(String column, Object value) {
        items.add(column + " < " + getValue(value));
        return this;
    }

    public Criteria lessEqualThan(String column, Object value) {
        items.add(column + " <= " + getValue(value));
        return this;
    }

    public Criteria is(String column, Object value) {
        items.add(column + " IS " + getValue(value));
        return this;
    }

    public Criteria isNot(String column, Object value) {
        items.add(column + " IS NOT " + getValue(value));
        return this;
    }

    public Criteria like(String column, Object value) {
        items.add(column + " LIKE " + getValue(value));
        return this;
    }

    public Criteria notLike(String column, Object value) {
        items.add(column + " NOT LIKE " + getValue(value));
        return this;
    }

    public Criteria between(String column, Object lowerValue, Object upperValue) {
        items.add(column + " BETWEEN " + getValue(lowerValue) + " AND " + getValue(upperValue));
        return this;
    }

    public Criteria notBetween(String column, Object lowerValue, Object upperValue) {
        items.add(column + " NOT BETWEEN " + getValue(lowerValue) + " AND " + getValue(upperValue));
        return this;
    }

    String build() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<items.size(); i++) {
            String criteria = items.get(i);
            sb.append("(").append(criteria).append(")");

            if(i < (items.size() - 1)) {
                sb.append(" AND ");
            }
        }

        return sb.toString();
    }
    
    private String getValue(Object value) {
        if(value != null) {
            return DatabaseUtils.sqlEscapeString(String.valueOf(value));
        }
        return null;
    }
}
