package com.example.cs65project.habitme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiboying on 2/27/15.
 */


public class EntryDataSource {
    //Entry Data Source for connecting database with application layer
    private SQLiteDatabase database;
    private EntryDbHelper dbHelper;
    private String[] allColumns = {
            HistoryTable.KEY_ROWID,
            HistoryTable.KEY_HABIT_TITLE,
            HistoryTable.KEY_FREQUENCY,
            HistoryTable.KEY_CHECK_TIME_LIST,
            HistoryTable.KEY_CREATE_TYPE,
            HistoryTable.KEY_CHOOSE_POSITION,
            HistoryTable.KEY_USE_NUM,
            HistoryTable.KEY_TIME_LENGTH,
            HistoryTable.KEY_LOCATION
    };
    private String[] allPostsColumns = {
            PostsTable.KEY_ROWID,
            PostsTable.KEY_USERNAME,
            PostsTable.KEY_USERIMAGE,
            PostsTable.KEY_POST_TITLE,
            PostsTable.KEY_POST_CONTENT,
            PostsTable.KEY_POST_IMAGE,
            PostsTable.KEY_POST_LOCATION,
            PostsTable.KEY_POST_PRIVACY,
            PostsTable.KEY_POST_ID
    };
    private String[] allCommentsColumns = {
            CommentTable.KEY_ROWID,
            CommentTable.KEY_POSTID,
            CommentTable.KEY_USER_NAME,
            CommentTable.KEY_CONTENT,
    };

    public EntryDataSource(Context context) {
        dbHelper = new EntryDbHelper(context);
    }

    //open database
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    //close database
    public void close() {
        dbHelper.close();
    }


    /**
     * Inser new entry into the database
     * @param entry: new post entry
     * @return entry id in database
     */
    public long insertPosts(FriendPostItem entry) {
        ContentValues values = new ContentValues();
        values.put(PostsTable.KEY_USERNAME, entry.getUid());
        values.put(PostsTable.KEY_USERIMAGE, entry.getUser_img());
        values.put(PostsTable.KEY_POST_TITLE, entry.getPost_title());
        values.put(PostsTable.KEY_POST_CONTENT,entry.getPost_content());
        values.put(PostsTable.KEY_POST_IMAGE,entry.getPost_img());
        values.put(PostsTable.KEY_POST_LOCATION,entry.getLocation());
        values.put(PostsTable.KEY_POST_PRIVACY,entry.getPrivacy());
        values.put(PostsTable.KEY_POST_ID,entry.getPid());
        long insertId = database.insert(PostsTable.TABLE_NAME_ENTRIES, null,
                values);
        return insertId;
    }

    /**
     * Insert a new entry into the database
     * @param entry: new habit entry
     * @return entry id in database
     */
    public long insertEntry(HabitItem entry) {
        ContentValues values = new ContentValues();
        values.put(HistoryTable.KEY_ROWID, entry.getId());
        values.put(HistoryTable.KEY_HABIT_TITLE, entry.getHabitTitle());
        values.put(HistoryTable.KEY_FREQUENCY, entry.getFrequency());
        values.put(HistoryTable.KEY_CHECK_TIME_LIST, entry.getCheckTimeList());
        values.put(HistoryTable.KEY_CREATE_TYPE,entry.getCreateType());
        values.put(HistoryTable.KEY_CHOOSE_POSITION,entry.getChoosePosition());
        values.put(HistoryTable.KEY_USE_NUM,entry.getUseNum());
        values.put(HistoryTable.KEY_TIME_LENGTH,entry.getTimeLength());
        long insertId = database.insert(HistoryTable.TABLE_NAME_ENTRIES, null,
                values);
        return insertId;
    }

    /**
     * Insert a new entry into the database
     * @param comment new comment created by user
     * @return entry id in database
     */
    public long insertComments(Comment comment) {
        ContentValues values = new ContentValues();
        values.put(CommentTable.KEY_POSTID, comment.getPid());
        values.put(CommentTable.KEY_USER_NAME, comment.getUser());
        values.put(CommentTable.KEY_CONTENT, comment.getComment());
        long insertId = database.insert(CommentTable.TABLE_NAME_ENTRIES, null,
                values);
        return insertId;
    }


    // Remove an entry by giving its index
    public void removePost(long rowIndex) {
        database.delete(PostsTable.TABLE_NAME_ENTRIES,
                HistoryTable.KEY_ROWID + "=" + rowIndex,
                null);
        return;
    }

    // Remove an entry by giving its index
    public void removeEntry(long rowIndex) {
        database.delete(HistoryTable.TABLE_NAME_ENTRIES,
                HistoryTable.KEY_ROWID + "=" + rowIndex,
                null);
        return;
    }

    /**
     * Fetch a post from the database according its title
     * @param title post title
     * @return a list of post with the same title
     */
    public List<FriendPostItem> fetchPostsByPostTitle(String username, String title) {
        List<FriendPostItem> posts = new ArrayList<FriendPostItem>();
        Cursor cursor = database.rawQuery("select * from posts where key_user_name like ? and key_post_title like ?", new String[]{username, title});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FriendPostItem entry = cursorToPostEntries(cursor);
            posts.add(entry);
            cursor.moveToNext();
        }
        cursor.close();
        return posts;
    }

    /**
     * Fetch a comment from the database according the post id
     * @param pid post title
     * @return a list of comment of a post
     */
    public List<Comment> fetchCommentsByPost(String pid) {
        List<Comment> comments = new ArrayList<Comment>();
        Cursor cursor = database.rawQuery("select * from comments where key_comment_postid like ?", new String[]{pid});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Comment entry = cursorToCommentEntries(cursor);
            comments.add(entry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }


    private Comment cursorToCommentEntries(Cursor cursor){
        Comment comment = new Comment();
        comment.setPid(cursor.getString(1));
        comment.setUser(cursor.getString(2));
        comment.setComment(cursor.getString(3));
        return comment;
    }

    private FriendPostItem cursorToPostEntries(Cursor cursor) {
        FriendPostItem post = new FriendPostItem ();
        post.setUid(cursor.getString(1));
        post.setUser_img(cursor.getString(2));
        post.setPost_title(cursor.getString(3));
        post.setPost_content(cursor.getString(4));
        post.setPost_img(cursor.getString(5));
        post.setLocation(cursor.getString(6));
        post.setPrivacy(cursor.getInt(7));
        post.setPid(cursor.getString(8));
        return post;
    }

    /**
     * Fetch a habit from the database according to its id
     * @param rowId post title
     * @return a habit with the row id in the database
     */
    public HabitItem fetchEntryByIndex(long rowId) {
        HabitItem entry = new HabitItem();
        Cursor cursor = database.query(HistoryTable.TABLE_NAME_ENTRIES,
                allColumns, HistoryTable.KEY_ROWID + " = " + rowId, null, null,
                null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            entry = cursorToExerciseEntry(cursor);

            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return entry;
    }

    /**
     * Get all the habits in the database
     * @return A list of all the habits in database
     */
    public ArrayList<HabitItem> fetchEntries() {
        ArrayList<HabitItem> entries = new ArrayList<HabitItem>();
        Cursor cursor = database.query(HistoryTable.TABLE_NAME_ENTRIES,
                allColumns,
                null,
                null,
                null,
                null,
                null);

        //move cursor to the first item
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HabitItem entry = cursorToExerciseEntry(cursor);
            entries.add(entry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();

        return entries;
    }


    private HabitItem cursorToExerciseEntry(Cursor cursor) {
        HabitItem entry = new HabitItem ();
        entry.setId(cursor.getLong(0));
        entry.setHabitTitle(cursor.getString(1));
        entry.setFrequency(cursor.getInt(2));
        entry.setCheckTimeList(cursor.getString(3));
        entry.setCreateType(cursor.getInt(4));
        entry.setChoosePosition(cursor.getInt(5));
        entry.setUseNum(cursor.getInt(6));
        entry.setTimeLength(cursor.getString(7));
        return entry;
    }

    /**
     * update the entry in the database according to its id
     * @param rowIndex id in database
     * @param entry new values
     */
    public void updateEntry(long rowIndex, HabitItem entry) {
        ContentValues values = new ContentValues();
        values.put(HistoryTable.KEY_ROWID, entry.getId());
        values.put(HistoryTable.KEY_HABIT_TITLE, entry.getHabitTitle());
        values.put(HistoryTable.KEY_FREQUENCY, entry.getFrequency());
        values.put(HistoryTable.KEY_CHECK_TIME_LIST, entry.getCheckTimeList());
        values.put(HistoryTable.KEY_CREATE_TYPE,entry.getCreateType());
        values.put(HistoryTable.KEY_CHOOSE_POSITION,entry.getChoosePosition());
        values.put(HistoryTable.KEY_USE_NUM,entry.getUseNum());
        values.put(HistoryTable.KEY_TIME_LENGTH,entry.getTimeLength());
        database.update(HistoryTable.TABLE_NAME_ENTRIES,
                values, HistoryTable.KEY_ROWID+"="+rowIndex,
                null);
        return;
    }

    /**
     * update the image in the database according to its title
     * @param img title of the post
     */
    public void updatePostImage(String img) {
        //String sql = "UPDATE "+ PostsTable.TABLE_NAME_ENTRIES + " SET "+ PostsTable.KEY_USERIMAGE +" like ?" + img;
       Cursor cursor = database.rawQuery("UPDATE posts SET "+ PostsTable.KEY_USERIMAGE +"=?", new String[]{img});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FriendPostItem entry = cursorToPostEntries(cursor);
            cursor.moveToNext();
        }
        return;
    }

    /**
     * Get all the public post in the database
     * @return A list of post, which are the whole list of public posts in the database
     */
    public List<FriendPostItem> fetchAllPublicPosts() {
        ArrayList<FriendPostItem> entries = new ArrayList<FriendPostItem>();
        Cursor cursor = database.query(PostsTable.TABLE_NAME_ENTRIES,
                allPostsColumns, PostsTable.KEY_POST_PRIVACY + " = " + 1, null, null,
                null, null);

        //move cursor to the first item
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FriendPostItem entry = cursorToPostEntries(cursor);
            entries.add(0,entry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();

        return entries;
    }


}
