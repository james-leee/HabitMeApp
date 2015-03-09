package com.example.cs65project.habitme;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by haominzhang on 15/3/8.
 */
public class CommentTable {
    public static final String DATABASE_NAME = "entry.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_ENTRIES = "comments";
    public static final String KEY_ROWID = "key_row_id";
    public static final String KEY_POSTID = "key_comment_postid";
    public static final String KEY_USER_NAME = "key_comment_username";
    public static final String KEY_CONTENT = "key_comment_content";

    // SQL query to create the table for the first time
    // Data types are defined below
    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME_ENTRIES
            + " ("
            + KEY_ROWID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_POSTID
            + " TEXT NOT NULL, "
            + KEY_USER_NAME
            + " TEXT NOT NULL, "
            + KEY_CONTENT
            + " TEXT NOT NULL);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_ENTRIES);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(HistoryTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + CommentTable.TABLE_NAME_ENTRIES);
        onCreate(database);
    }
}
