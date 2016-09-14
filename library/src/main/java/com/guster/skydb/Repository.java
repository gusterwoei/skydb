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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;
import com.guster.sqlbuilder.SqlBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gusterwoei on 1/15/14.
 */

public class Repository<T> {

    private interface OnEachFieldListener {
        void onEachField(String column, Object value, Field field, Column dbField, int colIndex);
    }
    
    public interface CursorToInstanceListener<T> {
        T onEachCursor(Cursor cursor);
    }
    
    protected static boolean isTransactionBegun = false;
    private static Map<String, Repository<?>> repositories = new HashMap<>();

    // internal statistic data
    private static SQLiteDatabase db;
    private Class<T> classType;
    private String TABLE_NAME;
    private List<String> primaryKeys;
    private List<Field> primaryKeyFields;
    private List<Field> allDbFields;
    private List<Column> allDbFieldAnnotations;
    private int numberOfColumns;


    public Repository(Class<T> type) {
        db = SkyDatabase.getInstance().createDatabase();
        this.classType = type;
        init(classType);
    }

    protected SQLiteDatabase getSQLDatabase() {
        return db;
    }

    public synchronized static void beginTransaction() {
        if(!isTransactionBegun) {
            setIsTransactionBegun(true);
            db.beginTransaction();
        }
    }

    public synchronized static void endTransaction() {
        if(isTransactionBegun) {
            setIsTransactionBegun(false);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    private static void setIsTransactionBegun(boolean b) {
        isTransactionBegun = b;
    }

    /**
     * Return a singleton Repository that represents an Entity of class {@code cls}
     *
     * @param cls   Entity class
     * @param <T>   Entity class type
     * @return      Repository of a given class type
     */
    public static <T> Repository<T> get(Class<T> cls) {
        if(!repositories.containsKey(cls.getName())) {
            repositories.put(cls.getName(), new Repository<>(cls));
        }

        return (Repository<T>) repositories.get(cls.getName());
    }

    private void init(Class<?> cls) {
        // if tableName is null, loop through T's constructors and get it
        if(TABLE_NAME == null) {
            Table table = cls.getAnnotation(Table.class);
            if(table != null) {
                // get table name
                TABLE_NAME = table.name();
            }
        }

        // initialize all columns and its fields
        if(allDbFields == null) {
            primaryKeys = new ArrayList<>();
            primaryKeyFields = new ArrayList<>();
            allDbFields = new ArrayList<>();
            allDbFieldAnnotations = new ArrayList<>();
            List<Field> fields = getInheritedFields(cls);

            int size = 0;
            for(Field field : fields) {
                field.setAccessible(true);
                Column column = field.getAnnotation(Column.class);
                if(column != null) {
                    if (column.primaryKey()) {
                        // add primary key to the primary key list
                        primaryKeys.add(column.name());
                        primaryKeyFields.add(field);
                    }

                    allDbFields.add(field);
                    allDbFieldAnnotations.add(column);

                    size++;
                }
            }

            // set total number of columns
            numberOfColumns = size;
        }
    }

    private void forEachDbField(T item, OnEachFieldListener listener) {
        int i = 0;
        for(Field field : allDbFields) {
            try {
                Column column = allDbFieldAnnotations.get(i);
                listener.onEachField(column.name(),
                        (item != null ? field.get(item) : null),
                        field,
                        column, i);
                i++;
            } catch (IllegalAccessException e) {
                Util.loge("for each field exception: ", e);
                raise(e);
            }
        }
    }
    
    private void raise(Throwable e) {
        throw new SkyDbException(e);
    }

    private List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private String getUniqueWheres(T item) {
        final List<String> wheres = new ArrayList<>();
        forEachDbField(item, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField, int index) {
                if(dbField.unique()) {
                    wheres.add(column + " = " + (value != null? DatabaseUtils.sqlEscapeString((String) value) : "null") );
                }
            }
        });

        // construct where query part
        String query = null;
        if(!wheres.isEmpty()) {
            query = "";
            for (int i = 0; i < wheres.size(); i++) {
                String where = wheres.get(i);
                query += where;
                if (i < wheres.size() - 1) {
                    query += " AND ";
                }
            }
        }
        return query;
    }

    /**
     * Return an entity object with a given cursor, the fields are mapped automatically
     * based on @Column and the cursor's column name
     *
     * @param cursor    Database Cursor
     * @return          Object of type T
     */
    public T getInstance(final Cursor cursor) {
        try {
            // create a new object to return
            final T obj;
            obj = classType.getConstructor().newInstance();

            // assign data to each field by column's name
            for(Field field : allDbFields) {
                try {
                    // determine the data type of a column
                    Column col = field.getAnnotation(Column.class);
                    int columnIndex = cursor.getColumnIndex(col.name());
                    int type = cursor.getType(columnIndex);

                    Object val = null;

                    switch (type) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            //int valint = cursor.getInt(columnIndex);
                            if(field.getType().equals(Long.class) || field.getType().equals(Long.TYPE)) {
                                //val = Long.parseLong(valint+"");
                                val = cursor.getLong(columnIndex);
                            } else if(field.getType().equals(Boolean.class) || field.getType().equals(Boolean.TYPE)) {
                                int valint = cursor.getInt(columnIndex);
                                val = (valint == 1);
                            } else {
                                val = cursor.getInt(columnIndex);
                            }
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            if(field.getType().equals(Double.class) || field.getType().equals(Double.TYPE)) {
                                //val = Double.parseDouble(val+"");
                                val = cursor.getDouble(columnIndex);
                            } else {
                                val = cursor.getFloat(columnIndex);
                            }
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            val = cursor.getString(columnIndex);
                            break;
                    }

                    field.set(obj, val);
                
                } catch (IllegalAccessException e) {
                    Util.loge("getInstance each field exception: ", e);
                    raise(e);
                }
                //colIndex++;
            }

            return obj;
        } catch (NoSuchMethodException e) {
            Util.loge("getInstance NoSuchMethodException: ", e);
            raise(e);
        } catch (InstantiationException e) {
            Util.loge("getInstance InstantiationException: ", e);
            raise(e);
        } catch (IllegalAccessException e) {
            Util.loge("getInstance IllegalAccessException: ", e);
            raise(e);
        } catch (InvocationTargetException e) {
            Util.loge("getInstance InvocationTargetException: ", e);
            raise(e);
        }

        return null;
    }

    protected ContentValues getFields(final T t) {
        final ContentValues values = new ContentValues();
        forEachDbField(t, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField, int index) {
                if(!dbField.autoIncrement() && value != null) {
                    if(field.getType().equals(Boolean.class) || field.getType().equals(Boolean.TYPE))
                        values.put(column, Boolean.valueOf(value + ""));
                    else
                        values.put(column, value + "");
                }
            }
        });
        return values;
    }

    private List<T> cursorToList(String query) {
        init(classType);
        return cursorToList(query, new CursorToInstanceListener<T>() {
            @Override
            public T onEachCursor(Cursor cursor) {
                return getInstance(cursor);
            }
        });
    }

    private List<T> cursorToList(String query, CursorToInstanceListener<T> listener) {
        List<T> items = new ArrayList<T>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if(cursor.getCount() <= 0) return items;

            cursor.moveToFirst();
            do {
                if(listener != null) {
                    items.add(listener.onEachCursor(cursor));
                } else {
                    items.add(getInstance(cursor));
                }
            } while(cursor.moveToNext());

        } finally {
            if(cursor != null)
                cursor.close();
        }

        return items;
    }

    /**
     * Persist or update an SkyDb-annotated item into database depending on the existence of item
     * in the database. The existence check is based on the entity's Primary Key and Unique fields.
     *
     * @param item  New item to be saved
     * @return      T
     * @throws      SkyDbException
     */
    public T save(T item) {
        init(item.getClass());

        Long id;
        id = db.insertWithOnConflict(TABLE_NAME, null, getFields(item), SQLiteDatabase.CONFLICT_REPLACE);

        if(id != null) {
            for(Field f : primaryKeyFields) {
                try {
                    Column column = f.getAnnotation(Column.class);
                    if(column.autoIncrement()) {
                        f.set(item, id);
                        break;
                    }
                } catch (IllegalAccessException e) {
                    Util.loge("save()", e);
                    raise(e);
                }
            }
        }

        return item;
    }

    /**
     * Persist or update a list of entity objects into database depending on the existence of item
     * in the database. The existence check is based on the entity's Primary Key and Unique fields.
     *
     * @param items  List of saving items
     */
    public void saveAll(List<T> items) {
        int maxRowsPerInsert = 25;
        int size = items.size();
        int numOfInserts = (size / maxRowsPerInsert);
        int remainInserts = (size % maxRowsPerInsert);
        int count = 0;

        // threshold inserts
        //beginTransaction();
        for(int k=0; k<numOfInserts; k++) {
            // one insert statement
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<maxRowsPerInsert; i++) {
                T item = items.get(count);

                addInsertValues(item, sb);

                if(i < maxRowsPerInsert- 1) {
                    sb.append(", ");
                }
                count++;
            }
            String sql = "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES " + sb.toString();
            runQuery(sql);
        }

        // remaining inserts
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<remainInserts; i++) {
            T subject = items.get(count);
            addInsertValues(subject, sb);

            if(i < remainInserts - 1) {
                sb.append(", ");
            }
            count++;
        }
        if(remainInserts > 0) {
            String sql = "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES " + sb.toString();
            runQuery(sql);
        }
        //endTransaction();
    }

    private void addInsertValues(T item, StringBuilder sb) {
        final Object[] dbValues = new Object[numberOfColumns];
        int i = 0;
        try {
            for (Field field : allDbFields) {
                dbValues[i] = field.get(item);
                i++;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        sb.append(SqlBuilder.arrayToCommaSeparatedString(dbValues));
    }

    /**
     * Find an entity object based on its primary key value. If an entity does not contain
     * a primary key, null will be returned.
     *
     * @param id    primary key id
     * @return      entity object with primary key value = id, or null if not found
     */
    public T find(final Object id) {
        final List<String> wheres = new ArrayList<>();
        forEachDbField(null, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField, int index) {
                // find by primary key(s)
                if(dbField.primaryKey()) {
                    //wheres.add(column + " = " + DatabaseUtils.sqlEscapeString((String) value));
                    wheres.add(column + " = " + DatabaseUtils.sqlEscapeString((String) id));
                }
            }
        });

        if(wheres.isEmpty()) {
            return null;
        }

        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "WHERE ";
        for(int i=0; i<wheres.size(); i++) {
            query += wheres.get(i);
            if(i < wheres.size() - 1) {
                query += " AND ";
            }
        }
        List<T> result = findByQuery(query);

        return result.isEmpty()? null : result.get(0);
    }

    /**
     * Find a list of entity objects based on a given query string.
     *
     * @param query     Sql query
     * @return          List of entity objects
     */
    public List<T> findByQuery(String query) {
        return cursorToList(query);
    }

    /**
     * Find a List of entity object based on a given query string with an intermediate callback
     * for each row returned. CursorToInstanceListener will return a cursor for each row,
     * you can use this for customization.
     *
     * @param query     Sql query
     * @param listener  A callback for each row returned
     * @return          List of found entities
     */
    public List<T> findByQuery(String query, CursorToInstanceListener<T> listener) {
        return cursorToList(query, listener);
    }

    /**
     * Find a List of entity object based on a column.
     *
     * @param column    table column
     * @param value     table value
     * @return          List of found entities
     */
    public List<T> findBy(String column, Object value) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(column + " = :a1")
                .bindValue("a1", value)
                .getQuery();

        return cursorToList(query);
    }

    /**
     * Find a List of entity object based on a given list of columns and values.
     * The number of columns and the number of values must be the same.
     *
     * @param columns   table columns to query with
     * @param values    columns' values
     * @param order     order criteria (eg. name, title DESC)
     * @return          List of found entities
     */
    public List<T> findBy(String[] columns, Object[] values, String order) {
        if(columns == null || values == null) {
            throw new IllegalArgumentException("columns and values arguments cannot be null");
        } else if(columns.length != values.length) {
            throw new IllegalArgumentException("columns and values size must match");
        }

        SqlBuilder builder = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t");
        for(int i=0; i<columns.length; i++) {
            String col = columns[i];
            Object val = values[i];
            String where = col + " = " + (val != null? DatabaseUtils.sqlEscapeString(val+"") : "null");
            if(i == 0)
                builder.where(where);
            else
                builder.andWhere(where);

            if(order != null && !order.isEmpty()) {
                builder.orderBy(order);
            }
        }

        String query = builder.getQuery();

        return cursorToList(query);
    }

    /**
     * Find a List of entity object based on a given list of columns and values.
     * The returned result is group by {@code groupByCol}
     *
     * @param column        Table column
     * @param value         column value
     * @param groupByCol    column to group
     * @return              List of entity object
     */
    public List<T> findByGroupBy(String column, Object value, String groupByCol) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(column + " = :a1")
                .groupBy(groupByCol)
                .bindValue("a1", value).getQuery();

        return cursorToList(query);
    }

    /**
     * Find a List of entity object based on a given list of columns and values.
     * The returned result is sorted by {@code orderByCol} and {@code desc}
     *
     * @param column        table column
     * @param value         column value
     * @param orderByCol    column to sort by
     * @param desc          true for descending, false for ascending
     * @return              List of entity object
     */
    public List<T> findByOrderBy(String column, Object value, String orderByCol, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(column + " = :a1")
                .orderBy(orderByCol, desc)
                .bindValue("a1", value).getQuery();

        return cursorToList(query);
    }

    /**
     * Find a List of entity object based on a given list of columns and values.
     * The returned result is group by {@code groupByCol} and sorted by {@code orderByCol} and {@code desc}
     *
     * @param column        Table column
     * @param value         column value
     * @param groupByCol    column to group by
     * @param orderByCol    column to sort by
     * @param desc          true for descending, false for ascending
     * @return              List of entity object
     */
    public List<T> findByGroupByOrderBy(String column, Object value, String groupByCol, String orderByCol, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(column + " = :a1")
                .groupBy(groupByCol)
                .orderBy(orderByCol, desc)
                .bindValue("a1", value).getQuery();

        return cursorToList(query);
    }

    /**
     * Find all records of an entity
     * 
     * @return      List of entity object
     */
    public List<T> findAll() {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t").getQuery();

        return cursorToList(query);
    }

    /**
     * Find all records of an entity, group by {@code column}
     *
     * @param column    column to group by
     * @return          List of entity object
     */
    public List<T> findAllGroupBy(String column) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .groupBy(column)
                .getQuery();

        return cursorToList(query);
    }

    /**
     * Find all records of an entity, order by {@code column}
     *
     * @param column    table column
     * @param desc      true for descending, false for ascending
     * @return          List of entity object
     */
    public List<T> findAllOrderBy(String column, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .orderBy(column, desc)
                .getQuery();

        return cursorToList(query);
    }

    /**
     * Find all records of an entity, group by {@code groupByCol} order by {@code orderByCol}
     *
     * @param groupByCol    column to group by
     * @param orderByCol    column to sort by
     * @param desc          true for descending, false for ascending
     * @return              List of entity object
     */
    public List<T> findAllGroupByOrderBy(String groupByCol, String orderByCol, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .groupBy(groupByCol)
                .orderBy(orderByCol, desc)
                .getQuery();

        return cursorToList(query);
    }

//    public List<T> findByCriteria(T criteria) {
//        return findByCriteria(criteria, null);
//    }
//
//    public List<T> findByCriteria(T criteria, String orderColName) {
//        // insert WHERE clause
//        String strCris = getWhereQueryParts(criteria, null);
//        if (strCris.length() > 0)
//            strCris = " WHERE " + strCris;
//        String query = "" +
//                "SELECT * " +
//                "FROM " + TABLE_NAME
//                + strCris;
//        if(orderColName != null && !orderColName.equals(""))
//            query += " ORDER BY " + orderColName;
//        return cursorToList(query);
//    }

//    public List<T> findUnique(T item) {
//        // get criteria statement for each field marked with unique = true
//        String where = getUniqueWheres(item);
//
//        // apply where clause to the query
//        if(where != null) {
//            String query = SqlBuilder.newInstance()
//                    .select("*")
//                    .from(TABLE_NAME, "t")
//                    .where(where)
//                    .getQuery();
//            return findByQuery(query);
//
//        }
//        return new ArrayList<T>();
//    }

//    protected String getWhereQueryParts(T criteria, String alias) {
//        StringBuilder sb = new StringBuilder();
//        List<Field> allFields = getInheritedFields(criteria.getClass());
//
//        try {
//            for(Field f : allFields) {
//                f.setAccessible(true);
//                Object value = f.get(criteria);
//                if(value != null && !value.equals("")) {
//                    // get field's annotation
//                    Column df = f.getAnnotation(Column.class);
//                    if(df != null) {
//                        if (sb.length() > 0)
//                            sb.append(" AND ");
//                        if(f.getType().equals(Boolean.class)) {
//                            value = Boolean.parseBoolean(value+"")? 1 : 0;
//                        }
//                        sb.append( ((alias != null)? alias+"." : "") + df.name() + " = " + DatabaseUtils.sqlEscapeString(value + "") );
//                    }
//                }
//            }
//            return sb.toString();
//        } catch(Exception e) {
//            Util.loge("Repository GetWhereStatement Exception: ", e);
//            raise(e);
//        }
//        return "";
//    }

    /**
     * Find a list of entity records based on a Criteria. Each criteria's condition is glued
     * by an AND.
     *
     * Example:
     *
     * Criteria criteria = new Criteria()
     *      .equal("first_name", "Julia")
     *      .equal("last_name", "Mai")
     *      .notEqual("status", 0)
     *      .greaterThan("score", 90)
     *      .between("height", 160, 170);
     * List<Person> persons = findByCriteria(criteria);
     *
     * @param criteria      Criteria object
     * @return              List of entity objects
     */
    public List<T> findByCriteria(Criteria criteria) {
        return findByCriteria(criteria, null);
    }

    /**
     * Find a list of entity records based on a Criteria. Each criteria's condition is glued
     * by an AND.
     *
     * Example:
     *
     * Criteria criteria = new Criteria()
     *      .equal("first_name", "Julia")
     *      .equal("last_name", "Mai")
     *      .notEqual("status", 0)
     *      .greaterThan("score", 90)
     *      .between("height", 160, 170);
     * List<Person> persons = findByCriteria(criteria);
     *
     * @param criteria      Criteria object
     * @param order         column to sort
     * @return              List of entity objects
     */
    public List<T> findByCriteria(Criteria criteria, String order) {
        SqlBuilder builder = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(criteria.build());
        if(order != null) {
            builder.orderBy(order);
        }
        String query = builder.getQuery();
        return findByQuery(query);
    }

    /**
     * Update an entity's columns defined in {@code values} by a given column and its equal value.
     *
     * @param column    where column
     * @param value     where value
     * @param values    columns to update and the their values
     * @return          number of updated rows
     */
    public int updateBy(String column, Object value, ContentValues values) {
        String wheres = (column + " = ?");
        String[] whereArgs = new String[] { (value != null ? String.valueOf(value) : null) };

        return db.update(TABLE_NAME, values, wheres, whereArgs);
    }

    /**
     * Update an entity's columns defined in {@code values} based on the conditions defined
     * in {@code criteria}
     *
     * @param criteria  a Criteria object that defines a set of conditions
     * @param values    columns to update and the their values
     * @return          number of updated rows
     */
    public int updateBy(Criteria criteria, ContentValues values) {
        String where = criteria.build();
        return db.update(TABLE_NAME, values, where, null);
    }

    /**
     * Execute a raw Sql query.
     *
     * @param query - Sql query
     */
    public void runQuery(String query) {
        db.execSQL(query);
    }

    /**
     * Delete an entity object based on its primary key value.
     *
     * @param object    entity object
     * @return          true if an object is deleted
     */
    public boolean delete(T object) {
        init(object.getClass());

        try {
            String where = "";
            for(int i=0; i<primaryKeyFields.size(); i++) {
                String column = primaryKeys.get(i);
                String value = primaryKeyFields.get(i).get(object).toString();
                where += column + " = " + DatabaseUtils.sqlEscapeString(value);

                if(i < primaryKeyFields.size() - 1) {
                    where += " AND ";
                }
            }

            // no primary key is defined
            if(where.isEmpty()) {
                return false;
            }

            int rows = db.delete(TABLE_NAME, where, null);
            return (rows > 0);
        } catch (IllegalAccessException e) {
            Util.loge("delete(): ", e);
            raise(e);
        }

        return false;
    }

    /**
     * Delete entity objects based on given column and column value.
     *
     * @param column    table column
     * @param value     column value
     * @return          true if at least one row is deleted
     */
    public boolean deleteBy(String column, Object value) {
        int rows;
        if(value != null)
            rows = db.delete(TABLE_NAME, column + " = ?", new String[] {String.valueOf(value)});
        else
            rows = db.delete(TABLE_NAME, column + " IS NULL", null);
        return (rows > 0);
    }

    /**
     * Delete an entity data entirely
     */
    public void deleteAll() {
        runQuery(SqlBuilder.newInstance().delete(TABLE_NAME).getQuery());
    }

    /**
     * Return the number of rows in an entity table.
     *
     * @return      number of rows
     */
    public int size() {
        return findAll().size();
    }

}
