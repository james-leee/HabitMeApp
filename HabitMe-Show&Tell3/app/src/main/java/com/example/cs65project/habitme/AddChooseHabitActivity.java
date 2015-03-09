package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity is used for users to choose the habit type when they want to add a new habit.
 * The App provides three pre-defined habit. User can also click the edit button to create their own habit.
 * The pre-defined habits are shown by a cardview
 */

public class AddChooseHabitActivity extends Activity {

   //Cardview parameter to show the provided habits for choose
    private static AddHabitAdapter ca;
    private static RecyclerView recList;
    private static List<HabitItem> mEntryList = new ArrayList<HabitItem>();

    //edit button to create self-defined habit
    private ImageView mWriteHabit;


    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_main_habit);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102, 153, 0)));
        recList = (RecyclerView)findViewById(R.id.cardListAdd);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        setTopHabits();
        ca = new AddHabitAdapter(mEntryList);
        recList.setAdapter(ca);

        recList.addOnItemTouchListener(
                new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(mContext, AddHabitActivity.class);
                        HabitItem mEntry = mEntryList.get(position);
                        intent.putExtra("Title", mEntry.getHabitTitle());
                        intent.putExtra("inputType",Globals.INPUTTYPE_CHOOSE);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }
                })
        );
        mWriteHabit = (ImageView)findViewById(R.id.addHabitButton);
        mWriteHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddHabitActivity.class);
                intent.putExtra("inputType",Globals.INPUTTYPE_MANUAL);
                startActivity(intent);
                finish();
            }
        });

    }

    /**
     *  provided habit for user to choose
     */
    private void setTopHabits() {
        mEntryList.clear();
        mEntryList.add(new HabitItem("Study","none",1));
        mEntryList.add(new HabitItem("Sleep quickly","none",1));
        mEntryList.add(new HabitItem("Running","none",1));

    }


}
