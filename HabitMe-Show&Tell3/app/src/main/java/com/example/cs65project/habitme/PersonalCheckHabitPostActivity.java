package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PersonalCheckHabitPostActivity extends Activity {

    private RecyclerView mPostsView;
    private Activity mContext = this;
    public static EntryDataSource dataSource;
    public static FriendCircleListAdapter mAdapter;
    public static List<FriendPostItem> mPostList;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_check_habit_post);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102, 153, 0)));

        Intent i = getIntent();
        String title = i.getStringExtra("Title");
        mPostsView = (RecyclerView)findViewById(R.id.my_posts_view);
        mPostsView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(mContext);
        mPostsView.setLayoutManager(mLayoutManager);
        dataSource = new EntryDataSource(this);
        dataSource.open();
        mPostList = dataSource.fetchPostsByPostTitle(Globals.user.getUname(),title);
        dataSource.close();
        mAdapter = new FriendCircleListAdapter(mContext,mPostList);
        mPostsView.setAdapter(mAdapter);
    }
//
//    public static void getAllPosts(String title) {
//        dataSource.open();
//        mPostList = dataSource.fetchPostsByPostTitle(title);
//        mAdapter.notifyDataSetChanged();
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal_check_habit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
