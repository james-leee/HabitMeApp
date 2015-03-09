package com.example.cs65project.habitme;

/**
 * Created by haominzhang on 15/2/27.
 */
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;


/**
 * Global files for common keys and variables used in entire client side
 */
public class Globals {
    //Global keys for putting etras in each intent
    public static final String HABIT_SENSOR_TYPE = "com.example.cs65project.habitme.HABIT_SENSOR_TYPE";
    public static final String SERVICE_SHOULD_STOP = "com.example.cs65project.habitme.SERVICE_SHOULD_STOP";
    public static final String START_SERVICE = "com.example.cs65project.habitme.START_SERVICE";
    public static final String POST_OR_NOT = "com.example.cs65project.habitme.POST_OR_NOT";
    public static final String ENTRY_ID = "com.example.cs65project.habitme.ENTRY_ID_FOR_SELECT";
    public static final String HABIT_FINISH_OR_NOT = "com.example.cs65project.habitme.HABIT_FINISH_OR_NOT";


    //ID for two different notifications
    public static final int CHECKINGIN_NOTIFICATION_ID = 0;
    public static final int FINISH_NOTIFICATION_ID = 1;

    //Two different input habit types
    public static final int INPUTTYPE_CHOOSE = 0;
    public static final int INPUTTYPE_MANUAL = 1;

    //Data memory size for connection
    public static final int ACCELEROMETER_BUFFER_CAPACITY = 2048;
    public static final int ACCELEROMETER_BLOCK_CAPACITY = 64;

    //Different sensor type
    public static final int SOUND_DETECTOR = 0;
    public static final int LIGHT_SENSOR = 1;
    public static final int ACCELEROMETER_SENSOR = 2;

    //Global vars for checking finished habits check in
    public static boolean UNFINISHED_HABIT = false;
    public static boolean BACKGROUND_FINISH = false;

    //Other global variables
    public static User user;
    public static long SAVE_FOR_BACKGROUD_ID;
    public static long SAVE_FOR_BACKGROUD_CHECKTIME;
    public static String SERVICE_COUNT_TIME = "0";
    public static String SAVE_FOR_BACKGROUD_LOCATION;
    public static String SAVE_FOR_BACKGROUD_FILENAME;
    public static String SAVE_FOR_BACKGROUD_CONTENT;


    /**
     * Static global function for setting number picker text color
     * @param numberPicker
     * @param color
     * @return
     */
    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color){
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }
}
