/*******************************************************************************
 * Copyright 2014 Ingenious Lab

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 *******************************************************************************/

package com.guster.sqlitecreator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.guster.sqlitecreator.annotation.Column;
import com.guster.sqlitecreator.annotation.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Gusterwoei on 1/15/14.
 */

public abstract class Repository<T> {
    protected Context context;
    protected static SQLiteDatabase db;
    protected InglabSQLiteOpenHelper dbManager;
    private Class<T> classType;

    protected static boolean isTransactionBegun = false;

    private String TABLE_NAME;
    private List<String> primaryKeys;
    private List<Field> primaryKeyFields;

    //public Repository(Context context, String tableName, InglabSQLiteOpenHelper dbManager) {
    public Repository(Context context, InglabSQLiteOpenHelper dbManager, Class<T> type) {
        this.context = context;
        this.dbManager = dbManager;
        this.classType = type;
        open();
        init(classType);
    }

    public void open() {
        db = dbManager.openDatabase();
    }

    public void close() {
        dbManager.close();
    }

    public void recreate() {
        dbManager.recreateTable(db);
    }

    protected Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    protected SQLiteDatabase getDb() {
        return db;
    }

    protected void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    public synchronized static void beginTransaction() {
        if(!isTransactionBegun) {
            db.beginTransaction();
            setIsTransactionBegun(true);
        }
    }

    public synchronized static void endTransaction() {
        if(isTransactionBegun) {
            db.setTransactionSuccessful();
            db.endTransaction();
            setIsTransactionBegun(false);
        }
    }

    private void init(Class<?> cls) {
        // if tableName is null, loop through T's constructors and get it
        if(TABLE_NAME == null) {
            Constructor[] constructors = cls.getDeclaredConstructors();
            for(Constructor cont : constructors) {
                Table table = (Table) cont.getAnnotation(Table.class);
                if(table != null) {
                    // get table name
                    TABLE_NAME =  table.name();
                    break;
                }
            }
        }

        // also get the primary keys
        if(primaryKeys == null) {
            primaryKeys = new ArrayList<String>();
            primaryKeyFields = new ArrayList<Field>();
            List<Field> fields = getInheritedFields(cls);
            for(Field field : fields) {
                field.setAccessible(true);
                Column column = field.getAnnotation(Column.class);
                if(column != null && column.primaryKey()) {
                    // add primary key to the primary key list
                    primaryKeys.add(column.column());
                    primaryKeyFields.add(field);
                }
            }
        }
    }

    private void forEachDbField(T item, OnEachFieldListener listener) {
        //List<Field> fields = getInheritedFields(item.getClass());
        List<Field> fields = getInheritedFields(classType);
        for(Field field : fields) {
            try {
                field.setAccessible(true);
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    listener.onEachField(column.column(), field.get(item), field, column);
                }
            } catch (IllegalAccessException e) {
                Log.e("SQLCREATOR", "for each field exception: " + e.getMessage());
            }
        }
    }
    private interface OnEachFieldListener {
        void onEachField(String column, Object value, Field field, Column dbField);
    }

    private static void setIsTransactionBegun(boolean b) {
        isTransactionBegun = b;
    }

    //protected abstract T getInstance(Cursor cursor);
    protected T getInstance(final Cursor cursor) {
        try {
            // create a new object to return
            final T obj;
            obj = classType.getConstructor().newInstance();

            // assign data to each field by column's name
            forEachDbField(obj, new OnEachFieldListener() {
                @Override
                public void onEachField(String column, Object value, Field field, Column dbField) {
                    try {
                        // determine the data type of a column
                        int colIndex = cursor.getColumnIndex(column);
                        int type = cursor.getType(colIndex);
                        Object val = null;
                        switch (type) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                val = cursor.getInt(colIndex);
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                val = cursor.getFloat(colIndex);
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                val = cursor.getString(colIndex);
                                break;
                        }
                        field.set(obj, val);
                    } catch (IllegalAccessException e) {
                        Log.e("SQLCREATOR", "getInstance each field exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            return obj;
        } catch (NoSuchMethodException e) {
            Log.e("SQLCREATOR", "getInstance NoSuchMethodException: " + e.getMessage());
            e.printStackTrace();
        } catch (InstantiationException e) {
            Log.e("SQLCREATOR", "getInstance InstantiationException: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("SQLCREATOR", "getInstance IllegalAccessException: " + e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e("SQLCREATOR", "getInstance InvocationTargetException: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    //protected abstract ContentValues getFields(T t);
    protected ContentValues getFields(final T t) {
        final ContentValues values = new ContentValues();
        forEachDbField(t, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField) {
                if(!dbField.autoIncrement() && value != null) {
                    values.put(column, value + "");
                }
            }
        });
        return values;
    }

    //protected abstract T save(T obj);

    /**
     *
     * @param newItem - New domain item to be saved
     * @return T
     *
     */
    //protected T save(T newItem, T existingItem) {
    public T save(T newItem) {
        init(newItem.getClass());

        String where = getSaveCriteriaWheres(newItem);

        if(where == null) {
            // save / update existing record
            db.insertWithOnConflict(TABLE_NAME, null, getFields(newItem), SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            // save new record based on the save criteria(s)
            List<T> existingList = findByQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + where);
            if(existingList.isEmpty())
                db.insert(TABLE_NAME, null, getFields(newItem));
            else
                db.update(TABLE_NAME, getFields(newItem), where, null);
        }

        /*try {
            String where = "";
            for (int i=0; i<primaryKeyFields.size(); i++) {
                String column = primaryKeys.get(i);
                Object value = primaryKeyFields.get(i).get(existingItem);
                where += column + " = " + DatabaseUtils.sqlEscapeString((String) value);

                if(i < primaryKeyFields.size() - 1) {
                    where += " AND ";
                }
            }
        } catch (IllegalAccessException e) {
            Log.e("SQLCREATOR", "save exception: " + e.getMessage());
            e.printStackTrace();
        }*/

        /*if(existingItem != null) {
            // update existing contact
            String id = "'" + existingItem.get_id() + "'";
            db.update(TABLE_NAME, getFields(newItem), COL_ID + " = " + id, null);
            newItem.set_id(existingItem.get_id());
        } else {
            // insert into db
            long id;
            id = db.insert(TABLE_NAME, null, getFields(newItem));
            newItem.set_id(id);
        }*/

        return newItem;
    }

    public T findOne(Object id) {
        final List<String> wheres = new ArrayList<String>();
        forEachDbField(null, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField) {
                // find by primary key(s)
                if(dbField.primaryKey()) {
                    wheres.add(column + " = " + DatabaseUtils.sqlEscapeString((String) value));
                }
            }
        });

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

        /*if(id instanceof String) {
            id = "'" + id + "'";
        }

        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + COL_ID + " = " + id;

        T item = null;
        List<T> list = cursorToList(query);
        if(list.size() > 0) {
            item = list.get(0);
        }
        return item;*/
    }

    public List<T> findByQuery(String query) {
        return cursorToList(query);
    }

    public List<T> findBy(String col, Object val) {
        if(val != null && val.getClass().equals(String.class)) {
            val = "'" + val + "'";
        }

        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + col + " = " + val;

        return cursorToList(query);
    }

    public List<T> findByGroupBy(String col, Object val, String groupByCol) {
        if(val != null && val.getClass().equals(String.class)) {
            val = "'" + val + "'";
        }

        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + col + " = " + val + " " +
                "GROUP BY " + groupByCol;

        return cursorToList(query);
    }

    public List<T> findByOrderBy(String col, Object val, String orderByCol, boolean desc) {
        if(val != null && val.getClass().equals(String.class)) {
            val = "'" + val + "'";
        }

        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + col + " = " + val + " " +
                "ORDER BY " + orderByCol + " ";
        if(desc) {
            query += "DESC";
        }

        return cursorToList(query);
    }

    public List<T> findByGroupByOrderBy(String col, Object val, String groupByCol, String orderByCol, boolean desc) {
        if(val != null && val.getClass().equals(String.class)) {
            val = "'" + val + "'";
        }

        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + col + " = " + val + " " +
                "GROUP BY " + groupByCol + " " +
                "ORDER BY " + orderByCol;
        if(desc) {
            query += " DESC";
        }

        return cursorToList(query);
    }

    public List<T> findAll() {
        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME;

        return cursorToList(query);
    }

    public List<T> findAllGroupBy(String col) {
        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "GROUP BY " + col;

        return cursorToList(query);
    }

    public List<T> findAllOrderBy(String col, boolean desc) {
        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "ORDER BY " + col + " ";
        if(desc) {
            query += "DESC";
        }

        return cursorToList(query);
    }

    public List<T> findAllGroupByOrderBy(String groupByCol, String orderByCol, boolean desc) {
        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME + " " +
                "GROUP BY " + groupByCol + " " +
                "ORDER BY " + orderByCol + " ";
        if(desc) {
            query += "DESC";
        }

        return cursorToList(query);
    }

    public List<T> findByCriteria(T criteria) {
        return findByCriteria(criteria, null);
    }

    public List<T> findByCriteria(T criteria, String orderColName) {
        // insert WHERE clause
        String strCris = getWhereQueryParts(criteria, null);
        if (strCris.length() > 0)
            strCris = " WHERE " + strCris;
        String query = "" +
                "SELECT * " +
                "FROM " + TABLE_NAME
                + strCris;
        if(orderColName != null && !orderColName.equals(""))
            query += " ORDER BY " + orderColName;
        //Log.d("ABC", criteria.getClass().getName() + " query: " + query);
        return cursorToList(query);
    }

    private List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public String getWhereQueryParts(T criteria, String alias) {
        StringBuilder sb = new StringBuilder();
        //Field[] allParentFields = criteria.getClass().getDeclaredFields();
        List<Field> allFields = getInheritedFields(criteria.getClass());

        try {
            for(Field f : allFields) {
                f.setAccessible(true);
                Object value = f.get(criteria);
                if(value != null && !value.equals("")) {
                    // get field's annotation
                    Column df = f.getAnnotation(Column.class);
                    if(df != null) {
                        if (sb.length() > 0)
                            sb.append(" AND ");
                        if(f.getType().equals(Boolean.class)) {
                            //Log.d("ABC", "FIELD BOOLEAN - " + f.getName());
                            value = Boolean.parseBoolean(value+"")? 1 : 0;
                        }
                        sb.append( ((alias != null)? alias+"." : "") + df.column() + " = " + DatabaseUtils.sqlEscapeString(value + "") );
                    }
                }
            }
            return sb.toString();
        } catch(Exception e) {
            Log.e("ABC", "Repository GetWhereStatement Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    public List<T> findBySaveCriteria(T item) {
        // get criteria statement for each field marked with saveCriteria = true
        String where = getSaveCriteriaWheres(item);

        // apply where clause to the query
        if(where != null) {
            String query = "" +
                    "SELECT * " +
                    "FROM " + TABLE_NAME + " " +
                    "WHERE " + where;
            return findByQuery(query);
        }
        return new ArrayList<T>();
    }

    private String getSaveCriteriaWheres(T item) {
        final List<String> wheres = new ArrayList<String>();
        forEachDbField(item, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField) {
                if(dbField.saveCriteria()) {
                    wheres.add(column + " = " + DatabaseUtils.sqlEscapeString((String) value));
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

    protected List<T> cursorToList(String query) {
        return cursorToList(query, new CursorToInstanceListener<T>() {
            @Override
            public T onEachCursor(Cursor cursor) {
                return getInstance(cursor);
            }
        });
        /*List<T> items = new ArrayList<T>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if(cursor.getCount() <= 0) return items;

            cursor.moveToFirst();
            do {
                T item = getInstance(cursor);
                items.add(item);
            } while(cursor.moveToNext());
        } finally {
            if(cursor != null)
                cursor.close();
        }

        return items;*/
    }

    public interface CursorToInstanceListener<T> {
        T onEachCursor(Cursor cursor);
    }

    public List<T> cursorToList(String query, CursorToInstanceListener<T> listener) {
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

    public void runQuery(String query) {
        db.execSQL(query);
    }

    public boolean delete(T car) {
        init(car.getClass());

        try {
            String where = "";
            for(int i=0; i<primaryKeyFields.size(); i++) {
                String column = primaryKeys.get(i);
                String value = primaryKeyFields.get(i).get(car).toString();
                where += column + " = " + DatabaseUtils.sqlEscapeString(value);

                if(i < primaryKeyFields.size() - 1) {
                    where += " AND ";
                }
            }

            int rows = db.delete(TABLE_NAME, where, null);
            return (rows > 0);
        } catch (IllegalAccessException e) {
            Log.e("SQLCREATOR", "sqlite delete exception: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
        //int rows = db.delete(TABLE_NAME, COL_ID + " = " + car.get_id(), null);
        //return (rows > 0);
    }

    public boolean deleteBy(String col, Object val) {
        if(val.getClass().equals(String.class)) {
            val = "'" + val + "'";
        }

        int rows = db.delete(TABLE_NAME, col + " = " + val, null);
        //app.saveDbToSdCard();
        return (rows > 0);
    }

    public void deleteAll() {
        for(T t : findAll()) {
            delete(t);
        }
    }

    public int size() {
        return findAll().size();
    }
}
