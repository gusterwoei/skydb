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
import android.content.Context;
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
import java.util.List;

/**
 * Created by Gusterwoei on 1/15/14.
 */

public abstract class Repository<T> {
    private static SQLiteDatabase db;
    protected static boolean isTransactionBegun = false;

    private Context context;
    private Class<T> classType;

    // internal statistic data
    private String TABLE_NAME;
    private List<String> primaryKeys;
    private List<Field> primaryKeyFields;
    private List<Field> allDbFields;
    private List<Column> allDbFieldAnnotations;
    private int numberOfColumns;

    public Repository(Context context, Class<T> type) {
        this.context = context;
        db = SkyDatabase.getInstance().createDatabase();
        this.classType = type;
        init(classType);
    }

    protected Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
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

    private void init(Class<?> cls) {
        // if tableName is null, loop through T's constructors and get it
        if(TABLE_NAME == null) {
            Table table = cls.getAnnotation(Table.class);
            if(table != null) {
                // get table name
                TABLE_NAME = table.name();
            }
            /*Constructor[] constructors = cls.getDeclaredConstructors();
            for(Constructor cont : constructors) {
                Table table = (Table) cont.getAnnotation(Table.class);
                if(table != null) {
                    // get table name
                    TABLE_NAME =  table.name();
                    break;
                }
            }*/
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
        //List<Field> fields = getInheritedFields(classType);
        for(Field field : allDbFields) {
            try {
                Column column = allDbFieldAnnotations.get(i);
                listener.onEachField(column.name(), field.get(item), field, column, i);
                i++;
            } catch (IllegalAccessException e) {
                Util.loge("for each field exception: " + e.getMessage());
            }
        }
    }
    private interface OnEachFieldListener {
        void onEachField(String column, Object value, Field field, Column dbField, int colIndex);
    }

    private static void setIsTransactionBegun(boolean b) {
        isTransactionBegun = b;
    }

    protected T getInstance(final Cursor cursor) {
        try {
            // create a new object to return
            final T obj;
            obj = classType.getConstructor().newInstance();

            // assign data to each field by column's name
            //int colIndex = 0;
            for(Field field : allDbFields) {
                try {
                    // determine the data type of a column
                    Column col = field.getAnnotation(Column.class);
                    int columnIndex = cursor.getColumnIndex(col.name());
                    int type = cursor.getType(columnIndex);

                    Object val = null;

                    switch (type) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            val = cursor.getInt(columnIndex);
                            if(field.getType().equals(Long.class) || field.getType().equals(Long.TYPE)) {
                                val = Long.parseLong(val+"");

                            } else if(field.getType().equals(Boolean.class) || field.getType().equals(Boolean.TYPE)) {
                                val = (val == 1);
                            }
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            val = cursor.getFloat(columnIndex);
                            if(field.getType().equals(Double.class) || field.getType().equals(Double.TYPE)) {
                                val = Double.parseDouble(val+"");
                            }
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            val = cursor.getString(columnIndex);
                            break;
                    }

                    field.set(obj, val);
                
                } catch (IllegalAccessException e) {
                    Util.loge("getInstance each field exception: " + e.getMessage());
                    e.printStackTrace();
                }
                //colIndex++;
            }

            return obj;
        } catch (NoSuchMethodException e) {
            Util.loge("getInstance NoSuchMethodException: " + e.getMessage());
            e.printStackTrace();
        } catch (InstantiationException e) {
            Util.loge("getInstance InstantiationException: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Util.loge("getInstance IllegalAccessException: " + e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Util.loge("getInstance InvocationTargetException: " + e.getMessage());
            e.printStackTrace();
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

    //protected abstract T save(T obj);

    /**
     *
     * @param newItem - New domain item to be saved
     * @return T
     *
     */
    public T save(T newItem) {
        init(newItem.getClass());

        Long id = null;
        /*String where = getUniqueWheres(newItem);
        if(where == null) {
            // save / update existing record
            id = db.insertWithOnConflict(TABLE_NAME, null, getFields(newItem), SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            // save new record based on the save criteria(s)
            List<T> existingList = findByQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + where);
            if(existingList.isEmpty())
                id = db.insert(TABLE_NAME, null, getFields(newItem));
            else
                db.update(TABLE_NAME, getFields(newItem), where, null);
        }*/
        id = db.insertWithOnConflict(TABLE_NAME, null, getFields(newItem), SQLiteDatabase.CONFLICT_REPLACE);

        if(id != null) {
            for(Field f : primaryKeyFields) {
                try {
                    Column column = f.getAnnotation(Column.class);
                    if(column.autoIncrement()) {
                        f.set(newItem, id);
                        break;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Util.loge(getClass().getSimpleName() + ": Save Exception - " + e.getMessage());
                }
            }
        }

        return newItem;
    }

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

    public T findOne(Object id) {
        final List<String> wheres = new ArrayList<>();
        forEachDbField(null, new OnEachFieldListener() {
            @Override
            public void onEachField(String column, Object value, Field field, Column dbField, int index) {
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
    }

    public List<T> findByQuery(String query) {
        return cursorToList(query);
    }

    public List<T> findBy(String col, Object val) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(col + " = :a1")
                .bindValue("a1", val)
                .getQuery();

        return cursorToList(query);
    }

    public List<T> findBy(String[] columns, Object[] values, String orderColumn) {
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

            if(orderColumn != null && !orderColumn.isEmpty()) {
                builder.orderBy(orderColumn);
            }
        }

        String query = builder.getQuery();

        return cursorToList(query);
    }

    public List<T> findByGroupBy(String col, Object val, String groupByCol) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(col + " = :a1")
                .groupBy(groupByCol)
                .bindValue("a1", val).getQuery();

        return cursorToList(query);
    }

    public List<T> findByOrderBy(String col, Object val, String orderByCol, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(col + " = :a1")
                .orderBy(orderByCol, desc)
                .bindValue("a1", val).getQuery();

        return cursorToList(query);
    }

    public List<T> findByGroupByOrderBy(String col, Object val, String groupByCol, String orderByCol, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .where(col + " = :a1")
                .groupBy(groupByCol)
                .orderBy(orderByCol, desc)
                .bindValue("a1", val).getQuery();

        return cursorToList(query);
    }

    public List<T> findAll() {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t").getQuery();

        return cursorToList(query);
    }

    public List<T> findAllGroupBy(String col) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .groupBy(col)
                .getQuery();

        return cursorToList(query);
    }

    public List<T> findAllOrderBy(String col, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .orderBy(col, desc)
                .getQuery();

        return cursorToList(query);
    }

    public List<T> findAllGroupByOrderBy(String groupByCol, String orderByCol, boolean desc) {
        String query = SqlBuilder.newInstance()
                .select("*")
                .from(TABLE_NAME, "t")
                .groupBy(groupByCol)
                .orderBy(orderByCol, desc)
                .getQuery();

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
        //Util.logd(criteria.getClass().getName() + " query: " + query);
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
                            value = Boolean.parseBoolean(value+"")? 1 : 0;
                        }
                        sb.append( ((alias != null)? alias+"." : "") + df.name() + " = " + DatabaseUtils.sqlEscapeString(value + "") );
                    }
                }
            }
            return sb.toString();
        } catch(Exception e) {
            Util.loge("Repository GetWhereStatement Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    public List<T> findUnique(T item) {
        // get criteria statement for each field marked with unique = true
        String where = getUniqueWheres(item);

        // apply where clause to the query
        if(where != null) {
            String query = SqlBuilder.newInstance()
                    .select("*")
                    .from(TABLE_NAME, "t")
                    .where(where)
                    .getQuery();
            return findByQuery(query);

        }
        return new ArrayList<T>();
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

    protected List<T> cursorToList(String query) {
        init(classType);
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
            Util.loge("sqlite delete exception: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteBy(String col, Object val) {
        if(val.getClass().equals(String.class)) {
            val = "'" + val + "'";
        }

        int rows = db.delete(TABLE_NAME, col + " = " + val, null);
        return (rows > 0);
    }

    public void deleteAll() {
        List<T> list = findAll();
        for(T t : list) {
            delete(t);
        }
    }

    public int size() {
        return findAll().size();
    }
}
