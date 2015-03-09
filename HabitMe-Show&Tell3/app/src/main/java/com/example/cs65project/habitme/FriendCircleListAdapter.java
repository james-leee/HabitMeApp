package com.example.cs65project.habitme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * FriendCircleListAdapter for recyclerview with cardview
 */
public class FriendCircleListAdapter extends RecyclerView.Adapter<FriendCircleListAdapter.ViewHolder>{
    private List<FriendPostItem> mDataset;
    private Context mContext;
    OnItemClickListener mListener;

    /**
     * interface: on item click listener
     */
    public interface OnItemClickListener{
        public void onItemClick(View v, int position);
    }

    /**
     * setOnItemClickListener to show comments of this post
     * @param listener
     */
    public void setOnItemClickListener(final OnItemClickListener listener){
        this.mListener = listener;
    }

    /**
     * Provide a suitable constructor (depends on the kind of dataset)
     * @param context
     * @param myDataset
     */
    public FriendCircleListAdapter(Context context,List<FriendPostItem> myDataset) {
        this.mDataset = myDataset;
        this.mContext = context;
    }


    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     * @param position
     * @param item
     */
    public void add(int position, FriendPostItem item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(FriendPostItem item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_circle_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final FriendCircleListAdapter.ViewHolder holder, int position) {
        FriendPostItem item = mDataset.get(position);
        holder.usrname.setText(item.getUid());
        holder.tags.setText("#"+item.getPost_title());

        //set profile picture
        String profilepath = item.getUser_img();
        if(profilepath != null
                && profilepath.length() != 0){
            DownloadRandomPicture download =
                    new DownloadRandomPicture(mContext,
                            LoginActivity.mApi,
                            MakePostActivity.PHOTO_DIR + profilepath,
                            holder.headerImage);
            download.execute();
        }else{
            if(item.getUid().equals("haomin")){
                holder.headerImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.octopus));
            }else if(item.getUid().equals("boying")){
                holder.headerImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lobster));
            }else if(item.getUid().equals("mubing")){
                holder.headerImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tuna));
            }else if(item.getUid().equals("yuan")){
                holder.headerImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chicken));
            }else if(item.getUid().equals("guest")){
                holder.headerImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.panda));
            }
        }

        //set post picture
        String fpath = item.getPost_img();
        if(fpath != null
                && fpath.length() != 0){
            holder.postImg.setVisibility(View.VISIBLE);
            DownloadRandomPicture download =
                    new DownloadRandomPicture(mContext,
                            LoginActivity.mApi,
                            MakePostActivity.PHOTO_DIR + fpath,
                            holder.postImg);
            download.execute();
        }else{
            holder.postImg.setVisibility(View.GONE);
        }

        //set location display
        holder.postContent.setText(item.getPost_content());
        if(!item.getLocation().equals("Location Disabled")){
            holder.postLocation.setText(item.getLocation());
        }else{
            holder.mMarker.setVisibility(View.GONE);
            holder.postLocation.setVisibility(View.GONE);
        }

        //set privacy or not display
        if(item.getPrivacy() == 0){
            holder.mPrivacy.setImageDrawable(mContext.getDrawable(R.drawable.lock));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }



    /**
     *  ViewHolder class to get reference from layout
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        protected ImageView headerImage;
        protected TextView usrname;
        protected TextView tags;
        protected ImageView postImg;
        protected TextView postContent;
        protected TextView postLocation;
        protected  ImageView mMarker;
        protected ImageView mPrivacy;

        public ViewHolder(View v) {
            super(v);
            headerImage = (ImageView)v.findViewById(R.id.profile_img);
            usrname = (TextView)v.findViewById(R.id.user_name);
            tags = (TextView)v.findViewById(R.id.hash_tags);
            postImg = (ImageView)v.findViewById(R.id.post_image);
            postContent = (TextView)v.findViewById(R.id.post_text);
            mMarker = (ImageView)v.findViewById(R.id.loc_marker);
            postLocation = (TextView)v.findViewById(R.id.post_loctext);
            mPrivacy = (ImageView)v.findViewById(R.id.privacy_icon);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mListener!=null){
                mListener.onItemClick(v,getPosition());
            }
        }
    }

}
