package com.example.cs65project.habitme;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Database table for all habits
 */

public class HistoryTable {
    //class for database table
    public static final String DATABASE_NAME = "entry.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_ENTRIES = "entries";
    public static final String KEY_ROWID = "key_row_id";
    public static final String KEY_HABIT_TITLE = "key_habit_title";
    public static final String KEY_FREQUENCY = "key_frequency";
    public static final String KEY_CHECK_TIME_LIST = "key_check_time_list";
    public static final String KEY_CREATE_TYPE = "key_create_type";
    public static final String KEY_CHOOSE_POSITION = "key_choose_position";
    public static final String KEY_USE_NUM = "key_use_num";
    public static final String KEY_TIME_LENGTH = "key_time_length";
    public static final String KEY_LOCATION = "key_location";

    // SQL query to create the table for the first time
    // Data types are defined below
    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME_ENTRIES
            + " ("
            + KEY_ROWID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_HABIT_TITLE
            + " TEXT NOT NULL, "
            + KEY_FREQUENCY
            + " INTEGER NOT NULL, "
            + KEY_CHECK_TIME_LIST
            + " TEXT NOT NULL, "
            + KEY_CREATE_TYPE
            + " INTEGER NOT NULL, "
            + KEY_CHOOSE_POSITION
            + " INTEGER NOT NULL, "
            + KEY_USE_NUM
            + " INTEGER NOT NULL, "
            + KEY_TIME_LENGTH
            + " TEXT NOT NULL, "
            + KEY_LOCATION
            + " BLOB );";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_ENTRIES);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(HistoryTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME_ENTRIES);
        onCreate(database);
    }
}
