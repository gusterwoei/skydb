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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final String PREF_NAME = "SkyDatabasePref";
    private static final String PREF_VAL_FRESH_INSTALL = "pref val ft";

    // data
    private static Context context;
    private static SQLiteDatabase sqLiteDatabase;
    private static SkyDatabase mInglabSqliteOpenHelper;
    private static DatabaseHelper databaseHelper;
    private static int currentVersion;

    public static SkyDatabase getInstance() {
        return mInglabSqliteOpenHelper;
    }

    public SkyDatabase(Context context, String databaseName, Integer databaseVersion) {
        super(context.getApplicationContext(), databaseName, null, databaseVersion);
        initialize(context);
        currentVersion = databaseVersion;
    }

    private void initialize(Context ctx) {
        context = ctx.getApplicationContext();
        if(mInglabSqliteOpenHelper == null) {
            mInglabSqliteOpenHelper = this;
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

    protected int getDatabaseFirstVersion() {
        return 1;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL(SQL_SYNC_MODE_NORMAL);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(databaseHelper == null)
            databaseHelper = new DatabaseHelper(db);
        onCreate(db, databaseHelper);

        // ensure that fresh installed user gets the upgrade
        final SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        final boolean isFreshInstall = sp.getBoolean(PREF_VAL_FRESH_INSTALL, true);
        if(isFreshInstall) {
            sp.edit().putBoolean(PREF_VAL_FRESH_INSTALL, false).apply();
            onUpgrade(db, getDatabaseFirstVersion(), currentVersion);
        }
    }
    public abstract void onCreate(SQLiteDatabase db, DatabaseHelper dbHelper);


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        if(databaseHelper == null)
            databaseHelper = new DatabaseHelper(db);
        for(int version = (i+1); version <= i2; version++) {
            onMigrate(db, version, databaseHelper);
        }
    }
    public abstract void onMigrate(SQLiteDatabase db, int version, DatabaseHelper creator);

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    private String createTable(Class<?> cls, SQLiteDatabase db, boolean executeCreation) {
        StringBuilder schema = new StringBuilder();

        // get table name from class
        Table table = cls.getAnnotation(Table.class);
        if(table != null) {
            // get table name
            schema.append("CREATE TABLE IF NOT EXISTS " + table.name() + "(");
        }

        // get table columns
        if(table != null) {
            List<Field> allFields = getInheritedDbFields(cls);
            List<String> uniqueColumns = new ArrayList<>();
            try {
                int count = 0;
                //String str = "";
                for(Field field : allFields) {
                    field.setAccessible(true);

                    Class fieldType = field.getType();
                    Column col = field.getAnnotation(Column.class);
                    if(col != null) {

                        String str = constructColumnStmnt(fieldType, col, uniqueColumns);

                        /*
                        String str = col.name() + " "; // get column name

                        // get column type
                        if(fieldType.equals(String.class)) {
                            str += "TEXT ";
                        } else if(fieldType.equals(Integer.TYPE) || fieldType.equals(Integer.class)) {
                            str += "INTEGER ";
                        } else if(fieldType.equals(Long.TYPE)  || fieldType.equals(Long.class)) {
                            str += "INTEGER ";
                        } else if(fieldType.equals(Double.TYPE)  || fieldType.equals(Double.class)) {
                            str += "NUMERIC ";
                        } else if(fieldType.equals(Float.TYPE)  || fieldType.equals(Float.class)) {
                            str += "NUMERIC ";
                        } else if(fieldType.equals(Boolean.TYPE)  || fieldType.equals(Boolean.class)) {
                            str += "INTEGER ";
                        } else {
                            str += "TEXT ";
                        }

                        // primary key?
                        if(col.primaryKey())
                            str += "PRIMARY KEY ";

                        // auto increment?
                        if(col.autoIncrement())
                            str += "AUTOINCREMENT ";

                        // not null?
                        if(col.notNull())
                            str += "NOT NULL ";

                        // unique column
                        if(col.unique())
                            uniqueColumns.add(col.name());*/

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
                Util.loge(getClass().getSimpleName() + ": CreateSchema Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // no table name, error
            Util.loge(getClass().getSimpleName() + ": No Table Name found");
        }

        String query = schema.toString();

        if(executeCreation) {
            db.execSQL(query);
        }
        
        return query;
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

    private List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private String constructColumnStmnt(Class fieldType, Column col, List<String> uniqueColumns) {
        // get column name
        String str = col.name() + " ";

        // get column type
        if(fieldType.equals(String.class)) {
            str += "TEXT ";
        } else if(fieldType.equals(Integer.TYPE) || fieldType.equals(Integer.class)) {
            str += "INTEGER ";
        } else if(fieldType.equals(Long.TYPE)  || fieldType.equals(Long.class)) {
            str += "INTEGER ";
        } else if(fieldType.equals(Double.TYPE)  || fieldType.equals(Double.class)) {
            str += "NUMERIC ";
        } else if(fieldType.equals(Float.TYPE)  || fieldType.equals(Float.class)) {
            str += "NUMERIC ";
        } else if(fieldType.equals(Boolean.TYPE)  || fieldType.equals(Boolean.class)) {
            str += "INTEGER ";
        } else {
            str += "TEXT ";
        }

        // primary key?
        if(col.primaryKey())
            str += "PRIMARY KEY ";

        // auto increment?
        if(col.autoIncrement())
            str += "AUTOINCREMENT ";

        // not null?
        if(col.notNull())
            str += "NOT NULL ";

        // default value?
        if(!col.defaultValue().isEmpty()) {
            str += "DEFAULT " + DatabaseUtils.sqlEscapeString(col.defaultValue()) + " ";
        }

        // unique column
        if(col.unique())
            uniqueColumns.add(col.name());

        return str;
    }

    private void deleteTable(Class<?> cls, SQLiteDatabase db) {
        // get table name
        Table table = cls.getAnnotation(Table.class);
        if(table != null) {
            db.execSQL("DROP TABLE IF EXISTS " + table.name());
        }
    }

    /**
     * Database Helper
     */
    protected class DatabaseHelper {
        private SQLiteDatabase db;
        public DatabaseHelper(SQLiteDatabase db) {
            this.db = db;
        }

        public void createTable(Class<?> targetTable) {
            SkyDatabase.this.createTable(targetTable, db, true);
        }

        public void addAllColumns(Class<?> targetTable) throws NoSuchFieldException {
            // get table name
            Table table = targetTable.getAnnotation(Table.class);
            String tableName = table.name();

            List<Field> fields = getInheritedDbFields(targetTable);
            for(Field field : fields) {
                field.setAccessible(true);

                Column col = field.getAnnotation(Column.class);

                if(!checkIfColumnExist(db, tableName, col.name())) {
                    addColumn(targetTable, field.getName());
                }
            }
        }

        public void addColumn(Class<?> targetTable, String fieldName) throws NoSuchFieldException {
            // get table name
            Table table = targetTable.getAnnotation(Table.class);
            String tableName = table.name();

            Field field = targetTable.getDeclaredField(fieldName);
            field.setAccessible(true);

            Column col = field.getAnnotation(Column.class);

            if(col != null) {
                boolean columnExist = checkIfColumnExist(db, tableName, fieldName);
                if(columnExist) {
                    Util.logd("Column '" + col.name() + "' already exists in table " + tableName + ", aborting operation...");
                    return;
                }

                Class fieldType = field.getType();
                List<String> uniqueColumns = new ArrayList<>();

                // add new column
                String str = constructColumnStmnt(fieldType, col, uniqueColumns);
                str = "ALTER TABLE " + tableName + " ADD COLUMN " + str;
                db.execSQL(str);

                //Util.logd("Alter Table statement: " + str);

                // create unique index if any
                if(!uniqueColumns.isEmpty()) {
                    String indexName = "sky_uniq_" + col.name();
                    String uniqStr = "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(";

                    for(int i = 0; i<uniqueColumns.size(); i++) {
                        uniqStr += uniqueColumns.get(i);
                        if(i < uniqueColumns.size() - 1) {
                            uniqStr += ", ";
                        }
                    }
                    uniqStr += ")";
                    db.execSQL(uniqStr);

                    //Util.logd("Create Unique Index statement: " + uniqStr);
                }

                uniqueColumns.clear();
            }
        }

        public void renameTable(Class<?> targetTable, String newTableName) {
            // get table name
            Table table = targetTable.getAnnotation(Table.class);
            String tableName = table.name();

            String stmnt = "ALTER TABLE " + tableName + " RENAME TO " + newTableName;
            db.execSQL(stmnt);
        }

        /*public void alterTable(Class<?> cls) throws NoSuchFieldException {
            // get table name
            Table table = cls.getAnnotation(Table.class);
            String tableName = table.name();

            db.beginTransactionNonExclusive();
            db.execSQL("PRAGMA writable_schema = 1;");

            String createQuery = SkyDatabase.this.createTable(cls, db, false);
            createQuery = "UPDATE SQLITE_MASTER SET SQL = \"" + createQuery + "\" WHERE NAME = '" + tableName + "';";
            db.execSQL(createQuery);

            Util.logd("Alter Table query: " + createQuery);

            db.execSQL("PRAGMA writable_schema = 0;");
            db.setTransactionSuccessful();
            db.endTransaction();
        }*/

        public void deleteTable(Class<?> cls) {
            SkyDatabase.this.deleteTable(cls, db);
        }

        private boolean checkIfColumnExist(SQLiteDatabase db, String tableName, String columnName) {
            String query = "PRAGMA TABLE_INFO(" + tableName + ")";
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, null);

                cursor.moveToFirst();
                do {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    //Util.logd("- " + name);
                    if(columnName.equals(name)) {
                        cursor.close();
                        return true;
                    }

                } while(cursor.moveToNext());

            } finally {
                if(cursor != null)
                    cursor.close();
            }

            return false;
        }
    }
}
