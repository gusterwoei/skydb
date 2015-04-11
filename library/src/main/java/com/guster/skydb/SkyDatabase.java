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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Gusterwoei on 10/3/14.
 */
public abstract class SkyDatabase extends SQLiteOpenHelper {
    private static final String SQL_ON_FOREIGN_KEY = "PRAGMA foreign_keys=ON;";
    private static final String SQL_SYNC_MODE_NORMAL = "PRAGMA synchronous=OFF";

    // data
    private static Context context;
    private static SQLiteDatabase sqLiteDatabase;
    private static SkyDatabase mInglabSqliteOpenHelper;
    private static DatabaseHelper databaseHelper;

    public SkyDatabase(Context context, String databaseName, Integer databaseVersion) {
        super(context.getApplicationContext(), databaseName, null, databaseVersion);
        initialize(context);
    }

    private void initialize(Context ctx) {
        context = ctx.getApplicationContext();
        if(mInglabSqliteOpenHelper == null) {
            mInglabSqliteOpenHelper = this;
        }
    }

    public static SkyDatabase getInstance() {
        return mInglabSqliteOpenHelper;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL(SQL_SYNC_MODE_NORMAL);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if(databaseHelper == null)
            databaseHelper = new DatabaseHelper(db);
        onCreate(db, databaseHelper);
    }
    public abstract void onCreate(SQLiteDatabase db, DatabaseHelper dbHelper);


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        for(int version = 1; version <= i2; version++) {
            onMigrate(db, version, databaseHelper);
        }
    }
    public abstract void onMigrate(SQLiteDatabase db, int version, DatabaseHelper creator);


    private void createTable(Class<?> cls, SQLiteDatabase db) {
        StringBuilder schema = new StringBuilder();

        // get table name from class
        Table table = cls.getAnnotation(Table.class);
        if(table != null) {
            // get table name
            schema.append("CREATE TABLE IF NOT EXISTS " + table.name() + "(");
        }
        /*Table table = null;
        Constructor[] constructors = cls.getDeclaredConstructors();
        for(Constructor cont : constructors) {
            table = (Table) cont.getAnnotation(Table.class);
            if(table != null) {
                // get table name
                schema.append("CREATE TABLE IF NOT EXISTS " + table.name() + "(");
                break;
            }
        }*/

        // get table columns
        if(table != null) {
            List<Field> allFields = getInheritedDbFields(cls);
            List<String> uniqueColumns = new ArrayList<>();
            try {
                int count = 0;
                //String str = "";
                for(Field f : allFields) {
                    f.setAccessible(true);

                    Class fieldType = f.getType();
                    Column df = f.getAnnotation(Column.class);
                    if(df != null) {
                        String str = df.name() + " "; // get column name

                        // get column type
                        if(fieldType.equals(String.class)) {
                            str += "TEXT ";
                        } else if(fieldType.equals(Integer.TYPE)) {
                            str += "INTEGER ";
                        } else if(fieldType.equals(Long.TYPE)) {
                            str += "INTEGER ";
                        } else if(fieldType.equals(Double.TYPE)) {
                            str += "NUMERIC ";
                        } else if(fieldType.equals(Float.TYPE)) {
                            str += "NUMERIC ";
                        } else if(fieldType.equals(Boolean.TYPE)) {
                            str += "INTEGER ";
                        } else {
                            str += "TEXT ";
                        }

                        // primary key?
                        if(df.primaryKey())
                            str += "PRIMARY KEY ";

                        // auto increment?
                        if(df.autoIncrement())
                            str += "AUTOINCREMENT ";

                        // not null?
                        if(df.notNull())
                            str += "NOT NULL ";

                        // unique column
                        if(df.unique())
                            uniqueColumns.add(df.name());

                        if(count < allFields.size() - 1) {
                            str += ", ";
                        }

                        schema.append(str);
                    }

                    count++;
                }

                // add unique columns
                if(!uniqueColumns.isEmpty()) {
                    String str = ", UNIQUE(";
                    for(int i=0; i<uniqueColumns.size(); i++) {
                        String col = uniqueColumns.get(i);
                        str += col;
                        if(i < uniqueColumns.size()-1)
                            str += ", ";
                    }
                    str += ")";
                    schema.append(str);
                }

                // close bracket
                schema.append(")");

            } catch(Exception e) {
                Log.e("ABC", getClass().getSimpleName() + ": CreateSchema Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // no table name, error
            Log.e("ABC", getClass().getSimpleName() + ": No Table Name found");
        }

        db.execSQL(schema.toString());
    }

    private void deleteTable(Class<?> cls, SQLiteDatabase db) {
        // get table name
        Table table = cls.getAnnotation(Table.class);
        if(table != null) {
            getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + table.name());
        }
    }

    public SQLiteDatabase createDatabase() {
        if(sqLiteDatabase == null) {
            sqLiteDatabase = getWritableDatabase();
        }
        return sqLiteDatabase;
    }

    public Context getContext() {
        return context;
    }

    private List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private List<Field> getInheritedDbFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for(Field f : getInheritedFields(type)) {
            if(f.getAnnotation(Column.class) != null) {
                fields.add(f);
            }
        }
        return fields;
    }


    /**
     * Database Helper
     */
    protected class DatabaseHelper {
        private SQLiteDatabase db;
        public DatabaseHelper(SQLiteDatabase db) {
            this.db = db;
        }

        public void createTable(Class<?> cls) {
            SkyDatabase.this.createTable(cls, db);
        }

        private void alterTable(Class<?> cls) {
            // todo: coming soon
        }

        public void deleteTable(Class<?> cls) {
            SkyDatabase.this.deleteTable(cls, db);
        }
    }
}
