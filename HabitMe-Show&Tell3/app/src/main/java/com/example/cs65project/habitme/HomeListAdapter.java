package com.example.cs65project.habitme;

/**
 * Created by shiboying on 3/1/15.
 */
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * HomeListAdapter for recyclerview with cardview
 */
public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder>  {
    //list to show on the recyclerview
    private List<HabitItem> habitList;

    /**
     * HomeListAdapter constructor
     * @param habitList
     */
    public HomeListAdapter(List<HabitItem> habitList) {
        this.habitList = habitList;
    }

    /**
     * Return habitlist size()
     * @return
     */
    @Override
    public int getItemCount() {
        return habitList.size();
    }

    /**
     * Override onBindViewHolder
     * @param HomeListViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(HomeListViewHolder HomeListViewHolder, int i) {
        HabitItem ci = habitList.get(i);
        HomeListViewHolder.title.setText(ci.getHabitTitle());
        int frequency = Integer.parseInt(ci.getTimeLength());
        int hour = frequency / 3600;
        frequency -= hour * 3600;
        int min = frequency / 60;
        frequency -= min * 60;
        HomeListViewHolder.frequency.setText(hour + " hr " +
                                                min + " min " +
                                                frequency + " sec");


        //get last checkin time list
        String[] times = ci.getCheckTimeList().split(",");
        String time = times[times.length-1];

        //set last checkin time in layout
        if(time.equals("none")){
            HomeListViewHolder.lastCheckInTime.setText("Last check in: New");
            HomeListViewHolder.checkInNum.setText("0");
        }else{
            Date date = new Date(Long.parseLong(time));
            HomeListViewHolder.lastCheckInTime.setText("Last check in: " + DateFormat.format("kk:mm MM/dd/yy",
                    date.getTime()).toString());
            HomeListViewHolder.checkInNum.setText(String.valueOf(times.length-2));
        }
    }

    /**
     * Adapt card view in recyclerview
     * @param viewGroup
     * @param i
     * @return
     */
    @Override
    public HomeListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.cardview, viewGroup, false);

        return new HomeListViewHolder(itemView);
    }

    /**
     * Get reference from layout
     */
    public static class HomeListViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView frequency;
        protected TextView lastCheckInTime;
        protected TextView checkInNum;
        public HomeListViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            frequency = (TextView) v.findViewById(R.id.frequency);
            lastCheckInTime =(TextView)v.findViewById(R.id.lastcheckintime);
            checkInNum = (TextView)v.findViewById(R.id.checkInNum);
        }
    }
}
