package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.cs65project.habitme.view.SlidingTabLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is the main activity with three fragments
 * Home fragments displays the habit list of the user
 * Friends fragment displays all the posts of the user and user's friends
 * Me fragments displays the check in posts details of the users
 */

public class MainActivity extends Activity implements ServiceConnection {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "MainActivity";

    private SlidingTabLayout slidingTabLayout;
    private Context mContext = this;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private ActionTabsViewPagerAdapter myViewPagerAdapter;
    private EntryDataSource datasource;

    //service parameters
    private boolean isBound;
    private Intent mServiceIntent;
    private IntentFilter mServiceIntentFilter;
    private boolean mServiceStop = false;
    private GoogleCloudMessaging gcm;
    public static String regid;
    public static String SENDER_ID = "982863968347";
    private int mHabitSensorType;
    private boolean mHabitFail;
    private Context context = this;
    private IntentFilter mMessageIntentFilter;

    //parameters for saved instance when service is going on
    private long entryId;
    private long checkinTime;
    private String location;
    private String filename;
    private String content;

    //Receive updates from server in friend fragment
    private BroadcastReceiver mMessageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");
            if (msg != null) {
                FriendFragment.updatePosts(mContext,msg);
            }
        }
    };

    //Receive results of the provided habits when service finished
    private BroadcastReceiver mSensorUpdateReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, final Intent intent){
            mHabitSensorType = intent.getIntExtra(Globals.HABIT_SENSOR_TYPE, -1);
            mHabitFail = intent.getBooleanExtra(Globals.HABIT_FINISH_OR_NOT, false);
            mServiceStop = intent.getBooleanExtra(Globals.SERVICE_SHOULD_STOP, false);
            if(mServiceStop){
                Globals.UNFINISHED_HABIT = false;
                doUnbindService();
                stopService(mServiceIntent);
                Log.d(TAG,"Service stops at broadcast");
            }
            String habitAlert_message = null;
            //if it is provided habits, it will check whether it succeeded or not, and give the user
            //its feedback
            switch (mHabitSensorType){
                case Globals.SOUND_DETECTOR:
                    if(mHabitFail) {
                        habitAlert_message = "Do you really study in a good place? I heard a lot of noise :(";
                    }else{
                        habitAlert_message = "Cong! You did a great study job!";
                    }
                    break;
                case Globals.LIGHT_SENSOR:
                    if(mHabitFail) {
                        habitAlert_message = "You didn't sleep till now";
                    }else{
                        habitAlert_message = "Cong! You got it! Good Night :)";
                    }
                    break;
                case Globals.ACCELEROMETER_SENSOR:
                    if(mHabitFail) {
                        habitAlert_message = "Don't give up! Come on";
                    }else{
                        habitAlert_message = "Cong! Keep running!";
                    }
                    break;
                default:
                    break;
            }


            //if this habit finishes successfully, add the check in time in the check in time list and
            //set the new successful tries, or notify the user that this is an illegal check in, and discard it
            if(mHabitFail) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(habitAlert_message);
                builder.setTitle("Check in failed");
                builder.setIcon(R.drawable.rainicon);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(habitAlert_message);
                builder.setTitle("Check in succeeded");
                builder.setIcon(R.drawable.sunshineicon);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
                HabitItem entry = datasource.fetchEntryByIndex(entryId);
                entry.setLocation(location);
                entry.addCheckTimeList(checkinTime);
                datasource.updateEntry(entryId, entry);
                HomeFragment.updateEntries();
                MakePostActivity.sendPostStream(entry,mContext.getString(R.string.server_addr),filename,content);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mServiceIntentFilter = new IntentFilter();

        if(getIntent() != null) {
            //if it is started by the background notification, need to get the feedback of the service
            //and notify the user and make the decision to update the last check in habit details or not
            if (Globals.BACKGROUND_FINISH) {
                Globals.BACKGROUND_FINISH = false;
                entryId = Globals.SAVE_FOR_BACKGROUD_ID;
                location = Globals.SAVE_FOR_BACKGROUD_LOCATION;
                checkinTime = Globals.SAVE_FOR_BACKGROUD_CHECKTIME;
                filename = Globals.SAVE_FOR_BACKGROUD_FILENAME;
                content = Globals.SAVE_FOR_BACKGROUD_CONTENT;

                datasource = new EntryDataSource(this);
                datasource.open();
                Globals.UNFINISHED_HABIT = false;
                Intent intent = getIntent();
                Boolean isFail = intent.getBooleanExtra("habitFail", false);
                if (!isFail) {
                    HabitItem entry = datasource.fetchEntryByIndex(entryId);
                    entry.setLocation(location);
                    entry.addCheckTimeList(checkinTime);
                    datasource.updateEntry(entryId, entry);
                    HomeFragment.updateEntries();
                    MakePostActivity.sendPostStream(entry, mContext.getString(R.string.server_addr), filename, content);
                }
            }
            // if it is started from the post activity, check the intend content and decide whether
            // to start service for supervision or not.
            else {
                Log.d(TAG, "getIntent() != null");
                Intent intent = getIntent();
                boolean startService = intent.getBooleanExtra(Globals.START_SERVICE, false);
                boolean isPost = intent.getBooleanExtra(Globals.POST_OR_NOT, false);
                entryId = intent.getLongExtra(Globals.ENTRY_ID, -1);

                checkinTime = intent.getLongExtra("checkintime", 0);
                location = intent.getStringExtra("location");
                filename = intent.getStringExtra("filename");
                content = intent.getStringExtra("content");

                if (startService) {
                    datasource = new EntryDataSource(this);
                    datasource.open();
                    HabitItem entry = datasource.fetchEntryByIndex(entryId);

                    // if it is a provided habit, start the service for supervision
                    // or update the habit check in state.
                    if (entry.getCreateType() == Globals.INPUTTYPE_CHOOSE) {
                        mServiceIntent = new Intent(mContext, SmartSensorService.class);
                        Globals.UNFINISHED_HABIT = true;
                        switch (entry.getChoosePosition()) {
                            case Globals.SOUND_DETECTOR:
                                mServiceIntentFilter.addAction(SmartSensorService.SOUND_DETECTOR);
                                mServiceIntent.putExtra("timeLength", entry.getTimeLength());
                                mServiceIntent.putExtra("sensorType", Globals.SOUND_DETECTOR);
                                Globals.SERVICE_COUNT_TIME = entry.getTimeLength();
                                startService(mServiceIntent);
                                doBindService();
                                break;
                            case Globals.LIGHT_SENSOR:
                                mServiceIntentFilter.addAction(SmartSensorService.LIGHT_SENSOR);
                                mServiceIntent.putExtra("timeLength", entry.getTimeLength());
                                mServiceIntent.putExtra("sensorType", Globals.LIGHT_SENSOR);
                                Globals.SERVICE_COUNT_TIME = entry.getTimeLength();
                                startService(mServiceIntent);
                                doBindService();
                                break;
                            case Globals.ACCELEROMETER_SENSOR:
                                mServiceIntentFilter.addAction(SmartSensorService.SPEED_DETECTOR);
                                mServiceIntent.putExtra("timeLength", entry.getTimeLength());
                                mServiceIntent.putExtra("sensorType", Globals.ACCELEROMETER_SENSOR);
                                Globals.SERVICE_COUNT_TIME = entry.getTimeLength();
                                startService(mServiceIntent);
                                doBindService();
                                break;
                            default:
                                break;
                        }

                    }


                }
            }
        }
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102,153,0)));
        slidingTabLayout = (SlidingTabLayout)findViewById(R.id.tab);
        viewPager = (ViewPager)findViewById(R.id.viewpager);

        fragments = new ArrayList<Fragment>();
        fragments.add(new HomeFragment());
        fragments.add(new FriendFragment());
        fragments.add(new SettingFragment());

        myViewPagerAdapter =new ActionTabsViewPagerAdapter(getFragmentManager(),fragments);
        viewPager.setAdapter(myViewPagerAdapter);

        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);

        mMessageIntentFilter = new IntentFilter();
        mMessageIntentFilter.addAction("GCM_NOTIFY");

        // Check device for Play Services APK. If check succeeds, proceed with
        // GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if (regid.isEmpty()) {
                registerInBackground();
            }
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                Log.e("doIn", "background");
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.e("message",msg);
                    // You should send the registration ID to your server over
                    // HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your
                    // app.
                    // The request to your server should be authenticated if
                    // your app
                    // is using accounts.
                    ServerUtilities.sendRegistrationIdToBackend(context, regid);

                    // For this demo: we don't need to send it because the
                    // device
                    // will send upstream messages to a server that echo back
                    // the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, "gcm register msg: " + msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_habit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_action) {
            Intent intent = new Intent(this,AddChooseHabitActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    private void doBindService() {
        //Log.i(LOGTAG, "doBindService");
        if(!isBound){
            bindService(mServiceIntent, this, 0);
        }
        isBound = true;
    }

    private void doUnbindService() {
        //Log.i(LOGTAG, "doUnBindService");
        if(isBound){
            unbindService(this);
        }
        isBound = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //Log.d(TAG, "onServiceConnected() called");


    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
//        Log.d(TAG, "onServiceDisconnected() called");
        stopService(mServiceIntent);

    }

    @Override
    protected void onPause() {
        unregisterReceiver(mSensorUpdateReceiver);
        unregisterReceiver(mMessageUpdateReceiver);
        doUnbindService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(mSensorUpdateReceiver, mServiceIntentFilter);
        registerReceiver(mMessageUpdateReceiver, mMessageIntentFilter);
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if(mServiceStop == false && mServiceIntent !=null) {
            doUnbindService();
            stopService(mServiceIntent);
            Log.d(TAG,"Service stops at onDestroy");
        }

        super.onDestroy();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        Globals.SAVE_FOR_BACKGROUD_ID = entryId;
        Globals.SAVE_FOR_BACKGROUD_LOCATION= location;
        Globals.SAVE_FOR_BACKGROUD_CHECKTIME = checkinTime;
        Globals.SAVE_FOR_BACKGROUD_FILENAME = filename;
        Globals.SAVE_FOR_BACKGROUD_CONTENT = content;
    }


}
