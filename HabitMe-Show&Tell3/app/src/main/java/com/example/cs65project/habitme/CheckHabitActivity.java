package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Date;


/**
 * CheckHabitActivity to let user check in a particular habit
 */
public class CheckHabitActivity extends Activity {
    //lay out entities
    private EditText mNameText, mChecktText;
    private ImageView mButtonView;
    private Intent mIntent;
    private ImageView mTreeView;

    //variables for managing data
    private EntryDataSource datasource;
    private HabitItem mEntry;

    //useful variable
    private long mEntryID;
    private Context mContext = this;

    /**
     * Override onCreate() in CheckHabitActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_habit);

        //set action bar color
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102, 153, 0)));

        //find reference from layout
        mNameText = (EditText)findViewById(R.id.activity_name_text);
        mChecktText = (EditText)findViewById(R.id.activity_check_in);
        mButtonView = (ImageView)findViewById(R.id.check_in_button_id);
        mTreeView = (ImageView)findViewById(R.id.check_in_tree_grow);

        //get id from previous activity
        mIntent = getIntent();
        mEntryID = mIntent.getLongExtra("ID",0);

        //get user habit item details from database
        datasource = new EntryDataSource(this);
        datasource.open();
        mEntry = datasource.fetchEntryByIndex(mEntryID);
        String[] times = mEntry.getCheckTimeList().split(",");
        String time = times[times.length - 1];
        String name = mEntry.getHabitTitle();

        //set changing tree grows pictures frequency
        int checkInNum = times.length - 2;
        checkInNum = checkInNum%15;

        //set last check in time display in layout
        if(time.equals("none")){
            mChecktText.setText("New");
        }else{
            Date date = new Date(Long.parseLong(time));
            mChecktText.setText(DateFormat.format("kk:mm MM/dd/yy",
                    date.getTime()).toString());
        }
        mNameText.setText(name);

        //send intent to MakePostActivity with entry id
        mButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,MakePostActivity.class);
                intent.putExtra("ID",mEntryID);
                startActivity(intent);
                finish();
            }
        });

        //details for change tree grows pictures
        if(checkInNum /3 == 0){
            mTreeView.setImageDrawable(getResources().getDrawable(R.drawable.treeone));
        }else if(checkInNum/3 == 1){

            mTreeView.setImageDrawable(getResources().getDrawable(R.drawable.treetwo));
        }else if(checkInNum/3 == 2){

            mTreeView.setImageDrawable(getResources().getDrawable(R.drawable.treethree));
        }else if(checkInNum/3 == 3){
            mTreeView.setImageDrawable(getResources().getDrawable(R.drawable.treefour));
        }else{
            mTreeView.setImageDrawable(getResources().getDrawable(R.drawable.treefive));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.check_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //click delete to delete a habit
        if (id == R.id.delete_action) {
            EntryDataSource dataSource = new EntryDataSource(this);
            //open database
            dataSource.open();
            dataSource.removeEntry(mEntryID);

            //after udpating, close databse
            dataSource.close();

            //send intent back to MainActivity
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
