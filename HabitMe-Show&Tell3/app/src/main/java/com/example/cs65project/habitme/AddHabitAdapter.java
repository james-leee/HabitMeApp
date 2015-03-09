package com.example.cs65project.habitme;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Add habit adapter for recyclerview with cardview
 */
public class AddHabitAdapter extends RecyclerView.Adapter<AddHabitAdapter.AddHabitViewHolder>  {
    //list postion
    private static final int POSITION_ONE = 0;
    private static final int POSITION_TWO = 1;
    private static final int POSITION_THREE = 2;

    //list to show on the recyclerview
    private List<HabitItem> habitList;

    /**
     * AddHabitAdapter constructor
     * @param habitList
     */
    public AddHabitAdapter(List<HabitItem> habitList) {
        this.habitList = habitList;
    }

    /**
     * override getItemCount() to return current list size
     * @return
     */
    @Override
    public int getItemCount() {
        return habitList.size();
    }

    /**
     * override onBindViewHolder
     * @param AddHabitViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(AddHabitViewHolder AddHabitViewHolder, int i) {
        HabitItem ci = habitList.get(i);
        AddHabitViewHolder.title.setText(ci.getHabitTitle());
        switch (i){
            case POSITION_ONE:
                AddHabitViewHolder.typeImage.setImageResource(R.drawable.studyicon);
                AddHabitViewHolder.use.setText("Sound detector enabled");
                break;
            case POSITION_TWO:
                AddHabitViewHolder.typeImage.setImageResource(R.drawable.sleepicon);
                AddHabitViewHolder.use.setText("Light sensor enabled");
                break;
            case POSITION_THREE:
                AddHabitViewHolder.typeImage.setImageResource(R.drawable.runningicon);
                AddHabitViewHolder.use.setText("Linear acceleration enabled");
                break;
            default:
                AddHabitViewHolder.typeImage.setImageResource(R.drawable.panda);
                break;
        }

    }

    /**
     * Override onCreateViewHolder
     * @param viewGroup
     * @param i
     * @return
     */
    @Override
    public AddHabitViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.cardview_addhabit, viewGroup, false);

        return new AddHabitViewHolder(itemView);
    }

    /**
     *  AddHabitViewHolder class to get reference from layout
     */
    public static class AddHabitViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView use;
        protected ImageView typeImage;
        public AddHabitViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.addhabittitle);
            use = (TextView) v.findViewById(R.id.addhabituse);
            typeImage = (ImageView)v.findViewById(R.id.add_habit_icon);
        }
    }
}

