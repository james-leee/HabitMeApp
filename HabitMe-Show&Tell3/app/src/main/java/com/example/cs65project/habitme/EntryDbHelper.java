package com.example.cs65project.habitme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * EntryDbHelper to create and update database
 */
public class EntryDbHelper extends SQLiteOpenHelper {
    /**
     * Constructor
     * @param context
     */
    public EntryDbHelper(Context context) {
        // DATABASE_NAME is, of course the name of the database, which is defined as a tring constant
        // DATABASE_VERSION is the version of database, which is defined as an integer constant
        super(context, HistoryTable.DATABASE_NAME, null, HistoryTable.DATABASE_VERSION);
    }

    /**
     * Create table schema if not exists
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryTable.CREATE_TABLE_ENTRIES);
        db.execSQL(PostsTable.CREATE_TABLE_ENTRIES);
        db.execSQL(CommentTable.CREATE_TABLE_ENTRIES);
    }

    /**
     * Upgrade database version
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HistoryTable.onUpgrade(db,oldVersion,newVersion);
        PostsTable.onUpgrade(db,oldVersion,newVersion);
        CommentTable.onUpgrade(db,oldVersion,newVersion);
    }
}
