package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * When user clicks on make post, then user can post a message.
 * User can choose if the post is Public or Private.
 */
public class MakePostActivity extends Activity {
    //request code for capture and crop photo
    private static final int REQUEST_PICK_PHOTO = 0;
    private static final int REQUEST_FINISH = 2;

    //reference for layout
    private ImageView mTakePhotoView;
    private TextView mPostText;
    private Switch locationSwitch;
    private RadioGroup radioButtonGroup;
    private View radioButton;
    private Context mContext = this;

    //variables for camera and photos
    private String mCameraFileName;
    private static byte[] byteArray;

    //variables for location
    public static final String PHOTO_DIR = "/Photos/";
    private LocationManager locationManager;
    private String provider;
    private Location location;
    String address = new String();

    //other userful variables
    public static EntryDataSource datasource;
    private static int privacy;
    private Intent intent;
    private long mEntryId;

    /**
     * save instance for profile
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(byteArray != null){
            outState.putByteArray("array_key", byteArray);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);

        //change action bar color
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(102, 153, 0)));

        //get intent and store entry id
        intent = getIntent();
        mEntryId = intent.getLongExtra("ID",0);

        //open database
        datasource = new EntryDataSource(this);
        datasource.open();

        //get reference from layout
        mPostText = (TextView)findViewById(R.id.postContentText);
        radioButtonGroup = (RadioGroup)findViewById(R.id.select_privacy);
        mTakePhotoView = (ImageView)findViewById(R.id.postPicture);
        mTakePhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoClicked();
            }
        });
        locationSwitch = (Switch)findViewById(R.id.locationToggle);
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setText(getAddress(location));
                } else {
                    buttonView.setText("Location Disabled");
                }
            }
        });


        if(savedInstanceState != null){
            byteArray = savedInstanceState.getByteArray("array_key");
            if(byteArray!=null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                mTakePhotoView.setImageBitmap(bitmap);
            }else{
                load_photo();
            }
        }else{
            load_photo();
        }

        //location manager to get location and display
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setCostAllowed(true);
        provider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(provider);
        address = getAddress(location);
    }


    //send post to friend circlue
    public static void sendPostStream(HabitItem entry, String url,String filename, String content) {
        FriendPostItem item = new FriendPostItem();
        item.setUid(Globals.user.getUname());
        item.setUser_img(SettingFragment.mCameraFileName);
        item.setPost_title(entry.getHabitTitle());
        item.setPost_content(content);
        item.setPost_img(filename);
        item.setLocation(entry.getLocation());
        item.setPrivacy(privacy);
        item.setPid(System.currentTimeMillis()+Globals.user.getUname());

        //insert item to database
        long insertId = datasource.insertPosts(item);

        //send post to server
        JSONObject post = new JSONObject();
        try {
            post.put("uid",Globals.user.getUid());
            post.put("title",entry.getHabitTitle());
            post.put("content",content);
            String[] timestamps = entry.getCheckTimeList().split(",");
            post.put("time",timestamps[timestamps.length-1]);
            post.put("img",filename);
            post.put("location",entry.getLocation());
            post.put("privacy",privacy);
            post.put("pid",item.getPid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postEntry(url, post.toString());
    }

    /**
     * Post entry to server
     * @param urlHolder
     * @param data
     */
    private static void postEntry(final String urlHolder,String data){
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String url = urlHolder + "/post.do";
                String res = "";
                Map<String, String> map = new HashMap<String,String>();
                map.put("new_post", params[0]);
                try {
                    res = ServerUtilities.post(url,map,"application/json");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return res;
            }
        }.execute(data);
    }

    /**
     * Take photo when click on photo button
     */
    private void takePhotoClicked() {
        AlertDialog dialog  = new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.dialog_title))
                .setItems(R.array.photo_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0:
                                takePhoto();
                                return;
                            case 1:
                                pickFromGallery();
                                return;
                        }
                    }
                }).create();
        dialog.show();
    }


    /**
     * set picture on lay out when user get a photo
     */
    private void load_photo() {
            mTakePhotoView.setImageResource(R.drawable.addpicture);
    }

    /**
     * User take photo
     */
    public void takePhoto() {
        Intent intent = new Intent();
        // Picture from camera
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);

        String newPicFile = df.format(date) + ".jpg";
        String outPath = new File(Environment.getExternalStorageDirectory(), newPicFile).getPath();
        File outFile = new File(outPath);
        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, REQUEST_FINISH);
    }

    public void pickFromGallery() {
        Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_PICK);
        startActivityForResult(in, REQUEST_PICK_PHOTO);
    }

    /**
     * reuse my runs taking photo's code
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK)
            return;
        switch(requestCode){
            case REQUEST_FINISH:
                break;
            case REQUEST_PICK_PHOTO:
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor == null) {
                // Source is Dropbox or other similar local file path
                    mCameraFileName = uri.getPath();
                } else {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    mCameraFileName = cursor.getString(idx);
                    cursor.close();
                }
                break;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10;
        Bitmap photo = BitmapFactory.decodeFile(mCameraFileName, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();
        Bitmap bMapScaled = Bitmap.createScaledBitmap(photo, 300, 300, true);
        mTakePhotoView.setImageBitmap(bMapScaled);
    }

    private String getAddress(Location location) {
        String addressString = "";

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder gc = new Geocoder(this, Locale.getDefault());

        if (!Geocoder.isPresent())
            addressString = "No geocoder available";
        else {
            try {
                List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
                StringBuilder sb = new StringBuilder();
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    sb.append(address.getAddressLine(0)).append(", ");
                    sb.append(address.getLocality());
                }
                addressString = sb.toString();
            } catch (IOException e) {
                Log.d("WHEREAMI", "IO Exception", e);
            }
        }
        return addressString;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.post_action) {
            Uri uri = null;
            if (uri == null && mCameraFileName != null) {
                uri = Uri.fromFile(new File(mCameraFileName));
            }
            if (uri == null && mCameraFileName != null) {
                uri = Uri.fromFile(new File(mCameraFileName));
            }
            if(mCameraFileName!=null){
                File file = new File(mCameraFileName);
                if (uri != null&&mCameraFileName!=null) {
                    UploadPicture upload = new UploadPicture(mContext, LoginActivity.mApi, PHOTO_DIR, file);
                    upload.execute();
                }
            }else{
                mCameraFileName = "";
            }
            int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
            radioButton = radioButtonGroup.findViewById(radioButtonID);
            privacy = radioButtonGroup.indexOfChild(radioButton);

            long currentTime = System.currentTimeMillis();
            String content = mPostText.getText().toString();
            HabitItem entry  = datasource.fetchEntryByIndex(mEntryId);
            Intent intent = new Intent(this,MainActivity.class);
            if(entry.getCreateType()==Globals.INPUTTYPE_CHOOSE){
                intent.putExtra("checkintime",currentTime);
                intent.putExtra("location",locationSwitch.getText().toString());
                intent.putExtra("filename", mCameraFileName.substring(mCameraFileName.lastIndexOf("/") + 1));
                intent.putExtra("content",content);
            }else{
                entry.addCheckTimeList(currentTime);
                entry.setLocation(locationSwitch.getText().toString());
                datasource.updateEntry(mEntryId,entry);
                String urlHolder = getString(R.string.server_addr);
                sendPostStream(entry,urlHolder,mCameraFileName.substring(mCameraFileName.lastIndexOf("/")+1),content);
            }

            intent.putExtra(Globals.START_SERVICE, true);
            intent.putExtra(Globals.POST_OR_NOT, true);
            intent.putExtra(Globals.ENTRY_ID, mEntryId);
            startActivity(intent);
            finish();
            return true;
        }

        if(id == R.id.cancel_action){
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra(Globals.START_SERVICE, false);
            intent.putExtra(Globals.POST_OR_NOT, false);
            intent.putExtra(Globals.ENTRY_ID, mEntryId);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(Globals.START_SERVICE, false);
        intent.putExtra(Globals.POST_OR_NOT, false);
        intent.putExtra(Globals.ENTRY_ID, mEntryId);
        startActivity(intent);
        super.onBackPressed();
    }

}
