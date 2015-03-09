package com.example.cs65project.habitme;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * setting fragment for user profile setting
 */
public class SettingFragment extends PreferenceFragment {
    //get reference from layout
    private ImageView mImageView;
    private TextView mAccountNameText;
    private Activity mContext;

    //request code for capture and crop photo
    private static final int REQUEST_PICK_PHOTO = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_FINISH = 2;

    //deal with image and take photos
    private static Uri imageUri;
    public static byte[] byteArray;
    public static String mCameraFileName;
    public static final String PHOTO_DIR = "/Photos/";

    //shared preference to store data
    public static SharedPreferences prefs;
    private String MyPreferences = "MYPREFERENCE";

    //used for card view list layout
    private static RecyclerView recList;
    private static HomeListAdapter ca;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(byteArray!=null){
            outState.putByteArray("array_key",byteArray);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        FragmentManager childFragmentManager = getChildFragmentManager();
        mContext = getActivity();
        prefs = mContext.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        mCameraFileName = prefs.getString("image-path","");

        //find reference from layout
        mAccountNameText = (TextView)v.findViewById(R.id.accountname);
        mAccountNameText.setText(Globals.user.getUname());
        mImageView = (ImageView)v.findViewById(R.id.imageProfile);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangePhotoClicked(v);
            }
        });

        //load pictures
        if(savedInstanceState != null){
            byteArray = savedInstanceState.getByteArray("array_key");
            if(byteArray!=null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                mImageView.setImageBitmap(bitmap);
            }else{
                load_photo();
            }
        }else{
            load_photo();
        }


        //change what's up content
        v.findViewById(R.id.whatsup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialogue_whatsup_title);
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // set up buttons
                builder.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ((TextView)v.findViewById(R.id.whatsup)).setText(input.getText());
                            }
                        });
                builder.setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                //TODO:set up dialog button action
                            }
                        });
                builder.create();
                builder.show();
            }
        });

        //show in card view list
        recList = (RecyclerView) v.findViewById(R.id.cardList_setting);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        ca = new HomeListAdapter(HomeFragment.mEntryList);
        recList.setAdapter(ca);

        //enable click listener on reclist
        recList.addOnItemTouchListener(
                new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(mContext, PersonalCheckHabitPostActivity.class);

                        HabitItem mEntry = HomeFragment.mEntryList.get(position);
                        intent.putExtra("Title", mEntry.getHabitTitle());
                        startActivity(intent);
                    }
                })
        );
        return v;
    }

    /**
     * save photos
     */
    private void savePhoto() {
        EntryDataSource datasource = new EntryDataSource(mContext);
        datasource.open();
        datasource.updatePostImage(mCameraFileName);
        datasource.close();
        updateUserImg();

        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = mContext.openFileOutput(
                    getString(R.string.profile_photo_file_name), mContext.MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * update user iamge
     */
    private void updateUserImg() {
        JSONObject user = new JSONObject();
        try {
            user.put("uid",Globals.user.getUid());
            user.put("uimg",mCameraFileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        registerUser(user.toString());
    }

    /**
     * register user with our server
     * @param data
     */
    private void registerUser(String data) {
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String url = getString(R.string.server_addr) + "/registeruser.do";
                String res = "";
                Map<String, String> map = new HashMap<String,String>();
                map.put("old_user", params[0]);
                try {
                    //res = ServerUtilities.post(url,map);
                    res = ServerUtilities.post(url,map,"application/json");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return res;
            }
        }.execute(data);
    }

    private void load_photo() {
        try {
            FileInputStream fis = mContext.openFileInput(getString(R.string.profile_photo_file_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default profile photo if no photo saved before.
            if(Globals.user.getUname().equals("haomin")){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.octopus));
            }else if(Globals.user.getUname().equals("boying")){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.lobster));
            }else if(Globals.user.getUname().equals("mubing")){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.tuna));
            }else if(Globals.user.getUname().equals("yuan")){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.chicken));
            }else if(Globals.user.getUname().equals("guest")){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.panda));
            }
        }
    }

    public void onChangePhotoClicked(View v){
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
        imageUri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("return-data",true);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    public void pickFromGallery() {
        Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_PICK);
        startActivityForResult(in, REQUEST_PICK_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK)
            return;
        switch(requestCode){
            case REQUEST_TAKE_PHOTO:
                if(resultCode==Activity.RESULT_OK)
                    cropImage();
                break;
            case REQUEST_PICK_PHOTO:
                imageUri = data.getData();
                cropImage();
                break;
            case REQUEST_FINISH:
                Bundle extras = data.getExtras();
                if(extras!=null){
                    Bitmap photo = (Bitmap)extras.getParcelable("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    mImageView.setImageBitmap(photo);

                    Uri uri = data.getData();
                    Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
                    if (cursor == null) {
                    // Source is Dropbox or other similar local file path
                        mCameraFileName = uri.getPath();
                    } else {
                        cursor.moveToFirst();
                        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        mCameraFileName = cursor.getString(idx);
                        cursor.close();
                    }

                    File file = new File(mCameraFileName);
                    UploadPicture upload = new UploadPicture(mContext, LoginActivity.mApi, PHOTO_DIR, file);
                    upload.execute();

                    mCameraFileName = mCameraFileName.substring(mCameraFileName.lastIndexOf("/")+1);
                    prefs.edit().putString("image-path",mCameraFileName);
                    Globals.user.setUimg(mCameraFileName);
                    savePhoto();

                }
        }
    }

    private void cropImage() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri,"image/*");
        cropIntent.putExtra("crop",true);
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("outputX",300);
        cropIntent.putExtra("outputY",300);
        cropIntent.putExtra("return-data",true);
        startActivityForResult(cropIntent,REQUEST_FINISH);
    }

}
