package com.example.cs65project.habitme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cs65project.habitme.FriendCircleListAdapter.OnItemClickListener;
import com.example.cs65project.habitme.library.PullToRefreshView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FriendFragment to show friends cycle. Can display latest friends posts
 */
public class FriendFragment extends Fragment implements PullToRefreshView.OnRefreshListener,OnItemClickListener{
    //variables for managing view
    private CardView cardView;
    private Context mContext;
    private PullToRefreshView mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    //variables for managing data
    public static FriendCircleListAdapter mAdapter;
    public static List<FriendPostItem> mPostList;

    public static String msg = null;

    public static FriendFragment newInstance() {
        FriendFragment fragment = new FriendFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();



    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friend, container, false);
        mSwipeRefreshLayout = (PullToRefreshView)v.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        if(mPostList==null) {
            EntryDataSource dataSource = new EntryDataSource(mContext);
            dataSource.open();
            mPostList = dataSource.fetchAllPublicPosts();
            dataSource.close();
        }
        mRecyclerView = (RecyclerView)v.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mContext = getActivity();
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new FriendCircleListAdapter(mContext,mPostList);
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    /**
     * Update posts when a user try to pull and refresh
     * @param context
     * @param msg
     */
    public static void updatePosts(Context context, String msg) {
            EntryDataSource dataSource = new EntryDataSource(context);
            dataSource.open();
            try {
                JSONObject obj = new JSONObject(msg);
                JSONArray array = obj.getJSONArray("array");
                for(int i = array.length()-1; i>=0; i--){
                    JSONObject post = array.getJSONObject(i);

                    FriendPostItem item = new FriendPostItem();
                    item.setUid((String)post.get("username"));
                    item.setUser_img((String) post.get("userimage"));
                    item.setPost_title((String) post.get("title"));
                    item.setPost_content((String) post.get("content"));
                    item.setPost_img((String) post.get("picture"));
                    item.setLocation((String) post.get("location"));
                    item.setPrivacy((int) post.get("privacy"));
                    item.setPid((String) post.get("pid"));

                    if(!item.getUid().equals(Globals.user.getUname())) {
                        dataSource.insertPosts(item);
                    }
                    mPostList.add(0, item);
                }

                //when there is a updated post, update recyclerview
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    /**
     * Get histroy posts. Store dowloaded posts from server and store it locally
     * @param context
     * @param s
     */
    public static void getHistoryPosts(Context context, String s) {
        final String host = context.getString(R.string.server_addr);
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String url =  host + "/get_history.do";
                String res = "";
                Map<String, String> map = new HashMap<String,String>();
                map.put("new_post", params[0]);
                try {
                    res = ServerUtilities.post(url,map);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return res;
            }
        }.execute(s);
    }

    /**
     * onRefresh(). When user pull to fresh, call this function
     */
    @Override
    public void onRefresh() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("regID",MainActivity.regid);
            obj.put("uid",Globals.user.getUid());
            obj.put("reqtime",System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getHistoryPosts(mContext,obj.toString());
        new Handler().postDelayed(new Runnable(){
            @Override public void run(){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        },2000);
    }


    /**
     * adapted cardview onto recyclerview
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardView = (CardView) view.findViewById(R.id.card_view);
        mAdapter.setOnItemClickListener(this);
    }

    /**
     * click on a particular item on the view and
     * start activity to PostDetailActivity
     * @param v
     * @param position
     */
    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(mContext,PostDetailActivity.class);
        intent.putExtra("position",position);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
