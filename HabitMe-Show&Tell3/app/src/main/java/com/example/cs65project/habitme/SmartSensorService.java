package com.example.cs65project.habitme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.meapsoft.FFT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This service is used for supervision when the user check in a provided habits
 * The service will start different types of sensor to detect the user's ongong activity
 * Then analyze the collected data to know whether the user finished doing what the user claimed to do
 * The service will stop when time is up according to the user's input time.
 */
public class SmartSensorService extends Service implements SensorEventListener {
    public static final String SPEED_DETECTOR = "com.example.cs65project.habitme.SPEED_DETECTOR";
    public static final String LIGHT_SENSOR = "com.example.cs65project.habitme.LIGHT_SENSOR";
    public static final String SOUND_DETECTOR = "com.example.cs65project.habitme.SOUND_DETECTOR";
    private String TAG = "SmartSensorService";

    private CountDownTimer cdt = null;
    private final IBinder mBinder = new LocalBinder();
    private boolean mHabitFail = false;
    private boolean mCountDownFinished = false;
    private static boolean isRunning = false;
    private int mTimeLength = 0;

    //Sensor parameter
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mServiceTaskType;
    private NotificationManager mNotificationManager;

    //ACCELEROMETER sensor service
    private OnRunningSensorChangedTask mRunningSensorTask;
    private static ArrayBlockingQueue<Double> mAccBuffer;
    private Intent mSpeedDetectorUpdateIntent;
    private double runningCnt = 0;
    private double noneRunningCnt = 0;

    //LIGHT sensor service
    private float currentLightValue = Float.NaN;
    private Intent mLightSensorUpdateIntent;

    //Audio Recorder service
    private Intent mSoundDetectorUpdateIntent;
    private MediaRecorder mRecorder;
    private ArrayList<Double> mSoundDBList = new ArrayList<>();
    private OnSoundRecordTask mSoundRecordTask;



    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onCreate() called");
        mAccBuffer = new ArrayBlockingQueue<Double>(
                Globals.ACCELEROMETER_BUFFER_CAPACITY);
        mTimeLength = Integer.parseInt(Globals.SERVICE_COUNT_TIME);
        Globals.SERVICE_COUNT_TIME = "0";
        //time counter according to user's time length
        //when time is up, analyze data and broadcast result
        cdt = new CountDownTimer(mTimeLength*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
            }
            @Override
            public void onFinish() {
                Log.i(TAG, "Timer finished");
                mCountDownFinished = true;
                Log.d(TAG, "Show finish notfitication");
                boolean serviceHasBroadcast = false;
                switch (mServiceTaskType) {
                    case Globals.SOUND_DETECTOR:
                        int sumDB = 0;
                        for (double eachDB : mSoundDBList) {
                            sumDB += eachDB;
                        }
                        double avgDB = 0.0;
                        if (mSoundDBList.size() != 0) {
                            avgDB = sumDB / mSoundDBList.size();
                        }
                        Log.d(TAG,"avgDB"+avgDB);
                        if (avgDB > -5.0) {
                            mHabitFail = true;
                        }
                        mSoundDetectorUpdateIntent = new Intent();
                        mSoundDetectorUpdateIntent.setAction(SmartSensorService.SOUND_DETECTOR);
                        mSoundDetectorUpdateIntent.putExtra(Globals.HABIT_SENSOR_TYPE, Globals.SOUND_DETECTOR);
                        mSoundDetectorUpdateIntent.putExtra(Globals.HABIT_FINISH_OR_NOT, mHabitFail);
                        mSoundDetectorUpdateIntent.putExtra(Globals.SERVICE_SHOULD_STOP, true);
                        sendBroadcast(mSoundDetectorUpdateIntent);
                        serviceHasBroadcast = true;
                        stopThisService();
                        break;
                    case Globals.LIGHT_SENSOR:
                        if (currentLightValue >= SensorManager.LIGHT_FULLMOON) {
                            mHabitFail = true;
                        }
                        mLightSensorUpdateIntent = new Intent();
                        mLightSensorUpdateIntent.setAction(SmartSensorService.LIGHT_SENSOR);
                        mLightSensorUpdateIntent.putExtra(Globals.HABIT_SENSOR_TYPE, Globals.LIGHT_SENSOR);
                        mLightSensorUpdateIntent.putExtra(Globals.HABIT_FINISH_OR_NOT, mHabitFail);
                        mLightSensorUpdateIntent.putExtra(Globals.SERVICE_SHOULD_STOP, true);
                        sendBroadcast(mLightSensorUpdateIntent);
                        serviceHasBroadcast = true;
                        stopThisService();
                        break;
                    case Globals.ACCELEROMETER_SENSOR:
                        double totalCnt = runningCnt + noneRunningCnt;
                        if (runningCnt / totalCnt < 0.7) {
                            mHabitFail = true;
                            Log.d(TAG, "running fail");
                        }
                        Log.d(TAG, "running percentage: " + runningCnt / totalCnt);
                        mSpeedDetectorUpdateIntent = new Intent();
                        mSpeedDetectorUpdateIntent.setAction(SmartSensorService.SPEED_DETECTOR);
                        mSpeedDetectorUpdateIntent.putExtra(Globals.HABIT_SENSOR_TYPE, Globals.ACCELEROMETER_SENSOR);
                        mSpeedDetectorUpdateIntent.putExtra(Globals.HABIT_FINISH_OR_NOT, mHabitFail);
                        mSpeedDetectorUpdateIntent.putExtra(Globals.SERVICE_SHOULD_STOP, true);
                        sendBroadcast(mSpeedDetectorUpdateIntent);
                        serviceHasBroadcast = true;
                        stopThisService();
                        break;
                    default:
                        break;
                }
                showFinishNofitication();
            }

        };

        cdt.start();
        super.onCreate();
    }

    //We need to declare the receiver with onReceive function as below
    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.putExtra("habitFail",mHabitFail);
            Globals.BACKGROUND_FINISH = true;
            getApplication().startActivity(dialogIntent);

            stopSelf();
        }
    };

    private void showFinishNofitication() {
        registerReceiver(stopServiceReceiver, new IntentFilter("myFilter"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("myFilter"), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = null;
        Bitmap rain = BitmapFactory.decodeResource(getResources(), R.drawable.rainicon);
        Bitmap sun = BitmapFactory.decodeResource(getResources(), R.drawable.sunshineicon);
        if(mHabitFail) {
            notification = new Notification.Builder(this)
                    .setContentTitle("Habit check Finished")
                    .setWhen(System.currentTimeMillis())
                    .setContentText("You failed for this time check in").setSmallIcon(R.drawable.haiku)
                    .setLargeIcon(rain)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent).build();
        }else{
            notification = new Notification.Builder(this)
                    .setContentTitle("Habit check Finished")
                    .setWhen(System.currentTimeMillis())
                    .setContentText("Great job! You checked in successfully").setSmallIcon(R.drawable.haiku)
                    .setLargeIcon(sun)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent).build();
        }
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT|Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(Globals.FINISH_NOTIFICATION_ID, notification);
    }

    private void stopThisService() {
        if(mNotificationManager != null){
            mNotificationManager.cancel(0);
        }

        if(mSensorManager!=null){
            mSensorManager.unregisterListener(this);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called");
        // TODO Auto-generated method stub
        showNotification();
        mServiceTaskType = intent.getIntExtra("sensorType",0);

        //Choose to start different service according to the service type
        switch (mServiceTaskType){
            case Globals.SOUND_DETECTOR:
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");
                try {
                    mRecorder.prepare();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }mRecorder.start();

                mSoundRecordTask = new OnSoundRecordTask();
                mSoundRecordTask.execute();
                break;
            case Globals.LIGHT_SENSOR:
                Log.d(TAG, "LIGHT_SENSOR");
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mSensor = mSensorManager
                        .getDefaultSensor(Sensor.TYPE_LIGHT);
                mSensorManager.registerListener((android.hardware.SensorEventListener) this, mSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);

                break;
            case Globals.ACCELEROMETER_SENSOR:
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mSensor = mSensorManager
                        .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                mSensorManager.registerListener((android.hardware.SensorEventListener) this, mSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
                mRunningSensorTask = new OnRunningSensorChangedTask();
                mRunningSensorTask.execute();
                break;
            default:
                break;
        }

        return START_NOT_STICKY; // Run until explicitly stopped.;
    }

    private class OnSoundRecordTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            while(!mCountDownFinished) {
                if(mRecorder == null){
                    Log.d(TAG,"inside soundTask recorder is null");
                }

                double amp = mRecorder.getMaxAmplitude();
                double db = 20 * Math.log10(amp / 2700.0);
                if(!Double.isInfinite(db)) {
                    mSoundDBList.add(db);
                    Log.d(TAG, "db value: " + db);
                }
            }
            return null;
        }
    }

    private class OnRunningSensorChangedTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            ArrayList<Double> instList = new ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY);
            int blockSize = 0;
            Log.d(TAG, "running task called " );
            FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
            double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
            double[] re = accBlock;
            double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];

            double max = Double.MIN_VALUE;
            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null;
                    }
                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().doubleValue();

                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0;
                        Log.d(TAG, "block full" );
                        max = .0;
                        for (double val : accBlock) {
                            if (max < val) {
                                max = val;
                            }
                        }
                        fft.fft(re, im);

                        for (int i = 0; i < re.length; i++) {
                            double mag = Math.sqrt(re[i] * re[i] + im[i]
                                    * im[i]);
                            instList.add(mag);
                            im[i] = .0; // Clear the field
                        }
                        // Append max after frequency component
                        int result = (int)WekaClassifier.classify(instList.toArray());
                        Log.d(TAG, "running result "+ result );
                        if(result == 2){

                            runningCnt += 1.0;
                        }else{
                            noneRunningCnt += 1.0;
                        }
                        instList.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLightValue = event.values[0];
            Log.i(TAG, "currentValue"+currentLightValue);
        }else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            double m = Math.sqrt(event.values[0] * event.values[0]
                    + event.values[1] * event.values[1] + event.values[2]
                    * event.values[2]);
            try {
                mAccBuffer.add(new Double(m));
            } catch (IllegalStateException e) {
                ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(
                        mAccBuffer.size() * 2);

                mAccBuffer.drainTo(newBuf);
                mAccBuffer = newBuf;
                mAccBuffer.add(new Double(m));
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onDestroy() called");
        unregisterReceiver(stopServiceReceiver);
        mSoundDBList.clear();
        if(mNotificationManager != null) {
            mNotificationManager.cancelAll(); // Cancel the persistent notification.
        }

        if(mServiceTaskType == Globals.SOUND_DETECTOR) {
            mSoundRecordTask.cancel(true);
            if(mRecorder != null){
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            }
        }else if(mServiceTaskType == Globals.ACCELEROMETER_SENSOR) {
            mSensorManager.unregisterListener(this);
            mRunningSensorTask.cancel(true);
            runningCnt = 0;
            noneRunningCnt = 0;
        }else if(mServiceTaskType == Globals.LIGHT_SENSOR){
            mSensorManager.unregisterListener(this);
        }
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called");
        return mBinder;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        SmartSensorService getService() {
            return SmartSensorService.this;
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }


    private void showNotification() {
        Log.i(TAG, "showNotification");
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(this,
                0, myIntent,
                0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Habit running")
                .setContentText("Service started").setSmallIcon(R.drawable.availableupdates)
                .setContentIntent(pendingIntent).build();
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(Globals.CHECKINGIN_NOTIFICATION_ID, notification);

    }


}
