package com.guster.sqlitecreator;

import android.database.DatabaseUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private StringBuilder selectQuery = new StringBuilder();
    private StringBuilder fromQuery = new StringBuilder();
    private StringBuilder whereQuery = new StringBuilder();
    private StringBuilder innerJoinQuery = new StringBuilder();
    private StringBuilder leftJoinQuery = new StringBuilder();
    private StringBuilder orderQuery = new StringBuilder();
    private StringBuilder groupQuery = new StringBuilder();
    private StringBuilder insertQuery = new StringBuilder();
    private StringBuilder updateQuery = new StringBuilder();
    private StringBuilder deleteQuery = new StringBuilder();
    private StringBuilder alterQuery = new StringBuilder();
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
        if(selectQuery.length() == 0) {
            selectQuery.append("SELECT ");
        }
        selectQuery.append(arrayToString(args));
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
        if(selectQuery.length() == 0) {
            return select(args);
        }
        selectQuery.append(", ");
        selectQuery.append(arrayToString(args));
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
        if(fromQuery.length() == 0) {
            fromQuery.append("FROM ");
        }
        fromQuery.append(tableName + " " + alias);
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
        if(whereQuery.length() == 0) {
            whereQuery.append("WHERE ");
        }
        whereQuery.append(where);
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
        if(whereQuery.length() > 0)
            whereQuery.append(" ");
        whereQuery.append("OR " + where);
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
        if(whereQuery.length() > 0)
            whereQuery.append(" ");
        whereQuery.append("AND " + where);
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
        if(innerJoinQuery.length() != 0)
            innerJoinQuery.append(clauseDivider);
        String q = "INNER JOIN " + joinTableName + " " + joinTableAlias + " ON " + condition;
        innerJoinQuery.append(q);
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
        if(leftJoinQuery.length() != 0)
            leftJoinQuery.append(clauseDivider);
        String q = "LEFT OUTER JOIN " + joinTableName + " " + joinTableAlias + " ON " + condition;
        leftJoinQuery.append(q);
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
        if(orderQuery.length() == 0) {
            orderQuery.append("ORDER BY ");
        }
        orderQuery.append(col + (desc? " DESC" : ""));
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
        if(orderQuery.length() == 0)
            return orderBy(col, desc);
        orderQuery.append(", ");
        return orderBy(col, desc);
    }

    /**
     * Create a GROUP BY statement
     *
     * @param col - column used for grouping
     * @return SqlBuilder
     *
     */
    public SqlBuilder groupBy(String col) {
        if(groupQuery.length() == 0) {
            groupQuery.append("GROUP BY ");
        }
        groupQuery.append(col);
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
        if(groupQuery.length() == 0) {
            return groupBy(col);
        }
        groupQuery.append(", ");
        return groupBy(col);
    }

    public SqlBuilder alterTable(String table) {
        alterQuery.append("ALTER TABLE " + table);
        return this;
    }

    public SqlBuilder renameTo(String tableName) {
        alterQuery.append(" RENAME TO " + tableName);
        return this;
    }

    public SqlBuilder addColumn(String column, String columnType) {
        alterQuery.append(" ADD COLUMN " + column + " " + columnType);
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
        updateQuery.append("UPDATE " + tableName);
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
        if(updateQuery.length() > 0)
            updateQuery.append(" ");
        updateQuery.append("SET " + key + " = " + value);
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
        deleteQuery.append("DELETE FROM " + tableName);
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
    public void bindValue(String var, Object value) {
        bindValues.put(var, value);
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
        StringBuilder sb = new StringBuilder();
        String space = clauseDivider;
        sb.append(selectQuery.toString());
        sb.append(space);
        sb.append(fromQuery.toString());
        sb.append(space);
        sb.append(innerJoinQuery.toString());
        sb.append(space);
        sb.append(leftJoinQuery.toString());
        if(insertQuery.length() > 0) {
            sb.append(insertQuery.toString());
        }
        if(updateQuery.length() > 0) {
            //sb.append(space);
            sb.append(updateQuery.toString());
        }
        if(deleteQuery.length() > 0) {
            //sb.append(space);
            sb.append(deleteQuery.toString());
        }
        sb.append(space);
        sb.append(whereQuery.toString());
        sb.append(space);
        sb.append(groupQuery.toString());
        sb.append(space);
        sb.append(orderQuery.toString());
        if(alterQuery.length() > 0) {
            sb.append(alterQuery);
        }

        query = sb.toString();
        bindQueryValues();

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
            query = query.replaceAll(":"+var, val);
            //query = query.replaceAll(var, val);
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
