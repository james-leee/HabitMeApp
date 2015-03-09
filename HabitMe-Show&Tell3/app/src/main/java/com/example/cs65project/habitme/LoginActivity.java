package com.example.cs65project.habitme;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    private static final String LOGIN_USERNAME = "USER_NAME" ;
    private static final String LOGIN_PASSWORD = "PASS_WORD";
    private static final String USERID = "USER_ID";
    private EditText mUsername,mPassword;
    private Button mSubmit;
    private String usrname,psword;
    Intent intent;
    private Context mContext = this;
    SharedPreferences sharedpreferences;
    private final String MyPreferences = "My Preference";

    public static DropboxAPI<AndroidAuthSession> mApi;
    private static final String APP_KEY = "9o9m9xz6k1zm4jn";
    private static final String APP_SECRET = "gbe9q8nvroxbq58";

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private boolean mLoggedIn;
    private static final int NEW_PICTURE = 1;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOGIN_USERNAME,usrname);
        outState.putString(LOGIN_PASSWORD,psword);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        checkAppKeySetup();
        if (mLoggedIn) {
            //logOut();
        } else {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            String key = prefs.getString(ACCESS_KEY_NAME, null);
            String secret = prefs.getString(ACCESS_SECRET_NAME, null);
            if(key==null&&secret==null){
                mApi.getSession().startOAuth2Authentication(LoginActivity.this);
            }
        }
        setLoggedIn(mApi.getSession().isLinked());





        ActionBar actionBar = getActionBar();
        actionBar.hide();

        intent = new Intent(this,MainActivity.class);


        mUsername = (EditText)findViewById(R.id.usr_text);
        mPassword = (EditText)findViewById(R.id.psw_text);
        mSubmit = (Button)findViewById(R.id.submit_button);

        sharedpreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        if(sharedpreferences.contains(LOGIN_USERNAME)&&sharedpreferences.contains(LOGIN_PASSWORD)){
            Globals.user = new User();
            Globals.user.setUid(sharedpreferences.getString(USERID,""));
            Globals.user.setUname(sharedpreferences.getString(LOGIN_USERNAME,""));
            FileInputStream fis = null;
            try {
                fis = mContext.openFileInput(getString(R.string.profile_photo_file_name));
                Bitmap bmap = BitmapFactory.decodeStream(fis);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //Globals.user.setImg(stream.toByteArray());
            } catch (FileNotFoundException e) {
                Drawable d = mContext.getDrawable(R.drawable.panda);
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                //Globals.user.setImg(stream.toByteArray());
            }
            startActivity(intent);
        }

        if(savedInstanceState!=null){
            mUsername.setText(usrname);
            mPassword.setText(psword);
        }


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usrname = mUsername.getText().toString();
                psword = mPassword.getText().toString();
                if(usrname.equals("haomin")&&psword.equals("123")||
                        usrname.equals("boying")&&psword.equals("456")||
                        usrname.equals("mubing")&&psword.equals("789")||
                        usrname.equals("yuan")&&psword.equals("000")||
                        usrname.equals("guest") && psword.equals("guest")){
                    Globals.user = new User();
                    String uid = String.valueOf(System.currentTimeMillis() + usrname);
                    Globals.user.setUid(uid);
                    Globals.user.setUname(usrname);
                    Drawable draw = mContext.getDrawable(R.drawable.panda);
                    Bitmap bitmap = ((BitmapDrawable)draw).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    regUser();
                    SharedPreferences.Editor editor= sharedpreferences.edit();
                    editor.putString(LOGIN_USERNAME, usrname);
                    editor.putString(LOGIN_PASSWORD,psword);
                    editor.putString(USERID,uid);
                    editor.commit();

                    startActivity(intent);
                }else{
                    Toast.makeText(mContext,"Wrong password!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void regUser() {
        JSONObject user = new JSONObject();
        try {
            user.put("uid",Globals.user.getUid());
            user.put("uname",Globals.user.getUname());
            user.put("uimg","");
            user.put("regtime",String.valueOf(System.currentTimeMillis()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
//
//        StringBuilder res = new StringBuilder();
//        res.append(Globals.user.getUid()+"#");
//        res.append(Globals.user.getUname()+"#");
//        res.append(" " +"#");
//        res.append(String.valueOf(System.currentTimeMillis()));
        registerUser(user.toString());
    }

    private void registerUser(String data) {
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String url = getString(R.string.server_addr) + "/registeruser.do";
                String res = "";
                Map<String, String> map = new HashMap<String,String>();
                map.put("new_user", params[0]);
                try {
                    res = ServerUtilities.post(url,map,"application/json");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return res;
            }
        }.execute(data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
            }
        }
        super.onResume();
    }
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }

    private void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
    }

}
