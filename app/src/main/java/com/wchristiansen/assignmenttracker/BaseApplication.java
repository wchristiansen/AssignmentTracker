package com.wchristiansen.assignmenttracker;

import android.app.Application;

import com.wchristiansen.assignmenttracker.helpers.DatabaseHelper;

/**
 * @author will
 * @version 9/27/17
 */
public class BaseApplication extends Application {

    private static BaseApplication app;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
        initializeDatabase();
    }

    private void initializeDatabase() {
        boolean useMockData = getResources().getBoolean(R.bool.use_mock_data);
        databaseHelper = new DatabaseHelper(this, useMockData);
    }

    public static BaseApplication getApp() {
        return app;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return app.databaseHelper;
    }

    public static String getAppName() {
        return app.getResources().getString(R.string.app_name);
    }

    public static String getDatabaseBackupPath() {
        return getAppName() + "/backups/";
    }
}
