# Sky Database
Sky Database is a powerful Android ORM library suitable for any project. It aims for easy
integration and high extensibility.

## Installation
Include the following dependency in your build.gradle file of your project.
```xml
repositories {
    jcenter()
}

dependencies {
    compile 'com.guster:skydb:2.0.6'
}
...
```

## How to use
Follow the 3 simple steps

### 1. Create a database class
First and foremost, you will need to create a class that extends SkyDatabase class.
SkyDatabase is a direct subclass of SQLiteOpenHelper. Meaning that you can easily perform
whatever functions in SkyDatabase as you did in SQLiteOpenHelper.
SkyDatabase will provide the following two functions: onCreate() and onMigrate().
Then, use DatabaseHelper.createTable() to create an entity table.

##### Example

```java
public class MyDatabase extends SkyDatabase {

    public MyDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db, DatabaseHelper helper) {
        helper.createTable(Student.class);
        helper.createTable(Lecturer.class);
        helper.createTable(Subject.class);
        helper.createTable(Attendance.class);
    }

    @Override
    public void onMigrate(SQLiteDatabase db, int version, DatabaseHelper helper) {
        switch(version) {
            case 1:
                break;
            case 2:
                SqlBuilder query = SqlBuilder.newInstance()
                        .alterTable(Student.TABLE_NAME)
                        .addColumn("status", "integer")
                        .build();
                db.execSQL(query.getQuery());
                break;
            // and so on...
        }
    }
}
```
Create database in your app

```java
public class MainActivity extends FragmentActivity {
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            MyDatabase myDatabase = new MyDatabase(this, "university.db", 1);
            myDatabase.createDatabase();

            ...
        }
}
```

### 2. Create a table
Skydb uses Java annotation to automatically create a Table. There are 2 types of Annotation:

```java
@Table // to specify a table name
@Column // to specify a column name and other constraints
```

##### Example

```java
@Table(name = "lecturers")
public class Lecturer {
    public static final String TABLE_NAME = "lecturers";
    public static final String COL_ID = "_id";
    public static final String COL_FIRST_NAME = "fname";
    public static final String COL_LAST_NAME = "lname";
    public static final String COL_LECTURER_ID = "lecturerId";
    public static final String COL_CREATED_DATE = "createdDate";
    public static final String COL_MODIFIED_DATE = "modifiedDate";

    @Column(column = COL_ID, unique = true)
    private long _id;
    @Column(name = COL_FIRST_NAME, notNull = true)
    private String firstName;
    @Column(name = COL_LAST_NAME, notNull = true)
    private String lastName;
    @Column(name = COL_LECTURER_ID, primaryKey = true, notNull = true)
    private String lecturerId;
    @Column(name = COL_CREATED_DATE, notNull = true)
    private long createdDate;
    @Column(name = COL_MODIFIED_DATE, notNull = true)
    private long modifiedDate;

    public Lecturer() {}
    ...
}
```

### 3. Create a DAO
Finally, you will need a DAO (Data Access Object) for each entity to read/write data from/to the database.
Simply create a class that extends Repository.
##### Example

```java
public class LecturerRepository extends Repository<Lecturer> {
    public LecturerRepository(Context context) {
        super(context, Lecturer.class);
    }
    ...
}
```

##### Example Usage
Repository provides a list of useful methods that you can use to interact with your entity table,
below are some examples:

```java
LecturerRepository lecturerRepository = new LecturerRepository(getApplicationContext());

// find by primary key
Lecturer lecturer = lecturerRepository.findOne("LECID001");

// find by columns that are marked as 'UNIQUE'
Lecturer criteria = new Lecturer();
criteria.setFirstName("Bryan");
criteria.setLastName("Keith");
Lecturer lecturer = lecturerRepository.findUnique(criteria);

// find all lecturers
List<Lecturer> lecturers = lecturerRepository.findAll();

// find by column 'name'
List<Lecturer> lecturers = lecturerRepository.findBy("name", "simon keith");

// save a lecturer
lecturerRepository.save(lecturer);

// save a list of lecturers, use this for batch saving to greatly improve the speed and performance
lecturerRepository.saveAll(lecturers);

// delete an existing lecturer by primary key
lecturerRepository.delete(lecturer);
```
The rest of the methods are self-explanatory, you can find in Repository.java

Of course, you can always create your own method to match your requirements, for example:

```java
public class LecturerRepository extends Repository<Lecturer> {

    public LecturerRepository(Context context) {
        super(context, Lecturer.class);
    }

    public List<Lecturer> findWithNameSimon() {
        SqlBuilder sqlBuilder = SqlBuilder.newInstance()
                .select("*")
                .from("lecturers", "l")
                .where("fname = :queryName")
                .orWhere("lname = :queryName")
                .bindValue("queryName", "simon")
                .build();
        return cursorToList(sqlBuilder.getQuery());
    }
}
```

# Developed by
* Guster Woei - <gusterwoei@gmail.com>

# License
```xml
 Copyright 2015 Gusterwoei

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```
