package com.example.cs65project.habitme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

//http://www.cnblogs.com/ManUtd/archive/2012/03/01/2373582.html
//http://www.cnblogs.com/qianxudetianxia/archive/2012/12/30/2839700.html
public class SplashActivity extends Activity {

    private static final int LOAD_DISPLAY_TIME = 1500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                /* Create an Intent that will start the Main WordPress Activity. */
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                SplashActivity.this.finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, LOAD_DISPLAY_TIME); //1500 for release

    }
}