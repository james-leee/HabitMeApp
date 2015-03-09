package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * click to show post details and show comments
 */
public class PostDetailActivity extends Activity {
    private static final String TAG = "PostDetailActivity";
    //reference from layout
    private ImageView headerImage;
    private  TextView usrname;
    private  TextView tags;
    private  ImageView postImg;
    private  TextView postContent;
    private  TextView postLocation;
    private   ImageView mMarker;
    private  ImageView mPrivacy;
    private Context mContext;
    private ImageView mLike;
    private ImageView mComment;
    private android.content.IntentFilter mCommentIntentFilter;
    private ListView commentListView;

    //other useful variables
    SharedPreferences pref;
    private Set<String> mLikeSets = new HashSet<String>();
    List<Comment> comments;
    String pid;
    ArrayAdapter<Comment> adapter;

    /**
     * broadcast receive to receive comment update
     */
    private BroadcastReceiver mCommentUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String comment = intent.getStringExtra("comment");
            if (comment != null) {
                updateComments(comment);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        mContext = this;
        comments = new ArrayList<Comment>();

        //set action bar colors
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102, 153, 0)));
        pref = getSharedPreferences(TAG,MODE_PRIVATE);
        mLikeSets = pref.getStringSet("likelist",new HashSet<String>());

        //GCM notification filter
        mCommentIntentFilter = new IntentFilter();
        mCommentIntentFilter.addAction("GCM_NOTIFY_COMMENT");

        //get intent to get position
        Intent i = getIntent();
        int position = i.getIntExtra("position",-1);
        final FriendPostItem item = FriendFragment.mPostList.get(position);
        pid = item.getPid();
        getCommentAsJSON(pid);

        //set comment list view
        commentListView = (ListView)findViewById(R.id.commentList);
        adapter = new commentListAdapter(this, R.id.commentList, comments);
        commentListView.setAdapter(adapter);

        //get reference from layout
        headerImage = (ImageView)findViewById(R.id.profile_img);
        usrname = (TextView)findViewById(R.id.user_name);
        tags = (TextView)findViewById(R.id.hash_tags);
        postImg = (ImageView)findViewById(R.id.post_image);
        postContent = (TextView)findViewById(R.id.post_text);
        mMarker = (ImageView)findViewById(R.id.loc_marker);
        postLocation = (TextView)findViewById(R.id.post_loctext);
        mPrivacy = (ImageView)findViewById(R.id.privacy_icon);
        mComment=(ImageView)findViewById(R.id.comment_button);
        mLike = (ImageView)findViewById(R.id.like_image);

        //set on click listener for mComment
        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText comment_text = new EditText(mContext);
                comment_text.setTextColor(Color.rgb(0,0,0));
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.comment_text)
                        .setView(comment_text)
                        .setPositiveButton(R.string.dialog_submit,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        String comment_str = comment_text.getText().toString();
                                        Comment new_comment = new Comment();
                                        new_comment.setUser(Globals.user.getUname());
                                        new_comment.setComment(comment_str);
                                        comments.add(new_comment);
                                        adapter.notifyDataSetChanged();
                                        sendCommentAsJSON(comment_str, pid, Globals.user.getUid());
                                    }
                                })
                        .setNegativeButton(R.string.dialog_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).create();
                dialog.show();
            }
        });

        //set tag
        usrname.setText(item.getUid());
        tags.setText("#" + item.getPost_title());

        //load picture
        String profilepath = item.getUser_img();
        if(profilepath!=null && profilepath.length()!=0){
            DownloadRandomPicture download = new DownloadRandomPicture(mContext,LoginActivity.mApi, MakePostActivity.PHOTO_DIR+profilepath,headerImage);
            download.execute();
        }else{
            // Default profile photo if no photo saved before.
            if(item.getUid().equals(Globals.user.getUname())){
                headerImage.setImageDrawable(getResources().getDrawable(R.drawable.octopus));
            }else if(item.getUid().equals(Globals.user.getUname())){
                headerImage.setImageDrawable(getResources().getDrawable(R.drawable.lobster));
            }else if(item.getUid().equals(Globals.user.getUname())){
                headerImage.setImageDrawable(getResources().getDrawable(R.drawable.tuna));
            }else if(item.getUid().equals(Globals.user.getUname())){
                headerImage.setImageDrawable(getResources().getDrawable(R.drawable.chicken));
            }else if(item.getUid().equals(Globals.user.getUname())){
                headerImage.setImageDrawable(getResources().getDrawable(R.drawable.panda));
            }
        }
        final String fpath = item.getPost_img();
        if(fpath!=null&&fpath.length()!=0){
            postImg.setVisibility(View.VISIBLE);
            DownloadRandomPicture download = new DownloadRandomPicture(mContext,LoginActivity.mApi, MakePostActivity.PHOTO_DIR+fpath,postImg);
            download.execute();
        }else{
            postImg.setVisibility(View.GONE);
        }

        if(mLikeSets.contains(fpath)){
            mLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.like));
        }
        mLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLikeSets.contains(fpath)){
                    mLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.unlike));
                    mLikeSets.remove(fpath);
                }else{
                    mLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.like));
                    mLikeSets.add(fpath);
                }
                pref.edit().putStringSet("likelist",mLikeSets).commit();
            }
        });

        postContent.setText(item.getPost_content());
        if(!item.getLocation().equals("Location Disabled")){
            postLocation.setText(item.getLocation());
        }else{
            mMarker.setVisibility(View.GONE);
            postLocation.setVisibility(View.GONE);
        }

        if(item.getPrivacy()==0){
            mPrivacy.setImageDrawable(mContext.getDrawable(R.drawable.lock));
        }

    }

    /**
     * get update comments from server
     * @param comment
     */
    public void updateComments(String comment) {
        comments.clear();
        try {
            JSONObject obj = new JSONObject(comment);
            JSONArray array = obj.getJSONArray("array");
            for(int i = 0; i < array.length(); i++) {
                JSONObject commentObj = array.getJSONObject(i);
                Comment temp = new Comment();
                temp.setUser((String)commentObj.get("username"));
                temp.setComment((String) commentObj.get("content"));
                comments.add(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * getCommentAsJSON
     * @param pid
     */
    public void getCommentAsJSON(String pid) {
        JSONObject post = new JSONObject();
        try {
            post.put("regID",MainActivity.regid);
            post.put("pid", pid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getComment(post.toString());
    }

    /**
     * get comment
     * @param data
     */
    private void getComment(String data){
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String url = getString(R.string.server_addr) + "/getcomments.do";
                String res = "";
                Map<String, String> map = new HashMap<String,String>();
                map.put("get_comment", params[0]);
                try {
                    res = ServerUtilities.post(url,map,"application/json");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return res;
            }
        }.execute(data);
    }

    /**
     * sendCommentAsJSON
     * @param commentString
     * @param pid
     * @param uid
     */
    public void sendCommentAsJSON(String commentString, String pid, String uid) {
        JSONObject post = new JSONObject();
        try {
            post.put("pid", pid);
            post.put("uid", uid);
            post.put("content", commentString);
            post.put("time",System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postComment( post.toString());
    }

    /**
     * post comment on friend cycle
     * @param data
     */
    private void postComment(String data){
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String url = getString(R.string.server_addr) + "/getcomments.do";
                String res = "";
                Map<String, String> map = new HashMap<String,String>();
                map.put("new_comment", params[0]);
                try {
                    res = ServerUtilities.post(url,map,"application/json");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return res;
            }
        }.execute(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
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
        //register broadcast listener
        registerReceiver(mCommentUpdateReceiver, mCommentIntentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //unregister broadcast listener
        unregisterReceiver(mCommentUpdateReceiver);
        super.onPause();
    }
}
