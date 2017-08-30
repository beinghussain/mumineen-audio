package com.mumineendownloads.mumineenaudio.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mumineendownloads.mumineenaudio.Helpers.PrefManager;
import com.mumineendownloads.mumineenaudio.R;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefManager prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            openMainScreen();
            finish();
        } else {
            openStartupScreen();
        }
    }

    public void openStartupScreen() {
        startActivity(new Intent(Splash.this, Startup.class));
        finish();
    }

    public void openMainScreen() {
        startActivity(new Intent(Splash.this, MainActivity.class));
        finish();
    }
}
