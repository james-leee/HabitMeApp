package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;


/**
 * This activity is used for users to define the new habit they want to create.
 * The main settings are: how long they will use to do what the habit describe and how often they want to do it.
 */

public class AddHabitActivity extends Activity implements NumberPicker.OnValueChangeListener,NumberPicker.OnScrollListener,NumberPicker.Formatter {

    //detailed information of a new habit which created by the user
    private HabitItem newHabit;
    private EditText activityText;
    private int inputType = -1;
    private int position = -1;

    //Time picker used for user to choose how long they need to finish the habit event and how often they
    //would like to do it
    private NumberPicker mChooseTimeHour;
    private NumberPicker mChooseTimeMinute;
    private NumberPicker mChooseTimeSecond;
    private NumberPicker mChooseTimeDay;
    private NumberPicker mChooseTimeMonth;
    private NumberPicker mChooseTimeWeek;

    private EntryDataSource dataSource;
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102, 153, 0)));

        dataSource = new EntryDataSource(this);
        activityText = (EditText)findViewById(R.id.activity_name);

        //get the details about the new habit the user want to add: title, type(provided or self-created)
        Intent intent = getIntent();
        inputType = intent.getIntExtra("inputType",-1);
        if(inputType == Globals.INPUTTYPE_CHOOSE){
            activityText.setText(intent.getStringExtra("Title"));
            activityText.setKeyListener(null);
            position = intent.getIntExtra("position",-1);
            activityText.setBackground(null);
        }


        //Time picker and frequency picker
        mChooseTimeHour = (NumberPicker)findViewById(R.id.hourpicker);
        mChooseTimeMinute = (NumberPicker)findViewById(R.id.minutepicker);
        mChooseTimeSecond = (NumberPicker)findViewById(R.id.secondpicker);
        mChooseTimeDay = (NumberPicker)findViewById(R.id.daypicker);
        mChooseTimeWeek = (NumberPicker)findViewById(R.id.weekpicker);
        mChooseTimeMonth = (NumberPicker)findViewById(R.id.monthpicker);
        Globals.setNumberPickerTextColor(mChooseTimeHour,Color.rgb(0,0,0));
        Globals.setNumberPickerTextColor(mChooseTimeMinute,Color.rgb(0,0,0));
        Globals.setNumberPickerTextColor(mChooseTimeSecond,Color.rgb(0,0,0));
        Globals.setNumberPickerTextColor(mChooseTimeDay,Color.rgb(0,0,0));
        Globals.setNumberPickerTextColor(mChooseTimeWeek,Color.rgb(0,0,0));
        Globals.setNumberPickerTextColor(mChooseTimeMonth,Color.rgb(0,0,0));

        init();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_habit_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //when user click on save button, the new habit and its detailed information will be saved to the database.
        if (id == R.id.save_action) {
            String act_name = activityText.getText().toString();
            int f = 0;
            newHabit = new HabitItem(act_name,"",f);
            if(inputType == Globals.INPUTTYPE_CHOOSE){
                newHabit.setCreateType(Globals.INPUTTYPE_CHOOSE);
                newHabit.setChoosePosition(position);
            }else{
                newHabit.setCreateType(Globals.INPUTTYPE_MANUAL);
            }
            String timeLength = String.valueOf(mChooseTimeHour.getValue()*3600 + mChooseTimeMinute.getValue() * 60 + mChooseTimeSecond.getValue());
            Log.d("TIMELENGTH:", timeLength);
            int frequency = mChooseTimeDay.getValue() + mChooseTimeWeek.getValue() * 7 + mChooseTimeMonth.getValue()*30;
            newHabit.setFrequency(frequency);
            newHabit.setTimeLength(timeLength);
            dataSource.open();
            dataSource.insertEntry(newHabit);
            dataSource.close();
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     *  Below is the settings of the time picker and frequency picker.
     *  Users are able to use the scroll to set their preferences.
     */
    private void init() {
        mChooseTimeHour.setFormatter(this);
        mChooseTimeHour.setOnValueChangedListener(this);
        mChooseTimeHour.setOnScrollListener(this);
        mChooseTimeHour.setMaxValue(24);
        mChooseTimeHour.setMinValue(0);
        mChooseTimeHour.setValue(0);

        mChooseTimeMinute.setFormatter(this);
        mChooseTimeMinute.setOnValueChangedListener(this);
        mChooseTimeMinute.setOnScrollListener(this);
        mChooseTimeMinute.setMaxValue(60);
        mChooseTimeMinute.setMinValue(0);
        mChooseTimeMinute.setValue(0);

        mChooseTimeSecond.setFormatter(this);
        mChooseTimeSecond.setOnValueChangedListener(this);
        mChooseTimeSecond.setOnScrollListener(this);
        mChooseTimeSecond.setMaxValue(60);
        mChooseTimeSecond.setMinValue(0);
        mChooseTimeSecond.setValue(1);

        mChooseTimeDay.setFormatter(this);
        mChooseTimeDay.setOnValueChangedListener(this);
        mChooseTimeDay.setOnScrollListener(this);
        mChooseTimeDay.setMaxValue(6);
        mChooseTimeDay.setMinValue(0);
        mChooseTimeDay.setValue(1);

        mChooseTimeWeek.setFormatter(this);
        mChooseTimeWeek.setOnValueChangedListener(this);
        mChooseTimeWeek.setOnScrollListener(this);
        mChooseTimeWeek.setMaxValue(3);
        mChooseTimeWeek.setMinValue(0);
        mChooseTimeWeek.setValue(0);

        mChooseTimeMonth.setFormatter(this);
        mChooseTimeMonth.setOnValueChangedListener(this);
        mChooseTimeMonth.setOnScrollListener(this);
        mChooseTimeMonth.setMaxValue(12);
        mChooseTimeMonth.setMinValue(0);
        mChooseTimeMonth.setValue(0);


    }


    public String format(int value) {
        String tmpStr = String.valueOf(value);
        if (value < 10) {
            tmpStr = "0" + tmpStr;
        }
        return tmpStr;
    }
    public void onScrollStateChange(NumberPicker view, int scrollState) {
        switch (scrollState) {
            case NumberPicker.OnScrollListener.SCROLL_STATE_FLING:
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_IDLE:
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                break;
        }
    }
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    }


}
