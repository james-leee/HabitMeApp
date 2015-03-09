package com.example.cs65project.habitme;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Post table is to store all post items in database
 */
public class PostsTable {
    public static final String DATABASE_NAME = "entry.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_ENTRIES = "posts";
    public static final String KEY_ROWID = "key_row_id";
    public static final String KEY_USERNAME = "key_user_name";
    public static final String KEY_USERIMAGE = "key_user_image";
    public static final String KEY_POST_TITLE = "key_post_title";
    public static final String KEY_POST_CONTENT = "key_post_content";
    public static final String KEY_POST_IMAGE = "key_post_image";
    public static final String KEY_POST_LOCATION = "key_post_location";
    public static final String KEY_POST_PRIVACY = "key_post_privacy";
    public static final String KEY_POST_ID = "key_post_id";


    // SQL query to create the table for the first time
    // Data types are defined below
    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME_ENTRIES
            + " ("
            + KEY_ROWID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_USERNAME
            + " TEXT, "
            + KEY_USERIMAGE
            + " TEXT, "
            + KEY_POST_TITLE
            + " TEXT NOT NULL, "
            + KEY_POST_CONTENT
            + " TEXT, "
            + KEY_POST_IMAGE
            + " TEXT, "
            + KEY_POST_LOCATION
            + " TEXT, "
            + KEY_POST_PRIVACY
            + " INTEGER NOT NULL, "
            + KEY_POST_ID
            + " INTEGER NOT NULL);";

    public static void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(CREATE_TABLE_ENTRIES);
        }catch (Exception e){

        }
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(PostsTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS "+PostsTable.TABLE_NAME_ENTRIES);
        onCreate(database);
    }
}
