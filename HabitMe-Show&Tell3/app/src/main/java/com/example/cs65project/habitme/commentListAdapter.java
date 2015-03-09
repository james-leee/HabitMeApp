package com.example.cs65project.habitme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yuanjiang on 3/7/15.
 * This is a adapter used to display comment view
 */
public class commentListAdapter extends ArrayAdapter<Comment> {
    public commentListAdapter(Context context, int resource) {
        super(context, resource);
    }


    public commentListAdapter(Context context, int resource, List<Comment> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.commt_entry_layout, null);
        }
        Comment p = getItem(position);
        if (p != null) {
            TextView userView = (TextView) v.findViewById(R.id.comment_etry_layout_user);
            TextView commentView  = (TextView) v.findViewById(R.id.comment_entry_layout_comment);
            userView.setText(p.getUser() + ": ");
            commentView.setText(p.getComment());
        }
        return v;

    }
}
