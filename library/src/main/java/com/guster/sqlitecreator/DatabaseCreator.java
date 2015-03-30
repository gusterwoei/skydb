/*******************************************************************************
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
 *******************************************************************************/

package com.guster.sqlitecreator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.guster.sqlitecreator.annotation.Column;
import com.guster.sqlitecreator.annotation.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Gusterwoei on 10/3/14.
 */
public abstract class DatabaseCreator extends SQLiteOpenHelper {
    private static final String SQL_ON_FOREIGN_KEY = "PRAGMA foreign_keys=ON;";
    private static final String SQL_SYNC_MODE_NORMAL = "PRAGMA synchronous=OFF";

    // data
    private static Context context;
    private static SQLiteDatabase sqLiteDatabase;
    private static DatabaseCreator mInglabSqliteOpenHelper;

    public DatabaseCreator(Context context, String databaseName, Integer databaseVersion) {
        super(context.getApplicationContext(), databaseName, null, databaseVersion);
        initialize(context);
    }

    private void initialize(Context ctx) {
        context = ctx.getApplicationContext();
        if(mInglabSqliteOpenHelper == null) {
            mInglabSqliteOpenHelper = getDatabase();
        }
    }

    protected abstract DatabaseCreator getDatabase();

    public static DatabaseCreator getInstance() {
        return mInglabSqliteOpenHelper;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL(SQL_SYNC_MODE_NORMAL);
        onOpenDb(db);
    }
    public abstract void onOpenDb(SQLiteDatabase db);


    @Override
    public void onCreate(SQLiteDatabase db) {
        onCreateDb(db);
    }
    public abstract void onCreateDb(SQLiteDatabase db);


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        for(int version = 1; version <= i2; version++) {
            onUpgradeDb(db, i2);
        }
    }
    public abstract void onUpgradeDb(SQLiteDatabase db, int newVersion);


    public void createSchemaFor(Class<?> cls, SQLiteDatabase db) {
        StringBuilder schema = new StringBuilder();

        // get constructors
        Table table = null;
        Constructor[] constructors = cls.getDeclaredConstructors();
        for(Constructor cont : constructors) {
            table = (Table) cont.getAnnotation(Table.class);
            if(table != null) {
                // get table name
                schema.append("CREATE TABLE IF NOT EXISTS " + table.name() + "(");
                break;
            }
        }

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

                // add primary key(s)
                /*if(primaryKeyCols.size() > 0) {
                    str += ", ";
                    str += "PRIMARY KEY (";
                    for(int i=0; i<primaryKeyCols.size(); i++) {
                        String keyCol = primaryKeyCols.get(i);
                        str += keyCol;

                        if(i < primaryKeyCols.size() - 1) {
                            str += ", ";
                        }
                    }
                    str += ")";
                }*/

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

    public void deleteSchema(Class<?> cls) {
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
}
