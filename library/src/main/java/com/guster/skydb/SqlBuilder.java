/**
 * Copyright 2015 Gusterwoei

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.guster.skydb;

import android.database.DatabaseUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Gusterwoei on 3/6/15.
 *
 * Android SQLite query builder class. Inspired by PHP Doctrine SQL library
 * Support the following clauses:
 *
 * SELECT, FROM, WHERE, INNER JOIN, LEFT OUTER JOIN, GROUP BY, ORDER BY,
 * UPDATE, DELETE
 *
 */
public class SqlBuilder {
    private String clauseDivider = "\n";
    private String query = "";
    private StringBuilder allQuery = new StringBuilder();
    private HashMap<String, Object> bindValues = new HashMap<String, Object>();

    private SqlBuilder() {}

    /**
     * Return a new SqlBuilder instance
     *
     * @return SqlBuilder
     */
    public static SqlBuilder newInstance() {
        return new SqlBuilder();
    }

    /**
     * Divider between each clause, default is a new line (\n)
     *
     * @param clauseDivider - clause divider string
     *
     */
    public void setClauseDivider(String clauseDivider) {
        if(clauseDivider == null || clauseDivider.isEmpty())
            clauseDivider = "\n";
        this.clauseDivider = clauseDivider;
    }

    /**
     * Create a SELECT statement
     *
     * @param args - an array fields to be returned for selection
     * @return SqlBuilder
     *
     */
    public SqlBuilder select(String ... args) {
        allQuery.append("SELECT " + arrayToString(args));
        /*if(selectQuery.length() == 0) {
            selectQuery.append("SELECT ");
        }
        selectQuery.append(arrayToString(args));*/
        return this;
    }

    /**
     * Create a SELECT statement
     *
     * @param args - an array fields to be returned for selection
     * @return SqlBuilder
     *
     */
    public SqlBuilder addSelect(String ... args) {
        if(allQuery.length() == 0) {
            return select(args);
        }
        allQuery.append(", ");
        allQuery.append(arrayToString(args));
        /*if(selectQuery.length() == 0) {
            return select(args);
        }
        selectQuery.append(", ");
        selectQuery.append(arrayToString(args));*/
        return this;
    }

    /**
     * Create a FROM statement
     *
     * @param tableName - table name to query from
     * @param alias - alias for the querying table
     * @return SqlBuilder
     *
     */
    public SqlBuilder from(String tableName, String alias) {
        allQuery.append(clauseDivider);
        allQuery.append("FROM " + tableName + " " + alias);
        /*if(fromQuery.length() == 0) {
            fromQuery.append("FROM ");
        }
        fromQuery.append(tableName + " " + alias);*/
        return this;
    }

    /**
     * Create a WHERE statement
     *
     * @param where - the where conditions
     * @return SqlBuilder
     *
     */
    public SqlBuilder where(String where) {
        allQuery.append(clauseDivider);
        allQuery.append("WHERE " + where);
        /*if(whereQuery.length() == 0) {
            whereQuery.append("WHERE ");
        }
        whereQuery.append(where);*/
        return this;
    }

    /**
     * Create a OR-related WHERE statement
     *
     * @param where - the where conditions
     * @return SqlBuilder
     *
     */
    public SqlBuilder orWhere(String where) {
        allQuery.append(" OR " + where + " ");
        /*if(whereQuery.length() > 0)
            whereQuery.append(" ");
        whereQuery.append("OR " + where);*/
        return this;
    }

    /**
     * Create a AND-related WHERE statement
     *
     * @param where - the where conditions
     * @return SqlBuilder
     *
     */
    public SqlBuilder andWhere(String where) {
        allQuery.append(" AND " + where + " ");
        /*if(whereQuery.length() > 0)
            whereQuery.append(" ");
        whereQuery.append("AND " + where);*/
        return this;
    }

    /**
     * Create an INNER JOIN statement
     *
     * @param fromAlias - OPTIONAL, alias of a from table
     * @param joinTableName - table name to join with
     * @param joinTableAlias - alias of the joining table
     * @param condition - the joining condition
     * @return SqlBuilder
     */
    public SqlBuilder innerJoin(String fromAlias, String joinTableName, String joinTableAlias, String condition) {
        allQuery.append(clauseDivider);
        String q = "INNER JOIN " + joinTableName + " " + joinTableAlias + " ON " + condition;
        allQuery.append(q);
        /*if(innerJoinQuery.length() != 0)
            innerJoinQuery.append(clauseDivider);
        String q = "INNER JOIN " + joinTableName + " " + joinTableAlias + " ON " + condition;
        innerJoinQuery.append(q);*/
        return this;
    }

    /**
     * Create an INNER JOIN statement
     *
     * @param fromAlias - OPTIONAL, alias of a from table
     * @param nestedSql - a nested SQL statement
     * @param joinTableAlias - alias of the joining table
     * @param condition - the joining condition
     * @return SqlBuilder
     */
    public SqlBuilder innerJoin(String fromAlias, SqlBuilder nestedSql, String joinTableAlias, String condition) {
        return innerJoin(fromAlias, "(" + nestedSql.getQuery() + ")", joinTableAlias, condition);
    }

    /**
     * Create a LEFT OUTER JOIN statement
     *
     * @param fromAlias - OPTIONAL, alias of a from table
     * @param joinTableName - a nested SQL statement
     * @param joinTableAlias - alias of the joining table
     * @param condition - the joining condition
     * @return SqlBuilder
     *
     */
    public SqlBuilder leftJoin(String fromAlias, String joinTableName, String joinTableAlias, String condition) {
        allQuery.append(clauseDivider);
        String q = "LEFT OUTER JOIN " + joinTableName + " " + joinTableAlias + " ON " + condition;
        allQuery.append(q);
        /*if(leftJoinQuery.length() != 0)
            leftJoinQuery.append(clauseDivider);
        String q = "LEFT OUTER JOIN " + joinTableName + " " + joinTableAlias + " ON " + condition;
        leftJoinQuery.append(q);*/
        return this;
    }

    /**
     * Create a LEFT OUTER JOIN statement
     *
     * @param fromAlias - OPTIONAL, alias of a from table
     * @param nestedSql - a nested SQL statement
     * @param joinTableAlias - alias of the joining table
     * @param condition - the joining condition
     * @return SqlBuilder
     *
     */
    public SqlBuilder leftJoin(String fromAlias, SqlBuilder nestedSql, String joinTableAlias, String condition) {
        return leftJoin(fromAlias, "(" + nestedSql.getQuery() + ")", joinTableAlias, condition);
    }

    /**
     * Create an ORDER BY statement, sorting in ascending order
     *
     * @param col - column used for result sorting
     * @return SqlBuilder
     *
     */
    public SqlBuilder orderBy(String col) {
        return orderBy(col, false);
    }

    /**
     * Create an ORDER BY statement
     *
     * @param col - column used for result sorting
     * @param desc - true for descending order, false for ascending
     * @return SqlBuilder
     *
     */
    public SqlBuilder orderBy(String col, boolean desc) {
        allQuery.append(clauseDivider);
        allQuery.append("ORDER BY ");
        allQuery.append(col + (desc ? " DESC" : ""));
        /*if(orderQuery.length() == 0) {
            orderQuery.append("ORDER BY ");
        }
        orderQuery.append(col + (desc? " DESC" : ""));*/
        return this;
    }

    public SqlBuilder addOrderBy(String col) {
        allQuery.append(", " + col);
        return this;
    }

    /**
     * Add an additional ORDER BY statement
     *
     * @param col - column used for result sorting
     * @param desc - true for descending order, false for ascending
     * @return SqlBuilder
     *
     */
    public SqlBuilder addOrderBy(String col, boolean desc) {
        allQuery.append(", ");
        allQuery.append(col + (desc? " DESC" : ""));
        /*if(orderQuery.length() == 0)
            return orderBy(col, desc);
        orderQuery.append(", ");
        return orderBy(col, desc);*/
        return this;
    }

    /**
     * Create a GROUP BY statement
     *
     * @param col - column used for grouping
     * @return SqlBuilder
     *
     */
    public SqlBuilder groupBy(String col) {
        allQuery.append(clauseDivider);
        allQuery.append("GROUP BY " + col);
        /*if(groupQuery.length() == 0) {
            groupQuery.append("GROUP BY ");
        }
        groupQuery.append(col);*/
        return this;
    }

    /**
     * Add additional GROUP BY statement
     *
     * @param col - column used for grouping
     * @return SqlBuilder
     *
     */
    public SqlBuilder addGroupBy(String col) {
        allQuery.append(", " + col);
        /*if(groupQuery.length() == 0) {
            return groupBy(col);
        }
        groupQuery.append(", ");*/
        //return groupBy(col);
        return this;
    }

    public SqlBuilder alterTable(String table) {
        allQuery.append("ALTER TABLE " + table);
        return this;
    }

    public SqlBuilder renameTo(String tableName) {
        allQuery.append(" RENAME TO " + tableName);
        return this;
    }

    public SqlBuilder addColumn(String column, String columnType) {
        allQuery.append(" ADD COLUMN " + column + " " + columnType);
        return this;
    }

    /*public static String getInsertStmnt(String tableName, Object ... vals) {
        String stmnt = "INSERT INTO " + tableName + " VALUES (";
        for(int i=0; i<vals.length; i++) {
            stmnt += "'" + vals[i] + "'";

            if(i < vals.length - 1) {
                stmnt += ", ";
            }
        }
        stmnt += "); ";
        return stmnt;
    }*/
    public static String getInsertStmnt(Object ... vals) {
        String stmnt = "(";
        for(int i=0; i<vals.length; i++) {
            Object value = vals[i];
            if(value != null)
                value = DatabaseUtils.sqlEscapeString(value.toString());
            stmnt += value;

            if(i < vals.length - 1) {
                stmnt += ", ";
            }
        }
        stmnt += ")";
        return stmnt;
    }

    public SqlBuilder insert(String tableName, Object ... vals) {
        String stmnt = "INSERT INTO " + tableName + " VALUES (";
        for(int i=0; i<vals.length; i++) {
            stmnt += vals[i];

            if(i < vals.length - 1) {
                stmnt += ", ";
            }
        }
        stmnt += "); ";
        return this;
    }

    /**
     * Create an UPDATE statement
     *
     * @param tableName - table name to perform update
     * @return SqlBuilder
     *
     */
    public SqlBuilder update(String tableName) {
        allQuery.append("UPDATE " + tableName + " ");
        //updateQuery.append("UPDATE " + tableName);
        return this;
    }

    /**
     * Set update values for UPDATE statement
     *
     * @param key - column name
     * @param value - new column value
     * @return SqlBuilder
     *
     */
    public SqlBuilder set(String key, String value) {
        allQuery.append("SET " + key + " = " + DatabaseUtils.sqlEscapeString(value));
        /*if(updateQuery.length() > 0)
            updateQuery.append(" ");
        updateQuery.append("SET " + key + " = " + value);*/
        return this;
    }

    /**
     * Create a DELETE statement
     *
     * @param tableName - table name to perform delete
     * @return SqlBuilder
     *
     */
    public SqlBuilder delete(String tableName) {
        allQuery.append("DELETE FROM " + tableName);
        //deleteQuery.append("DELETE FROM " + tableName);
        return this;
    }

    /**
     * Append a LIMIT keyword to the query
     *
     * @param size the number of records to return
     * @return SqlBuilder
     *
     */
    public SqlBuilder limit(int size) {
        allQuery.append(" LIMIT " + size);
        return this;
    }

    /**
     * Append a LIMIT keyword to the query
     *
     * @param startIndex the starting index in the cursor
     * @param size the number of records to return
     * @return SqlBuilder
     *
     */
    public SqlBuilder limit(int startIndex, int size) {
        allQuery.append(" LIMIT " + startIndex + "," + size);
        return this;
    }

    /**
     * Replace a surrogate query value with the actual value,
     * the value will be escaped and wrapped in single quotes (''), except null
     *
     * @param var - variable in the query to be replaced with
     * @param value - value to replace, nullable
     *
     */
    public SqlBuilder bindValue(String var, Object value) {
        bindValues.put(var, value);
        return this;
    }

    /**
     * Starting building the SQL query from the blocks specified,
     * and binding the corresponding values.
     * This will be final statement to call
     *
     * @return SqlBuilder
     *
     */
    public SqlBuilder build() {
        query = allQuery.toString();
        bindQueryValues();

        // after building, clear the query string builder
        allQuery = new StringBuilder();

        return this;
    }

    /**
     * Return the built SQL query
     *
     * @return SQL query
     *
     */
    public String getQuery() {
        return query;
    }

    private void bindQueryValues() {
        Iterator iterator = bindValues.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
            String var = entry.getKey();
            Object value = entry.getValue();
            String val = (value != null)? DatabaseUtils.sqlEscapeString(value + "") : "null";

            if(var.charAt(0) == ':') {
                var = var.replaceFirst(":", "");
            }
            //query = query.replaceAll(":"+var, val);
            query = query.replaceAll("(\\s)"+":"+var+"(\\b)", " "+val+" ");
        }
    }

    private String arrayToString(String ... args) {
        String result = "";
        for(int i=0; i<args.length; i++) {
            result += args[i];

            if(i < args.length-1) {
                result += ", ";
            }
        }

        return result;
    }
}
