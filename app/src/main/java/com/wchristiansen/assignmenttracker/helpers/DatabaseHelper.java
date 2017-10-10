package com.wchristiansen.assignmenttracker.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.wchristiansen.assignmenttracker.BaseApplication;
import com.wchristiansen.assignmenttracker.models.AdapterItem;
import com.wchristiansen.assignmenttracker.models.Assignment;
import com.wchristiansen.assignmenttracker.models.Course;
import com.wchristiansen.assignmenttracker.models.SubAssignment;
import com.wchristiansen.assignmenttracker.utils.MockDataUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author will
 * @version 9/27/17
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    // Database Names
    private static final String DATABASE_NAME = "assignment_track_db";
    private static final String DATABASE_PATH = "//data//com.wchristiansen.assignmenttracker//databases//" + DATABASE_NAME;
    // Table Names
    private interface Table {
        String COURSES = "courses";
        String ASSIGNMENTS = "assignments";
    }
    // Column Names
    private interface Column {
        String ID = "_id";
        String TITLE = "title";
        String NICKNAME = "nickname";
        String IS_HIDDEN = "is_hidden";
        String IS_COMPLETE = "is_complete";
        String DUE_DATE = "due_date";
        String COURSE_ID = "course_id";
        String PARENT_ASSIGNMENT_ID = "parent_assignment_id";
    }

    private boolean useMockData = false;
    private Calendar calender;

    public DatabaseHelper(Context context, boolean useMockData) {
        super(context, DATABASE_NAME , null, 1);
        this.useMockData = useMockData;
        this.calender = Calendar.getInstance();
        Log.d(TAG, "DatabaseHelper was configured to 'useMockData'? " + useMockData);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the Course table if necessary
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Table.COURSES + "(" +
                Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Column.TITLE + " VARCHAR, " +
                Column.NICKNAME + " VARCHAR, " +
                Column.IS_HIDDEN + " BOOL, " +
                "UNIQUE (" + Column.TITLE + ") " +
                "ON CONFLICT REPLACE);");

        // Create the Assignment table if necessary
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Table.ASSIGNMENTS + "(" +
                Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Column.TITLE + " VARCHAR, " +
                Column.IS_COMPLETE + " BOOL, " +
                Column.IS_HIDDEN + " BOOL, " +
                Column.DUE_DATE + " INTEGER, " +
                Column.COURSE_ID + " INTEGER, " +
                Column.PARENT_ASSIGNMENT_ID + " INTEGER, " +
                "UNIQUE(" + Column.TITLE + ", " + Column.COURSE_ID + ", " + Column.PARENT_ASSIGNMENT_ID + ") " +
                "ON CONFLICT REPLACE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table.COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + Table.ASSIGNMENTS);
        onCreate(db);
    }

    public boolean resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(Table.ASSIGNMENTS, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + Table.ASSIGNMENTS + "'");

        db.delete(Table.COURSES, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + Table.COURSES + "'");

        return true;
    }

    public File backupDatabase() throws IOException {
        File sdDirectory = Environment.getExternalStorageDirectory();
        File dataDirectory = Environment.getDataDirectory();

        if (sdDirectory.canWrite()) {
            DateFormat df = new SimpleDateFormat("yyyy_dd_FF_HH_mm", Locale.getDefault());
            String formattedDate = df.format(calender.getTime());
            String backupPath = BaseApplication.getDatabaseBackupPath() + DATABASE_NAME + "_" + formattedDate + ".db";

            File appDirectory = new File(sdDirectory, BaseApplication.getDatabaseBackupPath());
            if(appDirectory.exists() || appDirectory.mkdirs()) {
                File currentDB = new File(dataDirectory, DATABASE_PATH);
                File backupDB = new File(sdDirectory, backupPath);

                Log.d("DatabaseHelper", "Db Path:     " + currentDB.getPath() + " (" + currentDB.exists() + ")");
                Log.d("DatabaseHelper", "Backup Path: " + backupDB.getPath() + " (" + backupDB.exists() + ")");

                copyFile(new FileInputStream(currentDB), new FileOutputStream(backupDB));
                return backupDB;
            }
        }
        return null;
    }

    public void importDatabase(String importDbPath) throws IOException {
        // Close the SQLiteOpenHelper so it will commit the created empty database to internal storage.
        close();

        File newDb = new File(importDbPath);
        File oldDb = new File("/data" + DATABASE_PATH);
        if (newDb.exists()) {
            copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
            // Access the copied database so SQLiteHelper will cache it and mark it as created.
            getWritableDatabase().close();
        }
    }

    public Course insertOrUpdateCourse(Course course) {
        return insertOrUpdateCourse(course, getWritableDatabase());
    }

    private Course insertOrUpdateCourse(Course course, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TITLE, course.getName());
        contentValues.put(Column.NICKNAME, course.getNickname());
        contentValues.put(Column.IS_HIDDEN, course.isHidden());

        if(course.getId() != -1) {
            updateItem(db, course.getId(), Table.COURSES, contentValues, "--|");
        } else {
            long id = insertItem(db, Table.COURSES, contentValues, "--|");
            course.setId(id);
        }

        return course;
    }

    public SubAssignment insertOrUpdateSubAssignment(SubAssignment subAssignment) {
        return (SubAssignment)insertOrUpdateAssignment(subAssignment);
    }

    public Assignment insertOrUpdateAssignment(Assignment assignment) {
        return insertOrUpdateAssignment(assignment, getWritableDatabase());
    }

    private Assignment insertOrUpdateAssignment(Assignment assignment, SQLiteDatabase db) {
        long parentAssignmentId = -1;
        if(assignment instanceof SubAssignment) {
            parentAssignmentId = ((SubAssignment)assignment).getParentAssignment().getId();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.TITLE, assignment.getTitle());
        contentValues.put(Column.IS_COMPLETE, assignment.isComplete());
        contentValues.put(Column.IS_HIDDEN, assignment.isHidden());
        contentValues.put(Column.DUE_DATE, normalizeDate(assignment.getDueDate()).getTime());
        contentValues.put(Column.COURSE_ID, assignment.getCourseId());
        contentValues.put(Column.PARENT_ASSIGNMENT_ID, parentAssignmentId);

        String logStart = parentAssignmentId == -1 ? "----|" : "------|";
        if(assignment.getId() != -1) {
            updateItem(db, assignment.getId(), Table.ASSIGNMENTS, contentValues, logStart);
        } else {
            long id = insertItem(db, Table.ASSIGNMENTS, contentValues, logStart);
            assignment.setId(id);
        }

        return assignment;
    }

    public void batchItemUpdate(List<AdapterItem> itemList) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            for(AdapterItem item : itemList) {
                if(item instanceof Course) {
                    insertOrUpdateCourse((Course)item);
                }
                else if(item instanceof Assignment) {
                    insertOrUpdateAssignment((Assignment)item);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private long insertItem(SQLiteDatabase db, String table, ContentValues contentValues, String logStart) {
        long id = db.insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, logStart + " insert (" + id + "): " + contentValues.toString());
        return id;
    }

    private void updateItem(SQLiteDatabase db, long id,String table, ContentValues contentValues, String logStart) {
        db.update(table, contentValues, Column.ID + "=?", new String[] { id + "" });
        Log.d(TAG, logStart + " update (" + id + "): " + contentValues.toString());
    }

    private Date normalizeDate(Date date) {
        calender.setTime(date);
        calender.set(Calendar.HOUR, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);
        return calender.getTime();
    }

    public boolean removeAssignment(Assignment assignment) {
        if(useMockData) {
            return false;
        }

        long parentAssignmentId = -1;
        if(assignment instanceof SubAssignment) {
            parentAssignmentId = ((SubAssignment)assignment).getParentAssignment().getId();
        }

        SQLiteDatabase db = getWritableDatabase();

        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(assignment.getId());

        if(assignment.hasSubAssignments()) {
            for(SubAssignment subAssignment : assignment.getSubAssignmentList()) {
                idsToDelete.add(subAssignment.getId());
            }
        }

        String args = TextUtils.join(", ", idsToDelete);
        db.execSQL(String.format("DELETE FROM " + Table.ASSIGNMENTS + " WHERE " + Column.ID + " IN (%s);", args));

        if(parentAssignmentId == -1) {
            Log.d(TAG, "----| delete (" + idsToDelete.size() + "): " + assignment.toString());
        } else {
            Log.d(TAG, "------| delete (" + idsToDelete.size() + "): " + assignment.toString());
        }

        return true;
    }

    public List<Course> getCourses() {
        if(useMockData) {
            Log.d(TAG, "'useMockData' was set to true; generating mock data and not using ");
            return MockDataUtil.generateMockCourses(this);
        } else {
            List<Course> courseList = getCourseList(getReadableDatabase());

            int courseSize = 0, assignmentSize = 0, subAssignmentSize = 0;
            for(Course c : courseList) {
                for(Assignment a : c.getAssignmentList()) {
                    subAssignmentSize += a.getSubAssignmentList().size();
                    assignmentSize++;
                }
                courseSize++;
            }
            Log.d(TAG, String.format("Successfully read in %d courses, %d assignments and %d " +
                    "sub-assignments.", courseSize, assignmentSize, subAssignmentSize));
            return courseList;
        }
    }

    @SuppressWarnings("unused")
    public void saveCourses(List<Course> courseList) {
        for(Course c : courseList) {
            c = insertOrUpdateCourse(c);
            for(Assignment a : c.getAssignmentList()) {
                a = insertOrUpdateAssignment(a);
                for(SubAssignment s : a.getSubAssignmentList()) {
                    insertOrUpdateAssignment(s);
                }
            }
        }
    }

    private List<Course> getCourseList(SQLiteDatabase db) {
        List<Course> courseList = new ArrayList<>();
        // Get all courses in the database and fill in their assignments/sub-assignments as they are
        // created. Finding each top-level assignment linked to the current Course and adding the
        // necessary data from there.
        try(Cursor cursor = db.query(Table.COURSES, null, null, null, null, null, null, null)) {
            cursor.moveToFirst();

            Course course;
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(Column.ID));
                String title = cursor.getString(cursor.getColumnIndex(Column.TITLE));
                String nickname = cursor.getString(cursor.getColumnIndex(Column.NICKNAME));
                boolean isHidden = cursor.getInt(cursor.getColumnIndex(Column.IS_HIDDEN)) > 0;

                course = new Course(id, title, nickname, isHidden);
                course.setAssignmentList(getAssignmentListForCourse(db, course));

                courseList.add(course);
                cursor.moveToNext();
            }
        }
        return courseList;
    }

    private List<Assignment> getAssignmentListForCourse(SQLiteDatabase db, Course course) {
        List<Assignment> assignmentList = new ArrayList<>();

        // Get all the top-level assignments that below to the Course parameter that was passed in.
        // Each Assignment that was found to match the Course then attempts to locate any
        // sub-assignments that might be in the database and links them to the Assignment object
        String idString = String.valueOf(course.getId());
        String where = Column.COURSE_ID + "=? AND " + Column.PARENT_ASSIGNMENT_ID + "=?";
        String[] whereArgs = new String[] { idString, "-1" };
        try(Cursor cursor = db.query(Table.ASSIGNMENTS, null, where, whereArgs, null, null, Column.DUE_DATE)) {
            cursor.moveToFirst();

            Assignment assignment;
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(Column.ID));
                String title = cursor.getString(cursor.getColumnIndex(Column.TITLE));
                boolean isComplete = cursor.getInt(cursor.getColumnIndex(Column.IS_COMPLETE)) > 0;
                boolean isHidden = cursor.getInt(cursor.getColumnIndex(Column.IS_HIDDEN)) > 0;
                Date dueDate = new Date(cursor.getLong(cursor.getColumnIndex(Column.DUE_DATE)));

                assignment = new Assignment(id, course.getId(), title, isComplete, isHidden, dueDate);
                assignment.setSubAssignmentList(getSubAssignmentListForAssignment(db, assignment));

                assignmentList.add(assignment);
                cursor.moveToNext();
            }
        }
        return assignmentList;
    }

    private List<SubAssignment> getSubAssignmentListForAssignment(SQLiteDatabase db,
                                                                  Assignment assignment) {
        List<SubAssignment> subAssignmentList = new ArrayList<>();

        // Get all the sub-assignment objects that are associated with a certain top-level
        // Assignment object. Only the id, title and isComplete columns are needed since the other
        // data will be inherited from the top-level Assignment object.
        String idString = String.valueOf(assignment.getId());
        String where = Column.PARENT_ASSIGNMENT_ID + "=?";
        String[] selection = new String[] { Column.ID, Column.TITLE, Column.IS_COMPLETE };
        try(Cursor cursor = db.query(Table.ASSIGNMENTS, selection, where, new String[] { idString }, null, null, null)) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(Column.ID));
                String title = cursor.getString(cursor.getColumnIndex(Column.TITLE));
                boolean isComplete = cursor.getInt(cursor.getColumnIndex(Column.IS_COMPLETE)) > 0;

                subAssignmentList.add(new SubAssignment(id, assignment, title, isComplete, false));
                cursor.moveToNext();
            }
        }
        return subAssignmentList;
    }

    private void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }
}
